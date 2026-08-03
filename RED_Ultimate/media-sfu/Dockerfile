# RED Media SFU - Ultimate 4K AV1/VP9/H264 - Sovereign Edition V2
FROM node:22-bookworm

LABEL maintainer="RED Sovereign Engineering © 2026"
LABEL version="2.0.0-ULTIMATE"
LABEL system="A - VoIP 4K SFU"

# Install mediasoup build dependencies + curl for healthcheck
RUN apt-get update && apt-get install -y \
    python3 \
    python3-pip \
    build-essential \
    pkg-config \
    curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy package files first for caching
COPY package.json ./

# Install deps
RUN npm install --production --no-audit --no-fund && \
    npm cache clean --force

# Copy application - ultimate server.js
COPY server.js ./

# Create logs dir
RUN mkdir -p /app/logs && chmod 755 /app/logs

EXPOSE 4000
EXPOSE 40000-40100/udp
EXPOSE 40000-40100/tcp

ENV NODE_ENV=production
ENV PORT=4000
ENV MEDIASOUP_WORKER_BIN=/app/node_modules/mediasoup/worker/out/Release/mediasoup-worker

HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=40s \
  CMD node -e "fetch('http://localhost:4000/health').then(r=>{if(!r.ok)process.exit(1)}).catch(()=>process.exit(1))" || exit 1

# Increase file descriptor limits for high concurrency SFU
RUN echo "* soft nofile 65536" >> /etc/security/limits.conf && \
    echo "* hard nofile 65536" >> /etc/security/limits.conf

CMD ["node", "server.js"]
