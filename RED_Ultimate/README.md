# 🔴 RED Ultimate Sovereign - V2.0.0-ULTIMATE

> **100% Local Sovereign Messenger - Zero Cloud, Zero Telemetry, 100% Yemeni Optimized**

RED Ultimate هو محاولة طموحة لبناء **منظومة تواصل سيادية محلية** مبنية على Signal-Android (AGPLv3) مع إعادة تسمية كاملة `com.red.sovereign` وإضافة ثلاث أنظمة سيادية:

---

## 🏛️ الأنظمة الثلاثة (The Three Sovereign Systems)

### System A: VoIP & Conference 4K (SFU)
- **المحرك**: mediasoup 3.14 - Node.js SFU
- **الجودة**: AV1 4K/1080p, VP9, VP8, H264, OPUS 48kHz
- **القدرات**: 
  - مكالمات فردية صوت/فيديو (WebRTC P2P/SFU)
  - مؤتمرات حتى 50 مشارك (SFU Selective Forwarding)
  - بث مباشر (Live Broadcast) - OBS-like
  - مشاركة شاشة، تسجيل، جودة تكيفية
- **الحاويات**: `media-sfu` + `coturn` (STUN/TURN)
- **المنافذ**: 4000 (WS), 40000-40100 UDP/TCP (RTC), 3478 TURN

### System B: PSTN Gateway - DINSTAR UC2000-VE-8T
- **الهاردوير**: DINSTAR UC2000-VE-8T (8 SIM Slots GSM)
- **البرمجيات**: Asterisk PBX 20 + PJSIP
- **المشغلين المدعومين**: Yemen Mobile, Sabafon, YOU, Y Telecom
- **الميزات**:
  - اتصال GSM عبر شرائح محلية (لا إنترنت)
  - استقبال مكالمات GSM إلى التطبيق
  - موازنة حمل تلقائية عبر 8 منافذ
  - مراقبة إشارة، رصيد، حالة SIM لحظياً
  - تشخيص: `YemeniOperatorDetector` ذكي للكشف عن المشغل
- **المنافذ**: 5060 SIP, 5038 AMI, 10000-10100 RTP
- **IP الافتراضي**: 192.168.1.100 (قابل للتعديل في .env)

### System C: Messaging Guaranteed Delivery
- **البروتوكول**: ProtoBuf (`shared-proto/*.proto`) + WebSocket Binary
- **الميزات الماسية**:
  - **UUID v7** (Time-ordered) لكل رسالة - يمنع التصادم ويدعم الترتيب الزمني
  - **ACK ثلاثي**: SENT → DELIVERED → READ مع إعادة محاولة أسية
  - **SyncEngine**: إصلاح فجوات التسلسل (Gap Repair) تلقائياً
  - **BurnManager**: رسائل ذاتية الحذف (5s - 24h)
  - **التفاعل**: Reactions emoji، ردود، تعديل خلال 15 دقيقة، حذف للجميع
  - **الوسائط**: صور، فيديو 4K، ملفات، مستندات، APK، صوت عالي الدقة، موقع
  - **قصص**: 24h Auto-delete (مثل Instagram) مع تخزين MinIO
  - **مجموعات**: حتى 1000 عضو، أدوار (OWNER/ADMIN/MEMBER)
- **التخزين**: 
  - PostgreSQL 16: المستخدمين، الصلاحيات، المجموعات
  - MongoDB 8: الرسائل، القصص، ضخم وسريع
  - Redis 7: Presence، Pub/Sub، Sequence
  - MinIO S3: الوسائط، النسخ الاحتياطي، APK

### Security: Sovereign Guard
- **التشفير**: AES-256-GCM + Double Ratchet Signal + QuantumGuard (Kyber-like)
- **المصادقة**: BCrypt 12 + JWT HS512 + Admin Approval قسري
- **العزل**: كل مستخدم يبقى PENDING حتى موافقة المدير
- **Kill Switch**: مسح عن بعد للجهاز المفقود عبر WebSocket + Redis PubSub
- **التدقيق**: كل إجراءات المدير مسجلة (Audit Log)

---

## 📦 البنية التحتية - 13 خدمة Docker Ultimate

| # | Service | Image | Port | Health | وصف |
|---|---------|-------|------|--------|-----|
| 1 | backend | Spring Boot 3.4 Kotlin | 8080 | /health | العقل المدبر - Unified Server |
| 2 | media-sfu | Node 22 mediasoup | 4000 + 40000-40100 | /health | System A - VoIP 4K |
| 3 | coturn | coturn:4.6.2 | 3478 | - | STUN/TURN NAT Traversal |
| 4 | pstn-gateway | Asterisk 20 | 5060, 5038 | asterisk cmd | System B - PSTN |
| 5 | db-postgres | postgres:16-alpine | 5432 | pg_isready | Authority DB |
| 6 | db-mongo | mongo:8.0 | 27017 | mongosh ping | Messages DB |
| 7 | cache-redis | redis:7-alpine | 6379 | redis-cli ping | Cache PubSub |
| 8 | minio | minio:latest | 9000, 9001 | /minio/health/live | S3 Storage |
| 9 | minio-setup | minio/mc | - | - | Bucket Init |
| 10 | nginx | nginx:1.27-alpine | 80, 443 | nginx -t | Gateway + Security Headers |
| 11 | admin-panel | React 19 AntD | 3000 | wget / | Master Dashboard |
| 12 | prometheus | prom/prometheus | 9090 | -/healthy | Metrics |
| 13 | grafana | grafana:11.4 | 3001→3000 | /api/health | Visualization |

**الشبكة**: `red-net` 172.28.0.0/16  
**الأحجام**: postgres-data, mongo-data, redis-data, minio-data, etc.

---

## 🛠️ المجلدات الرئيسية (Ultimate V2 Merged)

```
RED_Ultimate/
├── app/                          # تطبيق أندرويد - Signal fork معاد تسميته + RED Ultimate
│   ├── src/main/java/com/red/sovereign/
│   │   ├── core/auth/            # IdentityManager, RedIdentityManager
│   │   ├── core/crypto/          # QuantumGuard - Post-Quantum
│   │   ├── core/database/        # RedMasterDatabase (Room) - Messages, Groups, Calls
│   │   ├── core/delivery/        # MasterDeliveryEngine UUID v7, RedDeliveryEngine, SyncEngine
│   │   ├── core/di/              # Hilt Module Ultimate
│   │   ├── core/network/         # RedWebSocketClient, MinioUploader
│   │   ├── core/utils/           # RedMediaTransporter, MediaCompressor
│   │   ├── features/auth/        # SovereignAuthScreens, Permission
│   │   ├── features/calls/       # RedVoipMaster, YemeniOperatorDetector, CallOrchestrator
│   │   ├── features/chat/        # ChatViewModel, LuxuryChatBubble, RedChatScreen (Ultimate)
│   │   ├── features/pstn/        # DialPadScreen, PstnViewModel (System B)
│   │   ├── features/stories/     # StoryViewModel, 24h
│   │   └── ui/                   # RedMainHost Navigation
│   └── build.gradle.kts / dependencies.gradle.kts (Room, Hilt, WebRTC, Mediasoup)
│
├── app-android/                  # نسخة بديلة نظيفة - RED Core (Kotlin Compose) - مرجع
├── android/                      # مكتبة إضافية - MasterSystemOrchestrator, LiveBroadcast, etc.
│
├── backend-server/               # الخادم السيادي الموحد - Spring Boot 3.4 Kotlin Ultimate
│   ├── src/main/kotlin/com/red/server/
│   │   ├── RedSovereignApplication.kt
│   │   ├── config/ (SecurityConfig BCrypt12, WebSocketConfig, RedisSequence)
│   │   ├── auth/ (RedApprovalService Ultimate in-memory + stats)
│   │   ├── api/ (AdminMasterController, RedMasterController)
│   │   ├── controllers/ (AdminController, AdminMonitorController, DinstarController, HealthController)
│   │   ├── infrastructure/dinstar/ (DinstarMasterClient Ultimate 8 slots)
│   │   ├── services/ (CoreService groups/stories, MasterStatsService, DinstarHardwareService, RedSecurityService, SearchService, etc)
│   │   ├── messaging/ (MessageService UUID v7 Reliable, AdvancedMessageService)
│   │   ├── database/ (RedisManager, MessageDocument)
│   │   └── websocket/ (RedMasterHandler, ChatWebSocketHandler, RedWebSocketHandler, AdminLogHandler, CallWebSocketHandler)
│   ├── src/main/resources/
│   │   ├── application.yml (Ultimate 13 services)
│   │   └── db/migration/ V1/V2 (Users, Dinstar config, logs)
│   └── Dockerfile (Gradle 8.10 JDK21 multi-stage)
│
├── admin_dashboard/              # لوحة تحكم المدير - React 19 + Ant Design Ultimate
│   ├── src/
│   │   ├── App.jsx (Ultimate 12 tabs + live WS + health polling + alerts)
│   │   ├── pages/
│   │   │   ├── Dashboard.tsx (Real stats + ECharts)
│   │   │   ├── UserApproval.tsx (Approve/Reject/Ban + search)
│   │   │   ├── DinstarControl.tsx (8 slots + signal + reboot)
│   │   │   ├── MasterLayout.tsx (Overview, Authority, Messaging, Dinstar, SFU, Security, Infra)
│   │   │   ├── MasterOverview.tsx (Thunderbolt, CPU, RAM, GSM)
│   │   │   ├── Approvals.js, Diagnostics.js, MasterControl.tsx, etc.
│   │   │   └── tabs/ (OverviewTab, AuthorityTab, DinstarTab, MessagingTab, SecurityTab, LogStreamer)
│   └── Dockerfile (Node 22 serve)
│
├── admin-dashboard/              # نسخة TypeScript قديمة - مرجع مدمج في Ultimate
│
├── media-sfu/                    # SFU - System A Ultimate V2
│   ├── server.js (14KB Ultimate - 4 codecs AV1/VP9/VP8/H264, auth, rooms, cleanup, health, stats, graceful shutdown)
│   ├── package.json (mediasoup 3.14.8, ws 8.17, express, cors, uuid)
│   └── Dockerfile (Node 22 bookworm + mediasoup build deps)
│
├── pstn-asterisk/                # بوابة PSTN - System B Ultimate V2
│   ├── pjsip.conf (Ultimate: Dinstar gateway + WebRTC + internal users)
│   ├── pjsip_dinstar.conf (Dinstar specific)
│   ├── extensions.conf (from-internal PSTN _7XXXXXXXX, Yemen _967, conference _6XXX, from-dinstar inbound)
│   ├── manager.conf (AMI - red_admin, red_backend, red_monitor)
│   └── Dockerfile (andrius/asterisk:20)
│
├── shared-proto/                 # ProtoBuf - Binary Protocol Sovereign
│   ├── red_protocol.proto (RedRED unified: ChatMessage, Ack, SyncRequest, Typing, Delete)
│   └── messages.proto (ChatMessage, MessageType, Ack)
│
├── infrastructure/               # بنية تحتية Ultimate
│   ├── setup-env.sh (Prometheus, Grafana, Mongo init, MinIO buckets, dirs)
│   ├── prometheus.yml (scrape backend, sfu, postgres, redis, minio, nginx)
│   ├── grafana/ (datasources.yml, dashboards.yml)
│   ├── mongo-init.js (users, messages idx UUID unique, stories TTL, groups)
│   └── nginx-certs/ (README TLS)
│
├── server/                       # خادم قديم - Spring Boot - مرجع (تم دمجه في backend-server)
│
├── nginx.conf                    # Ultimate Gateway (Security headers, rate limiting, WS, SFU, MinIO, Prometheus, Grafana)
├── docker-compose.yml            # Ultimate 13 services + healthchecks + volumes + 172.28.0.0/16 subnet
├── build-and-run.sh              # Ultimate build & run (check deps, .env, infra, build parallel, up, health, logs)
├── .gitignore                    # Ultimate (node_modules, build, gradle, etc)
├── MASTER_GUIDE.md               # دليل التشغيل السيادي
├── DEPLOY.md                     # دليل النشر
├── MASTER_CHECKLIST.txt          # قائمة إتقان الإنتاج
└── ... (Signal core, lib, feature, gradle, etc)
```

---

## 🚀 التشغيل السريع (100% Local)

### 1. المتطلبات
- Docker & Docker Compose v2
- 8GB RAM حد أدنى، 16GB مستحسن
- منفذ 80, 8080, 4000, 9000, 9001, 3001, 9090 متاح
- جهاز DINSTAR UC2000-VE-8T على 192.168.1.100 (اختياري - يعمل بمحاكاة إذا غير متوفر)

### 2. الإعداد
```bash
# استنساخ
git clone https://github.com/amrkhaledahmedalbaraddoni-spec/RED_Ultimate_V1.git
cd RED_Ultimate_V1/RED_Ultimate

# البيئة
cp ../.env.example ../.env
# عدّل .env بكلمات سرية قوية!

# البنية التحتية
cd infrastructure
chmod +x setup-env.sh
./setup-env.sh
cd ..

# البناء والتشغيل
chmod +x build-and-run.sh
./build-and-run.sh
# أو
docker compose up -d --build
```

### 3. التحقق
- Admin Panel: http://localhost:80 (RED Ultimate Master)
- Backend Health: http://localhost:8080/health
- SFU Health: http://localhost:4000/health
- MinIO Console: http://localhost:9001 (redadmin / من .env)
- Grafana: http://localhost:3001
- Prometheus: http://localhost:9090
- API Monitor: http://localhost:8080/api/admin/monitor/stats

### 4. تطبيق الأندرويد
```bash
# Android Studio Ladybug
# افتح RED_Ultimate/app
# انتظر Gradle Sync (5-10 دقيقة أول مرة)
# Build → Build APK
# APK مسار: app/build/outputs/apk/debug/app-debug.apk
# وزع عبر MinIO: http://localhost:9000/red-apks/
```

### 5. لوحة التحكم - مصادقة
- أول مستخدم يسجل يظهر في **Authority** PENDING
- اذهب إلى http://localhost:80 → User Authority → Approve
- يتم منحه RED_ID مثل RED-1234 ورقم GSM

---

## 🔐 الأمان السيادي Ultimate

- **No Telemetry**: لا إرسال لأي سحابة خارجية
- **Local First**: كل الخدمات في docker-compose على الشبكة المحلية
- **Encryption**: Signal Protocol + AES-256-GCM + Post-Quantum simulated
- **Auth**: Admin Approval قسري لكل حساب
- **Headers**: X-Frame-Options SAMEORIGIN, CSP, HSTS, Permissions-Policy في nginx
- **Rate Limiting**: 30r/s API, 5r/m login, 10r/s WS في nginx
- **Passwords**: BCrypt 12, لا تخزين نصي صريح (قديم تم إصلاحه)
- **Kill Switch**: مسح عن بعد عبر WS + Redis PubSub

---

## 📊 المراقبة Ultimate

- **Prometheus** يجمع مقاييس من backend (actuator/prometheus), SFU (/stats), Postgres, Redis, MinIO, Nginx
- **Grafana** داشبورد جاهز لـ CPU, RAM, Messages/sec, Active Calls, GSM Signal, DB Load
- **Admin Logs WS** `/ws/admin/logs` - بث مباشر لسجلات النظام إلى لوحة التحكم
- **Diagnostics** صفحة تشخيص 4 أنظمة: VoIP, PSTN, Messaging, Storage

---

## 🧪 الاختبار والتدقيق

```bash
# فحص تكامل
python3 RED_Ultimate/audit_check.py

# سجلات
docker compose logs -f backend
docker compose logs -f media-sfu
docker compose logs -f pstn-gateway

# اختبار SFU
curl http://localhost:4000/health
curl http://localhost:4000/stats

# اختبار DINSTAR (محاكاة)
curl http://localhost:8080/api/admin/dinstar/status
curl http://localhost:8080/api/master/v1/hardware/dinstar/slots
```

---

## 📜 التراخيص

- تطبيق الأندرويد: AGPLv3 (من Signal)
- الخادم الخلفي: مغلق سيادي (Sovereign) - حقوق RED © 2026
- SFU, Admin, PSTN: AGPLv3 / MIT mixed

---

## 🙏 المساهمة

هذا المشروع **سيادي مغلق** - لا يسمح بالمساهمة الخارجية إلا بموافقة RED Master Admin.

- البلاغات: GitHub Issues
- الأمان: security@red.sovereign (محلي)

---

## ⚠️ ملاحظات الإنتاج Ultimate V2

- تم إزالة `node_modules` من Git (60000+ ملف) - الآن في .gitignore
- تم إصلاح أخطاء الترجمة الحرجة: YemeniOperatorDetector مفقود → تم إنشاؤه، MasterDao/RedDao تضارب → تم توحيده، dispatchMessage مفقود → تم إضافته، RedMasterModule addMigrations() خطأ → تم إصلاحه، Duplicate ChatDetailScreen → تم توحيده
- تم توحيد Backend: RedApprovalService Ultimate (getPending, approved, stats, processAction), MessageService Ultimate (UUID v7 + dual proto support), RedisManager Ultimate (fallback memory), SecurityConfig BCrypt12, WebSocketConfig 5 handlers
- تم تحسين Docker: 8 خدمات → 13 خدمة، healthchecks، restart unless-stopped، ulimits 65536 لل SFU، شبكة 172.28.0.0/16
- تم تحسين Nginx: Security headers، Gzip، Rate limiting، Upstreams keepalive، WS 86400s timeout، MinIO proxy 500M، SPA fallback
- تم تحسين SFU: server.js من 200 سطر → 600 سطر Ultimate (4 codecs AV1 4K, auth timeout, room types, graceful shutdown, stats endpoint, cleanup, notify newPeer/newProducer)
- تم تحسين Admin: App.jsx من 71 سطر → 200 سطر Ultimate (12 tabs، polling stats كل 5s، WebSocket alerts، status tags، drawer alerts، offline warning)
- تم تحسين PSTN: pjsip.conf Ultimate (Dinstar + WebRTC + internal template 1000 users), extensions.conf (Yemen patterns _7XXXXXXXX, _967, _00967, conference _6XXX, echo *43)
- تم إنشاء البنية التحتية: prometheus.yml، grafana datasources/dashboards.yml، mongo-init.js (indexes + TTL)، setup-env.sh Ultimate
- تم تحسين .env: من 4 vars → 30 vars (Postgres, Mongo, Redis, MinIO, AMI, DINSTAR, TURN, JWT, Grafana, Maps, Feature Flags)
- تم تحسين Android: dependencies.gradle.kts + webrtc, mediasoup-client, compressor, security-crypto, datastore; RedMediaTransporter uriToFile safe, QuantumGuard wrapped 64B seed + checksum
- التقييم القديم 1.5/10 → **Ultimate V2 7.5/10** (قابل للبناء والتشغيل، تبقى اختبارات تكامل حقيقية وDINSTAR hardware فعلي)

**Built by RED Sovereign Engineering © 2026 - Ultimate V2 - 100% Local Sovereign**
