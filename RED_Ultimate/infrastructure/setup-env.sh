#!/bin/bash
# RED Ultimate Sovereign - Setup Environment Ultimate V2
# 100% Local Sovereign
set -e

echo "🔴 RED Sovereign Ultimate V2: Initializing Sovereignty Environment..."

# Check Docker
if ! command -v docker &> /dev/null; then
    echo "❌ Docker not found. Please install Docker."
    exit 1
fi

# Check .env
if [ ! -f "../.env" ] && [ ! -f "../../.env" ]; then
    echo "⚠️ .env not found, creating from .env.example..."
    if [ -f "../../.env.example" ]; then
        cp ../../.env.example ../../.env
        echo "📝 Created .env from .env.example - PLEASE EDIT SECURE PASSWORDS!"
    elif [ -f "../.env.example" ]; then
        cp ../.env.example ../.env
    else
        echo "⚠️ No .env.example found"
    fi
fi

# Create necessary directories
echo "📁 Creating volumes directories..."
mkdir -p ../infrastructure/grafana
mkdir -p ../infrastructure/nginx-certs
mkdir -p logs/{backend,sfu,nginx,asterisk}
mkdir -p data/{postgres,mongo,redis,minio,prometheus,grafana}

# Create Prometheus config if not exists
if [ ! -f "prometheus.yml" ]; then
cat > prometheus.yml << 'EOF'
global:
  scrape_interval: 15s
  evaluation_interval: 15s

scrape_configs:
  - job_name: 'red-backend'
    static_configs:
      - targets: ['backend:8080']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 10s

  - job_name: 'red-sfu'
    static_configs:
      - targets: ['media-sfu:4000']
    metrics_path: '/metrics'
    scrape_interval: 10s

  - job_name: 'postgres'
    static_configs:
      - targets: ['db-postgres:5432']
    metrics_path: '/metrics'

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
EOF
echo "✅ Created prometheus.yml"
fi

# Create Grafana datasource
if [ ! -f "grafana/datasources.yml" ]; then
mkdir -p grafana
cat > grafana/datasources.yml << 'EOF'
apiVersion: 1
datasources:
  - name: Prometheus
    type: prometheus
    access: proxy
    url: http://prometheus:9090
    isDefault: true
    editable: true
EOF
cat > grafana/dashboards.yml << 'EOF'
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
echo "✅ Created Grafana provisioning"
fi

# Create Mongo init
if [ ! -f "mongo-init.js" ]; then
cat > mongo-init.js << 'EOF'
// RED Sovereign Mongo Init - Ultimate V2
db = db.getSiblingDB('red_sovereign');

db.createUser({
  user: "red_user",
  pwd: "red_mongo_secure_2026",
  roles: [
    { role: "readWrite", db: "red_sovereign" },
    { role: "dbAdmin", db: "red_sovereign" }
  ]
});

db.createCollection("messages");
db.createCollection("stories");
db.createCollection("groups");
db.createCollection("calls");

db.messages.createIndex({ conversationId: 1, sequenceNumber: 1 });
db.messages.createIndex({ uuid: 1 }, { unique: true });
db.messages.createIndex({ senderId: 1 });
db.stories.createIndex({ expiresAt: 1 }, { expireAfterSeconds: 0 });
db.groups.createIndex({ groupId: 1 }, { unique: true });

print("🔴 RED MongoDB Initialized - Sovereign Collections Created");
EOF
echo "✅ Created mongo-init.js"
fi

# MinIO buckets via mc (if running)
echo "🪣 MinIO bucket setup will run via minio-setup container in docker-compose"
echo "   Buckets: red-media, red-backups, red-apks, red-avatars"

# Permissions
chmod +x setup-env.sh 2>/dev/null || true

echo ""
echo "✅ RED Ultimate Environment Setup Complete!"
echo "=========================================="
echo "🔴 Next steps:"
echo "   1. Edit .env with secure passwords"
echo "   2. Run: docker-compose up -d --build"
echo "   3. Check: http://localhost:80 (Admin)"
echo "   4. API: http://localhost:8080/health"
echo "   5. SFU: http://localhost:4000/health"
echo "   6. MinIO: http://localhost:9001"
echo "   7. Grafana: http://localhost:3001"
echo "   8. Prometheus: http://localhost:9090"
echo "=========================================="
