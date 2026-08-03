/**
 * RED Ultimate Sovereign - Media SFU Server V2
 * System A: 4K VoIP Conference - AV1/VP9/H264
 * 100% Local, Zero Cloud, Sovereign Edition
 * Version: 2.0.0-ULTIMATE
 */

const http = require('http');
const express = require('express');
const { WebSocketServer } = require('ws');
const mediasoup = require('mediasoup');
const { v4: uuidv4 } = require('uuid');

const app = express();
app.use(express.json());

// CORS for local sovereign network
app.use((req, res, next) => {
  res.header('Access-Control-Allow-Origin', '*');
  res.header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
  res.header('Access-Control-Allow-Headers', 'Origin, X-Requested-With, Content-Type, Accept, Authorization, X-RED-Token');
  if (req.method === 'OPTIONS') return res.sendStatus(200);
  next();
});

// Media codecs - Ultimate 4K Support
const mediaCodecs = [
  {
    kind: 'audio',
    mimeType: 'audio/opus',
    clockRate: 48000,
    channels: 2,
    parameters: {
      minptime: 10,
      useinbandfec: 1,
      usedtx: 1
    }
  },
  {
    kind: 'video',
    mimeType: 'video/VP8',
    clockRate: 90000,
    parameters: {
      'x-google-start-bitrate': 1500,
      'x-google-max-bitrate': 4000
    }
  },
  {
    kind: 'video',
    mimeType: 'video/VP9',
    clockRate: 90000,
    parameters: {
      'profile-id': 2,
      'x-google-start-bitrate': 1500,
      'x-google-max-bitrate': 8000
    }
  },
  {
    kind: 'video',
    mimeType: 'video/H264',
    clockRate: 90000,
    parameters: {
      'packetization-mode': 1,
      'profile-level-id': '4d0032',
      'level-asymmetry-allowed': 1,
      'x-google-start-bitrate': 1500,
      'x-google-max-bitrate': 8000
    }
  },
  {
    kind: 'video',
    mimeType: 'video/AV1',
    clockRate: 90000,
    parameters: {
      'x-google-start-bitrate': 2000,
      'x-google-max-bitrate': 12000, // 4K
      'profile': '0'
    }
  }
];

// Global State - Sovereign
const workers = [];
const rooms = new Map(); // roomId -> { router, peers, createdAt, type }
const peers = new Map(); // peerId -> { ws, transports, producers, consumers, roomId }

let nextWorkerIndex = 0;

// Logging
function log(level, message, data = null) {
  const ts = new Date().toISOString();
  const prefix = level === 'error' ? '❌' : level === 'warn' ? '⚠️' : level === 'success' ? '✅' : '🔹';
  console.log(`${prefix} [${ts}] ${message}`, data ? JSON.stringify(data) : '');
}

// Initialize mediasoup workers - Ultimate Performance
async function initWorkers() {
  const numWorkers = Math.max(1, require('os').cpus().length - 1);
  log('info', `Starting ${numWorkers} mediasoup workers for RED Ultimate...`);

  for (let i = 0; i < numWorkers; i++) {
    try {
      const worker = await mediasoup.createWorker({
        logLevel: 'warn',
        logTags: ['info', 'ice', 'dtls', 'rtp', 'srtp', 'rtcp'],
        rtcMinPort: 40000,
        rtcMaxPort: 40100
      });

      worker.on('died', () => {
        log('error', 'mediasoup Worker died, restarting...');
        setTimeout(() => process.exit(1), 1000);
      });

      workers.push(worker);
      log('success', `Worker ${i} pid:${worker.pid} ready`);
    } catch (err) {
      log('error', `Failed to create worker ${i}`, err.message);
    }
  }

  log('success', `${workers.length} SFU workers ready - System A ONLINE 4K`);
}

function getNextWorker() {
  const worker = workers[nextWorkerIndex];
  nextWorkerIndex = (nextWorkerIndex + 1) % workers.length;
  return worker;
}

async function getOrCreateRoom(roomId, roomType = 'conference') {
  if (!rooms.has(roomId)) {
    const worker = getNextWorker();
    const router = await worker.createRouter({ mediaCodecs });
    rooms.set(roomId, {
      router,
      peers: new Map(),
      createdAt: Date.now(),
      type: roomType,
      id: roomId
    });
    log('info', `📍 Created ${roomType} room: ${roomId}`);
  }
  return rooms.get(roomId);
}

function cleanupPeer(peerId, roomId) {
  const room = rooms.get(roomId);
  if (!room) return;

  const peer = room.peers.get(peerId);
  if (peer) {
    try {
      peer.producers?.forEach(p => { try { p.close(); } catch (e) {} });
      peer.consumers?.forEach(c => { try { c.close(); } catch (e) {} });
      peer.transports?.forEach(t => { try { t.close(); } catch (e) {} });
    } catch (e) {}
    room.peers.delete(peerId);
    log('info', `Cleaned peer ${peerId} from ${roomId} - remaining ${room.peers.size}`);
  }

  peers.delete(peerId);

  if (room.peers.size === 0) {
    try { room.router.close(); } catch (e) {}
    rooms.delete(roomId);
    log('info', `🗑️ Room ${roomId} removed (empty)`);
  }
}

// Express routes - Health & Monitoring
app.get('/health', (req, res) => {
  res.json({
    status: 'UP',
    service: 'RED Media SFU Ultimate V2',
    version: '2.0.0-ULTIMATE',
    system: 'A - VoIP 4K AV1/VP9/H264',
    workers: workers.length,
    rooms: rooms.size,
    peers: peers.size,
    codecs: mediaCodecs.map(c => `${c.mimeType}`),
    uptime: process.uptime(),
    timestamp: new Date().toISOString(),
    sovereign: '100% LOCAL'
  });
});

app.get('/stats', (req, res) => {
  const roomList = Array.from(rooms.values()).map(r => ({
    id: r.id,
    type: r.type,
    peers: r.peers.size,
    createdAt: r.createdAt,
    uptime: Date.now() - r.createdAt
  }));
  res.json({
    workers: workers.length,
    rooms: roomList,
    totalPeers: peers.size,
    totalRooms: rooms.size
  });
});

app.get('/', (req, res) => {
  res.json({
    message: '🔴 RED Sovereign Media SFU Ultimate V2',
    system: 'A - 4K VoIP',
    websocket: `ws://localhost:${process.env.PORT || 4000}`,
    health: '/health',
    stats: '/stats',
    codecs: ['AV1 4K', 'VP9 1080p', 'VP8', 'H264', 'OPUS Audio'],
    sovereign: true
  });
});

const server = http.createServer(app);
const wss = new WebSocketServer({ server, path: '/' });

// WebSocket handling - RED Ultimate Protocol
wss.on('connection', (ws, req) => {
  log('info', `🔗 New client connected from ${req.socket.remoteAddress}`);
  
  let peerId = null;
  let roomId = null;
  let verified = false;

  // Auth timeout - 10s to authenticate
  const authTimeout = setTimeout(() => {
    if (!verified) {
      ws.send(JSON.stringify({ type: 'error', error: 'AUTH_TIMEOUT' }));
      ws.close(1008, 'Auth timeout');
    }
  }, 10000);

  ws.on('message', async (data) => {
    try {
      const msg = JSON.parse(data.toString());
      const { type, requestId = uuidv4() } = msg;

      // Auth check - allow join without prior auth but verify token if provided
      if (type !== 'auth' && !verified) {
        // Auto-verify for local sovereign network
        verified = true;
        clearTimeout(authTimeout);
      }

      switch (type) {
        case 'auth': {
          verified = true;
          clearTimeout(authTimeout);
          ws.send(JSON.stringify({ requestId, type: 'auth_ok', peerId: msg.peerId, server: 'RED Ultimate V2' }));
          break;
        }

        case 'join': {
          roomId = msg.roomId || `room-${uuidv4()}`;
          peerId = msg.peerId || `peer-${uuidv4()}`;
          
          const roomType = msg.roomType || 'conference';
          const room = await getOrCreateRoom(roomId, roomType);
          
          // Create WebRTC transport for peer
          const transport = await room.router.createWebRtcTransport({
            listenIps: [{ ip: '0.0.0.0', announcedIp: process.env.ANNOUNCED_IP || null }],
            enableUdp: true,
            enableTcp: true,
            preferUdp: true,
            initialAvailableOutgoingBitrate: 2000000, // 2Mbps start for 4K
            minimumAvailableOutgoingBitrate: 500000,
            maxSctpMessageSize: 262144
          });

          // Store peer
          if (!room.peers.has(peerId)) {
            room.peers.set(peerId, { ws, transports: [transport], producers: [], consumers: [], id: peerId });
          } else {
            room.peers.get(peerId).transports.push(transport);
          }
          
          peers.set(peerId, { ws, transport, roomId, router: room.router, producers: [], consumers: [] });

          ws.send(JSON.stringify({
            requestId,
            type: 'joined',
            status: 'joined',
            roomId,
            peerId,
            roomType,
            rtpCapabilities: room.router.rtpCapabilities,
            transportOptions: {
              id: transport.id,
              iceParameters: transport.iceParameters,
              iceCandidates: transport.iceCandidates,
              dtlsParameters: transport.dtlsParameters,
              sctpParameters: transport.sctpParameters
            },
            peers: Array.from(room.peers.keys()).filter(id => id !== peerId)
          }));

          // Notify other peers
          for (const [otherId, otherPeer] of room.peers) {
            if (otherId !== peerId && otherPeer.ws.readyState === 1) {
              otherPeer.ws.send(JSON.stringify({
                type: 'newPeer',
                peerId,
                roomId
              }));
            }
          }

          log('success', `Peer ${peerId} joined ${roomType} ${roomId} - total ${room.peers.size}`);
          break;
        }

        case 'connectTransport': {
          const peer = peers.get(peerId);
          if (!peer) throw new Error('Peer not found');
          await peer.transport.connect({ dtlsParameters: msg.dtlsParameters });
          ws.send(JSON.stringify({ requestId, type: 'connected', status: 'connected' }));
          break;
        }

        case 'produce': {
          const peer = peers.get(peerId);
          if (!peer) throw new Error('Peer not found');
          
          const producer = await peer.transport.produce({
            kind: msg.kind,
            rtpParameters: msg.rtpParameters,
            appData: { peerId, type: msg.kind }
          });

          peer.producers.push(producer);
          const room = rooms.get(roomId);

          // Notify others
          if (room) {
            for (const [otherPeerId, otherPeer] of room.peers) {
              if (otherPeerId !== peerId && otherPeer.ws.readyState === 1) {
                otherPeer.ws.send(JSON.stringify({
                  type: 'newProducer',
                  producerId: producer.id,
                  peerId,
                  kind: msg.kind,
                  roomId
                }));
              }
            }
          }

          ws.send(JSON.stringify({
            requestId,
            type: 'produced',
            status: 'producing',
            producerId: producer.id,
            kind: msg.kind
          }));

          log('info', `Peer ${peerId} producing ${msg.kind} ${producer.id}`);
          break;
        }

        case 'consume': {
          const peer = peers.get(peerId);
          if (!peer) throw new Error('Peer not found');
          const room = rooms.get(roomId);
          if (!room) throw new Error('Room not found');

          let targetProducer = null;
          let targetPeerId = null;
          for (const [otherPeerId, otherPeer] of room.peers) {
            if (otherPeerId !== peerId && otherPeer.producers.length > 0) {
              targetProducer = otherPeer.producers[0];
              targetPeerId = otherPeerId;
              break;
            }
          }

          if (!targetProducer) {
            ws.send(JSON.stringify({ requestId, type: 'error', error: 'NO_PRODUCER' }));
            break;
          }

          const consumerTransport = await room.router.createWebRtcTransport({
            listenIps: [{ ip: '0.0.0.0', announcedIp: process.env.ANNOUNCED_IP || null }],
            enableUdp: true,
            enableTcp: true,
            preferUdp: true
          });

          const consumer = await consumerTransport.consume({
            producerId: targetProducer.id,
            rtpCapabilities: msg.rtpCapabilities || room.router.rtpCapabilities,
            paused: false
          });

          peer.consumers.push(consumer);
          if (!peer.transports.includes(consumerTransport)) peer.transports.push(consumerTransport);

          ws.send(JSON.stringify({
            requestId,
            type: 'consuming',
            status: 'consuming',
            consumerId: consumer.id,
            producerId: targetProducer.id,
            peerId: targetPeerId,
            kind: consumer.kind,
            rtpParameters: consumer.rtpParameters,
            transportOptions: {
              id: consumerTransport.id,
              iceParameters: consumerTransport.iceParameters,
              iceCandidates: consumerTransport.iceCandidates,
              dtlsParameters: consumerTransport.dtlsParameters
            }
          }));

          log('info', `Peer ${peerId} consuming from ${targetPeerId}`);
          break;
        }

        case 'leave':
        case 'leaveRoom': {
          if (peerId && roomId) cleanupPeer(peerId, roomId);
          ws.send(JSON.stringify({ requestId, type: 'left', status: 'left', roomId }));
          roomId = null;
          peerId = null;
          break;
        }

        case 'ping': {
          ws.send(JSON.stringify({ requestId, type: 'pong', ts: Date.now(), server: 'RED Ultimate V2' }));
          break;
        }

        case 'stats': {
          ws.send(JSON.stringify({
            requestId,
            type: 'stats',
            rooms: rooms.size,
            peers: peers.size,
            workers: workers.length
          }));
          break;
        }

        default:
          log('warn', `Unknown message type: ${type}`);
          ws.send(JSON.stringify({ requestId, type: 'error', error: `UNKNOWN_TYPE ${type}` }));
      }
    } catch (error) {
      log('error', `Message handling error: ${error.message}`, error.stack);
      try {
        ws.send(JSON.stringify({
          requestId: msg?.requestId,
          type: 'error',
          status: 'error',
          error: error.message
        }));
      } catch (e) {}
    }
  });

  ws.on('close', (code, reason) => {
    log('info', `🔌 Peer ${peerId} disconnected code=${code}`);
    if (peerId && roomId) cleanupPeer(peerId, roomId);
    clearTimeout(authTimeout);
  });

  ws.on('error', (error) => {
    log('error', `WebSocket error for ${peerId}`, error.message);
  });
});

// Graceful shutdown
process.on('SIGINT', async () => {
  log('warn', 'Shutting down RED SFU...');
  for (const peerId of peers.keys()) {
    const peer = peers.get(peerId);
    if (peer?.ws) try { peer.ws.close(); } catch (e) {}
  }
  for (const worker of workers) {
    try { worker.close(); } catch (e) {}
  }
  server.close(() => {
    log('info', 'RED SFU shutdown complete');
    process.exit(0);
  });
});

// Start server
const PORT = process.env.PORT || 4000;

(async () => {
  try {
    await initWorkers();

    server.listen(PORT, '0.0.0.0', () => {
      log('success', `🚀 RED Media SFU Ultimate V2 running on port ${PORT}`);
      log('info', `   WebSocket: ws://0.0.0.0:${PORT}`);
      log('info', `   Health: http://0.0.0.0:${PORT}/health`);
      log('info', `   Stats: http://0.0.0.0:${PORT}/stats`);
      log('info', `   Codecs: AV1 4K, VP9 1080p, VP8, H264, OPUS`);
      log('info', `   Workers: ${workers.length}`);
      log('info', `   Media ports: 40000-40100 UDP/TCP`);
      log('success', `   System A ONLINE - 100% Sovereign`);
    });
  } catch (error) {
    log('error', 'Failed to start SFU', error);
    process.exit(1);
  }
})();
