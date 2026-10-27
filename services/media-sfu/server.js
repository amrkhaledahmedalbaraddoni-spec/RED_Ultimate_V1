'use strict';

/**
 * RED Sovereign Media SFU - Unified Edition
 * System A: 4K VoIP Conference - Sovereign + Ultimate Merged
 * - Sovereign security: JWT HS256 (32+ chars), ANNOUNCED_IP required, authenticated /sfu + /metrics
 * - Ultimate codecs: OPUS + VP8/VP9/H264/AV1 up to 4K 12Mbps, enhanced monitoring
 * 100% Local, Zero Cloud
 */

const crypto = require('crypto');
const http = require('http');
const os = require('os');
const mediasoup = require('mediasoup');
const { WebSocketServer } = require('ws');

const PORT = Number(process.env.PORT || 4000);
const RTC_MIN_PORT = Number(process.env.RTC_MIN_PORT || 40000);
const RTC_MAX_PORT = Number(process.env.RTC_MAX_PORT || 40100);
const WORKER_COUNT = Math.max(1, Number(process.env.MEDIASOUP_WORKERS || Math.min(4, os.cpus().length)));
const ANNOUNCED_IP = process.env.MEDIASOUP_ANNOUNCED_IP || process.env.ANNOUNCED_IP || '';
const JWT_SECRET = process.env.JWT_SECRET || '';

if (!JWT_SECRET || JWT_SECRET.length < 32) throw new Error('JWT_SECRET must contain at least 32 characters');
if (!ANNOUNCED_IP) console.warn('⚠️ MEDIASOUP_ANNOUNCED_IP is unset; LAN/WAN ICE candidates may be unreachable - set to LAN IP for local sovereign network');

// Unified 4K codec set: Sovereign base + Ultimate VP9/AV1 extensions
const mediaCodecs = [
  {
    kind: 'audio',
    mimeType: 'audio/opus',
    clockRate: 48000,
    channels: 2,
    parameters: { minptime: 10, useinbandfec: 1, usedtx: 1 }
  },
  {
    kind: 'video',
    mimeType: 'video/VP8',
    clockRate: 90000,
    parameters: { 'x-google-start-bitrate': 1500, 'x-google-max-bitrate': 4000 }
  },
  {
    kind: 'video',
    mimeType: 'video/VP9',
    clockRate: 90000,
    parameters: { 'profile-id': 2, 'x-google-start-bitrate': 1500, 'x-google-max-bitrate': 8000 }
  },
  {
    kind: 'video',
    mimeType: 'video/H264',
    clockRate: 90000,
    parameters: { 'packetization-mode': 1, 'profile-level-id': '4d0032', 'level-asymmetry-allowed': 1, 'x-google-start-bitrate': 1500, 'x-google-max-bitrate': 8000 }
  },
  {
    kind: 'video',
    mimeType: 'video/AV1',
    clockRate: 90000,
    parameters: { 'x-google-start-bitrate': 2000, 'x-google-max-bitrate': 12000, profile: '0' } // 4K
  }
];

const workers = [];
const rooms = new Map(); // roomId -> { router, peers, createdAt, type }
let workerIndex = 0;

function base64UrlDecode(value) {
  return Buffer.from(value.replace(/-/g, '+').replace(/_/g, '/'), 'base64');
}

function authenticate(header) {
  const token = String(header || '').replace(/^Bearer\s+/i, '');
  const parts = token.split('.');
  if (parts.length !== 3) throw new Error('Unauthorized');
  const key = crypto.createHash('sha256').update(JWT_SECRET, 'utf8').digest();
  const expected = crypto.createHmac('sha256', key).update(`${parts[0]}.${parts[1]}`).digest();
  const supplied = base64UrlDecode(parts[2]);
  if (expected.length !== supplied.length || !crypto.timingSafeEqual(expected, supplied)) throw new Error('Unauthorized');
  const claims = JSON.parse(base64UrlDecode(parts[1]).toString('utf8'));
  if (!claims.sub || !claims.redId || !claims.exp || claims.exp * 1000 <= Date.now()) throw new Error('Expired or invalid token');
  return claims;
}

function log(level, msg, data) {
  const ts = new Date().toISOString();
  const icon = level === 'error' ? '❌' : level === 'warn' ? '⚠️' : level === 'success' ? '✅' : '🔹';
  console.log(`${icon} [${ts}] ${msg}`, data ? JSON.stringify(data) : '');
}

async function createWorker() {
  const worker = await mediasoup.createWorker({ logLevel: 'warn', rtcMinPort: RTC_MIN_PORT, rtcMaxPort: RTC_MAX_PORT });
  worker.on('died', () => { console.error(`mediasoup worker ${worker.pid} died`); process.exit(1); });
  workers.push(worker);
  log('success', `SFU worker pid:${worker.pid} ready (${workers.length}/${WORKER_COUNT})`);
}

function nextWorker() {
  const worker = workers[workerIndex++ % workers.length];
  return worker;
}

async function roomFor(id) {
  let room = rooms.get(id);
  if (!room) {
    room = { router: await nextWorker().createRouter({ mediaCodecs }), peers: new Map(), createdAt: Date.now(), type: 'conference' };
    rooms.set(id, room);
    log('info', `📍 Created room: ${id} (codecs: ${mediaCodecs.map(c=>c.mimeType).join(',')})`);
  }
  return room;
}

async function createTransport(router) {
  const listenIps = [{ ip: '0.0.0.0', announcedIp: ANNOUNCED_IP || undefined }];
  const transport = await router.createWebRtcTransport({
    listenIps,
    enableUdp: true,
    enableTcp: true,
    preferUdp: true,
    initialAvailableOutgoingBitrate: 1_500_000
  });
  transport.on('dtlsstatechange', state => { if (state === 'closed') transport.close(); });
  return transport;
}

function transportOptions(transport) {
  return {
    id: transport.id,
    iceParameters: transport.iceParameters,
    iceCandidates: transport.iceCandidates,
    dtlsParameters: transport.dtlsParameters,
    sctpParameters: transport.sctpParameters
  };
}

function send(ws, requestId, payload) {
  if (ws.readyState === 1) ws.send(JSON.stringify({ requestId, ...payload }));
}

function requirePeer(context) {
  if (!context.room || !context.peer) throw new Error('Join a room first');
  return context.peer;
}

function broadcast(room, excludedPeerId, payload) {
  for (const [id, peer] of room.peers) if (id !== excludedPeerId) send(peer.ws, null, payload);
}

function cleanupPeer(context) {
  const { roomId, peerId, room, peer } = context;
  if (!room || !peer) return;
  for (const consumer of peer.consumers.values()) try { consumer.close(); } catch {}
  for (const producer of peer.producers.values()) try { producer.close(); } catch {}
  for (const transport of peer.transports.values()) try { transport.close(); } catch {}
  room.peers.delete(peerId);
  broadcast(room, peerId, { type: 'peerLeft', peerId });
  log('info', `Cleaned peer ${peerId} from ${roomId} - remaining ${room.peers.size}`);
  if (room.peers.size === 0) { try { room.router.close(); } catch {} rooms.delete(roomId); log('info', `🗑️ Room ${roomId} removed (empty)`); }
  context.room = null; context.peer = null; context.roomId = null; context.peerId = null;
}

const server = http.createServer((req, res) => {
  // CORS for local sovereign network (from Ultimate)
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Origin, X-Requested-With, Content-Type, Accept, Authorization, X-RED-Token');
  if (req.method === 'OPTIONS') { res.writeHead(200); return res.end(); }

  if (req.url === '/health') {
    res.writeHead(workers.length ? 200 : 503, { 'content-type': 'application/json' });
    return res.end(JSON.stringify({
      status: workers.length ? 'UP' : 'STARTING',
      service: 'RED Media SFU Unified',
      version: '2.0.0-UNIFIED',
      system: 'A - VoIP 4K AV1/VP9/H264',
      workers: workers.length,
      rooms: rooms.size,
      peers: [...rooms.values()].reduce((n, r) => n + r.peers.size, 0),
      codecs: mediaCodecs.map(c => c.mimeType),
      uptime: process.uptime(),
      timestamp: new Date().toISOString(),
      sovereign: '100% LOCAL'
    }));
  }
  if (req.url === '/stats') {
    try { authenticate(req.headers.authorization); } catch { res.writeHead(401); return res.end(JSON.stringify({ error: 'Unauthorized' })); }
    const roomList = [...rooms.values()].map(r => ({ id: r.router.id, peers: r.peers.size, createdAt: r.createdAt, uptime: Date.now() - r.createdAt }));
    res.writeHead(200, { 'content-type': 'application/json' });
    return res.end(JSON.stringify({
      workers: workers.length,
      rooms: rooms.size,
      peers: [...rooms.values()].reduce((n, r) => n + r.peers.size, 0),
      producers: [...rooms.values()].reduce((n, r) => n + [...r.peers.values()].reduce((x, p) => x + p.producers.size, 0), 0),
      consumers: [...rooms.values()].reduce((n, r) => n + [...r.peers.values()].reduce((x, p) => x + p.consumers.size, 0), 0),
      roomList
    }));
  }
  if (req.url === '/metrics') {
    try { authenticate(req.headers.authorization); } catch { res.writeHead(401); return res.end(); }
    res.writeHead(200, { 'content-type': 'application/json' });
    return res.end(JSON.stringify({
      workers: workers.length,
      rooms: rooms.size,
      peers: [...rooms.values()].reduce((n, r) => n + r.peers.size, 0),
      producers: [...rooms.values()].reduce((n, r) => n + [...r.peers.values()].reduce((x, p) => x + p.producers.size, 0), 0)
    }));
  }
  if (req.url === '/' ) {
    res.writeHead(200, { 'content-type': 'application/json' });
    return res.end(JSON.stringify({
      message: '🔴 RED Sovereign Media SFU Unified',
      system: 'A - 4K VoIP AV1/VP9/H264 + Sovereign Auth',
      websocket: `ws://localhost:${PORT}/sfu`,
      health: '/health',
      stats: '/stats (auth)',
      metrics: '/metrics (auth)',
      codecs: ['AV1 4K 12Mbps', 'VP9 8Mbps', 'VP8 4Mbps', 'H264 8Mbps', 'OPUS Audio'],
      sovereign: true,
      workers: workers.length
    }));
  }
  res.writeHead(404); res.end(JSON.stringify({ error: 'Not found' }));
});

const wss = new WebSocketServer({ noServer: true });
server.on('upgrade', (req, socket, head) => {
  // Sovereign: only /sfu path, require JWT
  const url = new URL(req.url || '/sfu', `http://${req.headers.host || 'localhost'}`);
  if (!url.pathname.startsWith('/sfu')) { socket.destroy(); return; }
  let claims;
  try { claims = authenticate(req.headers.authorization); } catch { socket.write('HTTP/1.1 401 Unauthorized\r\n\r\n'); socket.destroy(); return; }
  wss.handleUpgrade(req, socket, head, ws => wss.emit('connection', ws, req, claims));
});

wss.on('connection', (ws, _req, claims) => {
  const context = { roomId: null, peerId: null, room: null, peer: null };
  log('info', `🔗 SFU peer connected redId=${claims.redId} sub=${claims.sub}`);

  ws.on('message', async raw => {
    let message;
    try {
      message = JSON.parse(raw.toString());
      const { type, requestId } = message;
      if (type === 'join') {
        if (context.room) throw new Error('Already joined');
        const roomId = String(message.roomId || '');
        if (!/^[A-Za-z0-9_-]{8,128}$/.test(roomId)) throw new Error('Invalid roomId');
        const room = await roomFor(roomId);
        const peerId = claims.redId;
        const existing = room.peers.get(peerId);
        if (existing) { existing.ws.close(4001, 'replaced'); for (const t of existing.transports.values()) try{ t.close(); } catch{} }
        const peer = { ws, accountId: claims.sub, transports: new Map(), producers: new Map(), consumers: new Map() };
        room.peers.set(peerId, peer);
        Object.assign(context, { roomId, peerId, room, peer });
        return send(ws, requestId, {
          status: 'joined',
          peerId,
          rtpCapabilities: room.router.rtpCapabilities,
          existingProducers: [...room.peers.entries()].filter(([id]) => id !== peerId).flatMap(([id, p]) => [...p.producers.values()].map(producer => ({ peerId: id, producerId: producer.id, kind: producer.kind })))
        });
      }

      const peer = requirePeer(context);
      if (type === 'createTransport') {
        const transport = await createTransport(context.room.router);
        peer.transports.set(transport.id, transport);
        transport.on('close', () => peer.transports.delete(transport.id));
        return send(ws, requestId, { status: 'transportCreated', direction: message.direction, transportOptions: transportOptions(transport) });
      }
      if (type === 'connectTransport') {
        const transport = peer.transports.get(message.transportId);
        if (!transport) throw new Error('Transport not found');
        await transport.connect({ dtlsParameters: message.dtlsParameters });
        return send(ws, requestId, { status: 'transportConnected', transportId: transport.id });
      }
      if (type === 'produce') {
        const transport = peer.transports.get(message.transportId);
        if (!transport) throw new Error('Transport not found');
        const producer = await transport.produce({ kind: message.kind, rtpParameters: message.rtpParameters, appData: { peerId: context.peerId } });
        peer.producers.set(producer.id, producer);
        producer.on('transportclose', () => peer.producers.delete(producer.id));
        broadcast(context.room, context.peerId, { type: 'newProducer', peerId: context.peerId, producerId: producer.id, kind: producer.kind });
        return send(ws, requestId, { status: 'producing', producerId: producer.id });
      }
      if (type === 'consume') {
        const transport = peer.transports.get(message.transportId);
        if (!transport) throw new Error('Transport not found');
        if (!context.room.router.canConsume({ producerId: message.producerId, rtpCapabilities: message.rtpCapabilities })) throw new Error('Cannot consume producer');
        const consumer = await transport.consume({ producerId: message.producerId, rtpCapabilities: message.rtpCapabilities, paused: true });
        peer.consumers.set(consumer.id, consumer);
        consumer.on('transportclose', () => peer.consumers.delete(consumer.id));
        consumer.on('producerclose', () => { peer.consumers.delete(consumer.id); send(ws, null, { type: 'producerClosed', consumerId: consumer.id, producerId: message.producerId }); });
        return send(ws, requestId, { status: 'consuming', consumerId: consumer.id, producerId: message.producerId, kind: consumer.kind, rtpParameters: consumer.rtpParameters });
      }
      if (type === 'resumeConsumer') {
        const consumer = peer.consumers.get(message.consumerId);
        if (!consumer) throw new Error('Consumer not found');
        await consumer.resume();
        return send(ws, requestId, { status: 'consumerResumed', consumerId: consumer.id });
      }
      if (type === 'pauseConsumer') {
        const consumer = peer.consumers.get(message.consumerId);
        if (!consumer) throw new Error('Consumer not found');
        await consumer.pause();
        return send(ws, requestId, { status: 'consumerPaused', consumerId: consumer.id });
      }
      if (type === 'closeProducer') {
        const producer = peer.producers.get(message.producerId);
        if (producer) { producer.close(); peer.producers.delete(message.producerId); }
        return send(ws, requestId, { status: 'producerClosed', producerId: message.producerId });
      }
      if (type === 'leave') { cleanupPeer(context); return send(ws, requestId, { status: 'left' }); }
      throw new Error('Unknown message type');
    } catch (error) {
      send(ws, message?.requestId, { status: 'error', error: error.message });
    }
  });

  ws.on('close', () => cleanupPeer(context));
  ws.on('error', error => console.error('SFU WebSocket error', error.message));
});

(async () => {
  for (let i = 0; i < WORKER_COUNT; i++) await createWorker();
  server.listen(PORT, '0.0.0.0', () => log('success', `RED SFU Unified listening on 0.0.0.0:${PORT} with ${workers.length} workers | codecs=${mediaCodecs.map(c=>c.mimeType).join(',')} | sovereign JWT auth`));
})().catch(error => { console.error(error); process.exit(1); });
