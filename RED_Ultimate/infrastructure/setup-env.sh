#!/bin/bash
# RED Unified Sovereign - Setup Environment (Merged Sovereign + Ultimate V2)
# 100% Local Sovereign - توحيد كامل بين GitHub والملفات المحلية
set -e

echo "🔴 RED Sovereign Unified: Initializing Sovereignty Environment..."

# Check Docker
if ! command -v docker &> /dev/null; then
    echo "❌ Docker not found. Please install Docker."
    exit 1
fi

# Check .env at repo root and RED_Ultimate
if [ ! -f ".env" ] && [ ! -f "../.env" ] && [ ! -f "../../.env" ]; then
    echo "⚠️ .env not found, creating from .env.example..."
    if [ -f "../../.env.example" ]; then
        cp ../../.env.example ../../.env
        echo "📝 Created .env from .env.example - PLEASE EDIT SECURE PASSWORDS!"
    elif [ -f "../.env.example" ]; then
        cp ../.env.example ../.env
        echo "📝 Created .env from ../.env.example"
    elif [ -f "./.env.example" ]; then
        cp ./.env.example ./.env
        echo "📝 Created .env from ./.env.example"
    else
        echo "⚠️ No .env.example found - creating template requires manual edit"
    fi
fi

# Create necessary directories (unified)
echo "📁 Creating volumes & logs directories..."
mkdir -p infrastructure/grafana
mkdir -p infrastructure/nginx-certs
mkdir -p logs/{backend,sfu,nginx,asterisk}
mkdir -p data/{postgres,mongo,redis,minio,prometheus,grafana}
mkdir -p secrets

# Create Prometheus config if not exists (from Ultimate)
if [ ! -f "infrastructure/prometheus.yml" ] && [ ! -f "prometheus.yml" ]; then
cat > infrastructure/prometheus.yml << 'EOF'
global:
  scrape_interval: 15s
  evaluation_interval: 15s

scrape_configs:
  - job_name: 'red-backend'
    static_configs:
      - targets: ['backend:8080']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 10s
    scrape_timeout: 5s

  - job_name: 'red-sfu'
    static_configs:
      - targets: ['media-sfu:4000']
    metrics_path: '/stats'
    scrape_interval: 10s

  - job_name: 'postgres'
    static_configs:
      - targets: ['db-postgres:5432']

  - job_name: 'redis'
    static_configs:
      - targets: ['cache-redis:6379']

  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']

  - job_name: 'minio'
    static_configs:
      - targets: ['minio:9000']
    metrics_path: '/minio/v2/metrics/cluster'

  - job_name: 'nginx'
    static_configs:
      - targets: ['nginx:80']
EOF
echo "✅ Created infrastructure/prometheus.yml"
fi

# Create Grafana datasource & dashboards (from Ultimate)
if [ ! -f "infrastructure/grafana/datasources.yml" ]; then
mkdir -p infrastructure/grafana
cat > infrastructure/grafana/datasources.yml << 'EOF'
apiVersion: 1
datasources:
  - name: Prometheus
    type: prometheus
    access: proxy
    url: http://prometheus:9090
    isDefault: true
    editable: true
EOF
cat > infrastructure/grafana/dashboards.yml << 'EOF'
apiVersion: 1
providers:
  - name: 'RED Sovereign'
    orgId: 1
    folder: ''
    type: file
    disableDeletion: false
    editable: true
    options:
      path: /etc/grafana/provisioning/dashboards
EOF
echo "✅ Created Grafana provisioning (datasources + dashboards)"
fi

# Create Mongo init if missing
if [ ! -f "infrastructure/mongo-init.js" ]; then
cat > infrastructure/mongo-init.js << 'EOF'
// RED Sovereign Mongo Init - Unified
db = db.getSiblingDB('red_sovereign');

try {
  db.createUser({
    user: "red_user",
    pwd: "RED_Mongo_2026_Ultra_Secure_32!",
    roles: [
      { role: "readWrite", db: "red_sovereign" },
      { role: "dbAdmin", db: "red_sovereign" }
    ]
  });
} catch (e) {
  print("User already exists: " + e);
}

db.createCollection("messages");
db.createCollection("stories");
db.createCollection("groups");
db.createCollection("calls");
db.createCollection("users");

db.messages.createIndex({ conversationId: 1, sequenceNumber: 1 });
db.messages.createIndex({ uuid: 1 }, { unique: true });
db.messages.createIndex({ senderId: 1 });
db.messages.createIndex({ createdAt: 1 });
db.stories.createIndex({ expiresAt: 1 }, { expireAfterSeconds: 0 });
db.stories.createIndex({ userId: 1 });
db.groups.createIndex({ groupId: 1 }, { unique: true });
db.users.createIndex({ email: 1 }, { unique: true });

print("🔴 RED MongoDB Initialized - Sovereign Collections Created Unified");
EOF
echo "✅ Created infrastructure/mongo-init.js"
fi

# Create nginx certs placeholder (from Ultimate)
if [ ! -f "infrastructure/nginx-certs/README.txt" ]; then
mkdir -p infrastructure/nginx-certs
echo "self-signed certs placeholder - use real certs in production" > infrastructure/nginx-certs/README.txt
echo "✅ Created infrastructure/nginx-certs placeholder"
fi

# Generate local identity authority if missing (from Sovereign)
if [ ! -f "secrets/red_identity_private_key.pem" ] && [ -f "../scripts/generate-local-identity-authority.sh" ]; then
  echo "🔑 Generating local identity authority..."
  bash ../scripts/generate-local-identity-authority.sh 2>&1 | head -n 20 || true
elif [ ! -f "./secrets/red_identity_private_key.pem" ] && [ -f "./scripts/generate-local-identity-authority.sh" ]; then
  bash ./scripts/generate-local-identity-authority.sh 2>&1 | head -n 20 || true
fi

# MinIO buckets via mc (if running) - legacy sovereign snippet
if command -v mc &> /dev/null; then
  echo "🪣 Attempting MinIO bucket setup via mc (if MinIO running)..."
  mc alias set local http://localhost:9000 admin "${MINIO_PASSWORD:-red_minio_2026_secure}" 2>&1 | head -n 5 || true
  mc mb local/red-media --ignore-existing 2>&1 | head -n 5 || true
  mc mb local/red-backups --ignore-existing 2>&1 | head -n 5 || true
  mc mb local/red-apks --ignore-existing 2>&1 | head -n 5 || true
  mc mb local/red-avatars --ignore-existing 2>&1 | head -n 5 || true
  echo "   Buckets: red-media, red-backups, red-apks, red-avatars"
else
  echo "🪣 MinIO bucket setup will run via minio-setup container in docker-compose"
  echo "   Buckets: red-media, red-backups, red-apks, red-avatars"
fi

# Initialize PostgreSQL Schemas hint (from Sovereign legacy)
if command -v psql &> /dev/null; then
  echo "🗄️ PostgreSQL schemas handled via Spring Boot Flyway - ensure DB exists"
  # psql -h db -U admin -d postgres -c "CREATE DATABASE red_sovereign;" 2>&1 | head -n 5 || true
fi

# Permissions
chmod +x infrastructure/setup-env.sh 2>/dev/null || true
chmod +x scripts/*.sh 2>/dev/null || true

echo ""
echo "✅ RED Unified Environment Setup Complete!"
echo "=========================================="
echo "🔴 Next steps (توحيد كامل):"
echo "   1. Edit .env with secure passwords (DB_PASSWORD, JWT_SECRET, etc)"
echo "   2. Ensure secrets/ contains red_identity keys (or run scripts/generate-local-identity-authority.sh)"
echo "   3. Run: docker compose up -d --build  (from RED_Ultimate/)"
echo "   4. Check: http://localhost:80 (Admin Master Control)"
echo "   5. API:   http://localhost:8080/health"
echo "   6. SFU:   http://localhost:4000/health  (System A 4K)"
echo "   7. PSTN:  Asterisk AMI 5038 / SIP 5060 (System B)"
echo "   8. MinIO: http://localhost:9001"
echo "   9. Grafana: http://localhost:3001 (metrics)"
echo "   10.Prometheus: http://localhost:9090"
echo ""
echo "📦 Unified branch contains: GitHub main + Sovereign RED (auth/E2EE/social/SFU/PSTN) + Ultimate infra (monitoring) + AQYAL design"
