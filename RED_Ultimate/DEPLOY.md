# RED Deployment Guide - Sovereign Edition Ultimate V2.0.0

## 1. Prerequisites - Ultimate

- **OS**: Ubuntu 22.04+ / Debian 12+ / Rocky 9+ (tested)
- **Docker**: 24.0+ + Docker Compose v2.20+
- **RAM**: 8GB minimum, 16GB recommended (SFU + DBs)
- **Disk**: 20GB free for images + volumes
- **Network**: Static IP recommended, ports free: 80, 443, 8080, 4000, 40000-40100/udp+tcp, 3478, 5060, 5038, 9000, 9001, 5432, 6379, 27017, 9090, 3001
- **Hardware (optional)**: DINSTAR UC2000-VE-8T GSM Gateway at 192.168.1.100 (simulation mode if absent)
- **Android Build**: Android Studio Ladybug+ + JDK 21 + Android SDK 35

## 2. Clone & Env Setup

```bash
git clone https://github.com/amrkhaledahmedalbaraddoni-spec/RED_Ultimate_V1.git
cd RED_Ultimate_V1

# Env - Ultimate 30 vars
cp .env.example .env
# EDIT SECURELY!
nano .env
# Change: DB_PASSWORD, MONGO_PASSWORD, REDIS_PASSWORD, MINIO_PASSWORD, AMI_PASSWORD, TURN_SECRET, JWT_SECRET (64 chars), GRAFANA_PASSWORD

# Check
cat .env
```

## 3. Infrastructure Init

```bash
cd RED_Ultimate/infrastructure
chmod +x setup-env.sh
./setup-env.sh
# Creates: prometheus.yml, grafana/datasources.yml, mongo-init.js, nginx-certs/README
cd ../..
```

## 4. Server Launch - 13 Services

```bash
cd RED_Ultimate

# Build & Run Ultimate
chmod +x build-and-run.sh
./build-and-run.sh

# Or manual:
docker compose build --parallel
docker compose up -d
docker compose ps
docker compose logs -f --tail=50

# Wait 60s for health
sleep 60
docker compose ps
curl http://localhost:8080/health | jq
curl http://localhost:4000/health | jq
curl http://localhost:80/nginx-health
```

## 5. Systems Check Ultimate

| Check | URL | Expected |
|-------|-----|----------|
| Backend | http://localhost:8080/health | status UP + services all UP |
| Admin Panel | http://localhost:80 | RED Ultimate UI 12 tabs |
| SFU System A | http://localhost:4000/health | workers 2 + AV1 4K |
| MinIO Console | http://localhost:9001 | Login redadmin / .env |
| MinIO API | http://localhost:9000/minio/health/live | 200 |
| Grafana | http://localhost:3001 | admin / .env |
| Prometheus | http://localhost:9090/-/healthy | Prometheus is Healthy |
| TURN | 3478/udp | coturn running |
| Asterisk | `docker exec red-pstn-gateway asterisk -rx 'pjsip show endpoints'` | endpoints online |
| Dinstar Mock | http://localhost:8080/api/admin/dinstar/status | 8 slots JSON |

## 6. Admin Approval Flow

1. Open http://localhost:80
2. Register first user via Android app or API:
   ```bash
   curl -X POST http://localhost:8080/api/auth/register -H "Content-Type: application/json" -d '{"email":"test@red.local","fullName":"Test User","phone":"777123456"}'
   ```
3. In Admin Panel → User Authority → Pending → Approve
4. Login:
   ```bash
   curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d '{"email":"test@red.local","password":"any"}'
   # Returns token red-jwt-... + redId RED-xxxx
   ```

## 7. App Distribution Ultimate

```bash
# Android
cd RED_Ultimate/app
# local.properties:
# sdk.dir=/home/user/Android/Sdk

./gradlew assembleDebug --no-daemon
# APK: app/build/outputs/apk/debug/app-debug.apk

# Upload to MinIO
# Via UI: http://localhost:9001 → bucket red-apks → Upload
# Or mc:
mc alias set local http://localhost:9000 redadmin $MINIO_PASSWORD
mc cp app/build/outputs/apk/debug/app-debug.apk local/red-apks/red-ultimate-v2.apk
mc anonymous set public local/red-apks

# Distribute link: http://YOUR_SERVER_IP:9000/red-apks/red-ultimate-v2.apk
```

## 8. DINSTAR Hardware (System B) Setup

- Connect DINSTAR UC2000-VE-8T to same LAN
- Default IP 192.168.1.100 (check device label)
- Web UI: http://192.168.1.100 (admin / admin)
- SIP Config:
  - SIP Server: YOUR_DOCKER_HOST_IP (e.g., 192.168.1.50)
  - Port: 5060 UDP
  - Authentication: dinstar / red_dinstar_2026
- SIMs: Insert 8 Yemen SIMs (Yemen Mobile / Sabafon)
- Test: `docker exec red-pstn-gateway asterisk -rx 'pjsip show contacts'` → should show dinstar-gateway
- If no hardware: backend auto-falls back to simulated data (70-95% signal random)

## 9. Monitoring & Logs Ultimate

```bash
# Logs
docker compose logs -f backend
docker compose logs -f media-sfu
docker compose logs -f pstn-gateway
docker compose logs -f nginx

# Metrics
curl http://localhost:8080/actuator/prometheus
curl http://localhost:4000/stats

# Grafana: Import RED dashboard JSON (to be created)
# Prometheus: Check targets http://localhost:9090/targets

# DBs
docker exec -it red-db-sql psql -U red_user -d red_sovereign -c "SELECT * FROM users;"
docker exec -it red-db-nosql mongosh -u red_user -p $MONGO_PASSWORD --eval "db.messages.countDocuments()"
docker exec -it red-cache redis-cli -a $REDIS_PASSWORD keys "red:*"
```

## 10. Security Hardening - Production

- Change all passwords in .env (32+ chars, JWT 64 chars)
- Generate JWT: `openssl rand -base64 64`
- Enable TLS in nginx.conf + mount certs in infrastructure/nginx-certs/
- Restrict ports: only 80/443 public, others via VPN or 172.28.0.0/16
- In nginx.conf: uncomment allow/deny for /prometheus/ and /grafana/
- Enable CSRF for non-API if adding web forms
- Backup cron: daily pg_dump + mongodump + MinIO mirror
- Update: `docker compose pull && docker compose up -d --build`

## 11. Shutdown & Cleanup

```bash
# Stop (keep data)
docker compose down

# Stop + remove volumes (DANGEROUS - deletes DBs!)
docker compose down -v

# Prune images
docker system prune -a

# Logs location
ls RED_Ultimate/infrastructure/logs/
```

## 12. Troubleshooting Ultimate

See MASTER_GUIDE.md Section 7.

**Rights Reserved to RED Sovereign © 2026 - Ultimate V2 - 100% Local**
