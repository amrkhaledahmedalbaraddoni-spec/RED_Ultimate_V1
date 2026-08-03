# RED Media SFU (mediasoup)
FROM node:22-bookworm

# Install mediasoup build dependencies
RUN apt-get update && apt-get install -y \
    python3 \
    python3-pip \
    build-essential \
    pkg-config \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy package files
COPY package.json ./
RUN npm install

# Copy application
COPY server.js .

EXPOSE 4000
EXPOSE 40000-40100/udp

CMD ["node", "server.js"]
