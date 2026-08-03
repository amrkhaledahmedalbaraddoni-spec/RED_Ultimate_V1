const mediasoup = require('mediasoup');
const http = require('http');
const { WebSocketServer } = require('ws');

const server = http.createServer();
const wss = new WebSocketServer({ server });

// Media codecs supported by the SFU
const mediaCodecs = [
    {
        kind: 'audio',
        mimeType: 'audio/opus',
        clockRate: 48000,
        channels: 2
    },
    {
        kind: 'video',
        mimeType: 'video/VP8',
        clockRate: 90000,
        parameters: {
            'x-google-start-bitrate': 1000
        }
    },
    {
        kind: 'video',
        mimeType: 'video/VP9',
        clockRate: 90000,
        parameters: {
            'profile-id': 2,
            'x-google-start-bitrate': 1000
        }
    },
    {
        kind: 'video',
        mimeType: 'video/H264',
        clockRate: 90000,
        parameters: {
            'packetization-mode': 1,
            'profile-level-id': '4d0032',
            'level-asymmetry-allowed': 1
        }
    }
];

// Global state
const workers = [];
const rooms = new Map(); // roomId -> { router, peers }
const peers = new Map(); // peerId -> { transport, producer, consumer }

let nextWorkerIndex = 0;

// Initialize mediasoup workers
async function initWorkers() {
    const numWorkers = 2; // Adjust based on CPU cores
    console.log(`Starting ${numWorkers} mediasoup workers...`);
    
    for (let i = 0; i < numWorkers; i++) {
        const worker = await mediasoup.createWorker({
            logLevel: 'warn',
            rtcMinPort: 40000,
            rtcMaxPort: 40100
        });
        
        worker.on('died', () => {
            console.error('mediasoup Worker died, exiting...');
            process.exit(1);
        });
        
        workers.push(worker);
    }
    
    console.log(`✅ ${workers.length} workers ready`);
}

// Get next worker (round-robin)
function getNextWorker() {
    const worker = workers[nextWorkerIndex];
    nextWorkerIndex = (nextWorkerIndex + 1) % workers.length;
    return worker;
}

// Create or get room
async function getOrCreateRoom(roomId) {
    if (!rooms.has(roomId)) {
        const worker = getNextWorker();
        const router = await worker.createRouter({ mediaCodecs });
        rooms.set(roomId, { router, peers: new Map() });
        console.log(`📍 Created room: ${roomId}`);
    }
    return rooms.get(roomId);
}

// Handle WebSocket connections
wss.on('connection', (ws) => {
    console.log('🔗 New client connected');
    
    let peerId = null;
    let roomId = null;

    ws.on('message', async (data) => {
        try {
            const msg = JSON.parse(data);
            const { type, requestId } = msg;

            switch (type) {
                case 'join': {
                    roomId = msg.roomId;
                    peerId = msg.peerId;
                    
                    const room = await getOrCreateRoom(roomId);
                    const transport = await room.router.createWebRtcTransport({
                        listenIps: [{ ip: '0.0.0.0', announcedIp: null }],
                        enableUdp: true,
                        enableTcp: true,
                        preferUdp: true
                    });
                    
                    room.peers.set(peerId, { ws, transport, producer: null, consumer: null });
                    peers.set(peerId, { ws, transport, roomId });
                    
                    ws.send(JSON.stringify({
                        requestId,
                        status: 'joined',
                        roomId,
                        peerId,
                        rtpCapabilities: room.router.rtpCapabilities,
                        transportOptions: {
                            id: transport.id,
                            iceParameters: transport.iceParameters,
                            iceCandidates: transport.iceCandidates,
                            dtlsParameters: transport.dtlsParameters
                        }
                    }));
                    
                    console.log(`✅ Peer ${peerId} joined room ${roomId}`);
                    break;
                }

                case 'connectTransport': {
                    const peer = peers.get(peerId);
                    if (!peer) throw new Error('Peer not found');
                    
                    await peer.transport.connect({ dtlsParameters: msg.dtlsParameters });
                    
                    ws.send(JSON.stringify({ requestId, status: 'connected' }));
                    break;
                }

                case 'produce': {
                    const peer = peers.get(peerId);
                    if (!peer) throw new Error('Peer not found');
                    
                    const producer = await peer.transport.produce({
                        kind: msg.kind,
                        rtpParameters: msg.rtpParameters
                    });
                    
                    peer.producer = producer;
                    
                    // Notify other peers in the room
                    const room = rooms.get(roomId);
                    if (room) {
                        for (const [otherPeerId, otherPeer] of room.peers) {
                            if (otherPeerId !== peerId && otherPeer.ws.readyState === 1) {
                                otherPeer.ws.send(JSON.stringify({
                                    type: 'newProducer',
                                    producerId: producer.id,
                                    peerId
                                }));
                            }
                        }
                    }
                    
                    ws.send(JSON.stringify({
                        requestId,
                        status: 'producing',
                        producerId: producer.id
                    }));
                    break;
                }

                case 'consume': {
                    const peer = peers.get(peerId);
                    if (!peer) throw new Error('Peer not found');
                    
                    const room = rooms.get(roomId);
                    if (!room) throw new Error('Room not found');
                    
                    // Find the producer to consume
                    let targetProducer = null;
                    for (const [otherPeerId, otherPeer] of room.peers) {
                        if (otherPeerId !== peerId && otherPeer.producer) {
                            targetProducer = otherPeer.producer;
                            break;
                        }
                    }
                    
                    if (!targetProducer) throw new Error('No producer available');
                    
                    const consumerTransport = await room.router.createWebRtcTransport({
                        listenIps: [{ ip: '0.0.0.0', announcedIp: null }],
                        enableUdp: true,
                        enableTcp: true,
                        preferUdp: true
                    });
                    
                    const consumer = await consumerTransport.consume({
                        producerId: targetProducer.id,
                        rtpCapabilities: room.router.rtpCapabilities
                    });
                    
                    peer.consumer = consumer;
                    
                    ws.send(JSON.stringify({
                        requestId,
                        status: 'consuming',
                        consumerId: consumer.id,
                        producerId: targetProducer.id,
                        kind: consumer.kind,
                        rtpParameters: consumer.rtpParameters,
                        transportOptions: {
                            id: consumerTransport.id,
                            iceParameters: consumerTransport.iceParameters,
                            iceCandidates: consumerTransport.iceCandidates,
                            dtlsParameters: consumerTransport.dtlsParameters
                        }
                    }));
                    break;
                }

                case 'leave': {
                    cleanupPeer(peerId, roomId);
                    ws.send(JSON.stringify({ requestId, status: 'left' }));
                    break;
                }

                default:
                    console.warn(`Unknown message type: ${type}`);
            }
        } catch (error) {
            console.error('❌ Error handling message:', error);
            ws.send(JSON.stringify({
                requestId: msg.requestId,
                status: 'error',
                error: error.message
            }));
        }
    });

    ws.on('close', () => {
        console.log(`🔌 Peer ${peerId} disconnected`);
        if (peerId && roomId) {
            cleanupPeer(peerId, roomId);
        }
    });

    ws.on('error', (error) => {
        console.error('WebSocket error:', error);
    });
});

// Cleanup peer resources
function cleanupPeer(peerId, roomId) {
    const room = rooms.get(roomId);
    if (!room) return;
    
    const peer = room.peers.get(peerId);
    if (peer) {
        if (peer.producer) peer.producer.close();
        if (peer.consumer) peer.consumer.close();
        if (peer.transport) peer.transport.close();
        room.peers.delete(peerId);
    }
    
    peers.delete(peerId);
    
    // Remove room if empty
    if (room.peers.size === 0) {
        room.router.close();
        rooms.delete(roomId);
        console.log(`🗑️ Room ${roomId} removed (empty)`);
    }
}

// Start server
const PORT = process.env.PORT || 4000;

(async () => {
    try {
        await initWorkers();
        
        server.listen(PORT, () => {
            console.log(`🚀 RED Media SFU running on port ${PORT}`);
            console.log(`   WebSocket: ws://localhost:${PORT}`);
            console.log(`   Media ports: 40000-40100`);
        });
    } catch (error) {
        console.error('Failed to start SFU:', error);
        process.exit(1);
    }
})();
