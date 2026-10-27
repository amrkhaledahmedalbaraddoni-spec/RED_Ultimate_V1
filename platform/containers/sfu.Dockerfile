# RED media SFU (mediasoup) — source: services/media-sfu.
# Build context MUST be the repository root.
FROM node:22-bookworm

LABEL org.opencontainers.image.title="RED Media SFU"
LABEL org.opencontainers.image.description="mediasoup SFU for RED voice/video (WebRTC, no SIP)"
LABEL org.opencontainers.image.version="2.0.0"

RUN apt-get update && apt-get install -y --no-install-recommends \
    python3 python3-pip build-essential pkg-config curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY services/media-sfu/package.json services/media-sfu/package-lock.json ./
RUN npm ci --omit=dev --no-audit --no-fund && npm cache clean --force

COPY services/media-sfu/server.js ./
RUN mkdir -p /app/logs && chmod 755 /app/logs \
    && useradd --system --uid 10002 --create-home red \
    && chown -R red:red /app
USER red

EXPOSE 4000
EXPOSE 40000-40100/udp
EXPOSE 40000-40100/tcp

ENV NODE_ENV=production PORT=4000
ENV MEDIASOUP_WORKER_BIN=/app/node_modules/mediasoup/worker/out/Release/mediasoup-worker

HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=40s \
  CMD node -e "fetch('http://localhost:4000/health').then(r=>{if(!r.ok)process.exit(1)}).catch(()=>process.exit(1))"

CMD ["node", "server.js"]
