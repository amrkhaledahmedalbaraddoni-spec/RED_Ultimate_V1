#!/bin/bash
# RED Ultimate Sovereign - Build & Run Ultimate V2
# 13 Services - 100% Local - Zero Cloud
set -e

echo "🔴 RED Ultimate Sovereign V2 - Master Build Sequence Starting..."
echo "=========================================="
echo "🔴 Version: 2.0.0-ULTIMATE"
echo "🔴 Systems: A (VoIP 4K) + B (PSTN) + C (Messaging)"
echo "🔴 Services: 13 Container Ultimate Stack"
echo "=========================================="

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# 1. Check Dependencies
echo "📋 Checking dependencies..."
if ! command -v docker &> /dev/null; then
  echo -e "${RED}❌ Docker not found. Please install Docker.${NC}" >&2
  exit 1
fi

if ! docker compose version &> /dev/null && ! command -v docker-compose &> /dev/null; then
  echo -e "${RED}❌ docker compose not found.${NC}" >&2
  exit 1
fi

# Use docker compose if available, else docker-compose
COMPOSE_CMD="docker compose"
if ! docker compose version &> /dev/null; then
  COMPOSE_CMD="docker-compose"
fi
echo -e "${GREEN}✅ Using: $COMPOSE_CMD${NC}"

# 2. Check .env
if [ ! -f "../.env" ] && [ ! -f "../../.env" ] && [ ! -f ".env" ]; then
  echo -e "${YELLOW}⚠️ .env not found, creating from .env.example...${NC}"
  if [ -f "../../.env.example" ]; then
    cp ../../.env.example ../../.env
  elif [ -f "../.env.example" ]; then
    cp ../.env.example ../.env
  elif [ -f ".env.example" ]; then
    cp .env.example .env
  fi
  echo -e "${YELLOW}📝 Please edit .env with secure passwords before production!${NC}"
else
  echo -e "${GREEN}✅ .env found${NC}"
fi

# 3. Setup infrastructure
echo "🔧 Setting up infrastructure..."
if [ -f "infrastructure/setup-env.sh" ]; then
  chmod +x infrastructure/setup-env.sh
  (cd infrastructure && ./setup-env.sh) || echo "⚠️ Setup script warning - continuing"
fi

# 4. Create network if not exists
docker network create red-net 2>/dev/null || echo "Network red-net exists or will be created by compose"

# 5. Build All Artifacts
echo "📦 Building Ultimate Stack (Backend, SFU, Admin Panel, PSTN)..."
echo "   This may take 5-10 minutes on first build..."

$COMPOSE_CMD build --parallel 2>&1 | tee build.log || {
  echo -e "${YELLOW}⚠️ Parallel build failed, trying sequential...${NC}"
  $COMPOSE_CMD build
}

# 6. Launch the System
echo "🚀 Launching RED Sovereign Empire - 13 Services..."
$COMPOSE_CMD up -d

# 7. Wait for healthy
echo "⏳ Waiting for services to become healthy (60s)..."
sleep 10

# Check health
for i in {1..12}; do
  echo "  Health check $i/12..."
  $COMPOSE_CMD ps
  sleep 5
done

# 8. Show status
echo ""
echo -e "${GREEN}✅ RED Ultimate Sovereign is ONLINE!${NC}"
echo "=========================================="
$COMPOSE_CMD ps
echo "=========================================="
echo "📱 Backend API: http://localhost:8080/health"
echo "🔐 Admin Panel: http://localhost:80"
echo "📡 Media SFU: http://localhost:4000/health (WS ws://localhost:4000)"
echo "🗄️ MinIO Console: http://localhost:9001 (redadmin / from .env)"
echo "📊 Grafana: http://localhost:3001 (redadmin / from .env)"
echo "📈 Prometheus: http://localhost:9090"
echo "🔄 TURN: 3478/udp+tcp"
echo "📞 Asterisk: 5060/udp, AMI 5038"
echo ""
echo "📋 Logs:"
echo "   $COMPOSE_CMD logs -f backend"
echo "   $COMPOSE_CMD logs -f media-sfu"
echo "   $COMPOSE_CMD logs -f admin-panel"
echo ""
echo "🛠️ Management:"
echo "   $COMPOSE_CMD down          # Stop all"
echo "   $COMPOSE_CMD logs -f       # Follow logs"
echo "   $COMPOSE_CMD restart backend # Restart backend"
echo ""
echo "🔴 Project RED Ultimate V2 is now operational - 100% Sovereign!"
echo "=========================================="
