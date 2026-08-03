# 🔴 دليل تشغيل منظومة RED السيادية - Ultimate V2.0.0

أهنئك على امتلاك أقوى نظام تواصل سيادي محلي 100%. اتبع التعليمات التالية بدقة لـ Ultimate V2:

## 1. البنية التحتية - 13 خدمة Docker Ultimate

انتقل إلى المجلد الجذري:

```bash
cd RED_Ultimate
# تأكد من .env
cp ../.env.example ../.env
nano ../.env  # ضع كلمات سر قوية!

# البنية التحتية
cd infrastructure
chmod +x setup-env.sh
./setup-env.sh
cd ..

# التشغيل
chmod +x build-and-run.sh
./build-and-run.sh
# أو يدوياً
docker compose up -d --build
docker compose ps
docker compose logs -f backend
```

### الخدمات المكتملة:
- `backend` 8080 HEALTH
- `media-sfu` 4000 + 40000-40100 UDP/TCP - System A
- `coturn` 3478 STUN/TURN
- `pstn-gateway` 5060 SIP + 5038 AMI - System B
- `db-postgres` 5432 + health
- `db-mongo` 27017 + health
- `cache-redis` 6379 + health
- `minio` 9000/9001 + health
- `minio-setup` bucket init
- `nginx` 80/443 - Ultimate Gateway
- `admin-panel` 3000 - React Master
- `prometheus` 9090 Metrics
- `grafana` 3001

## 2. ضبط جهاز DINSTAR UC2000-VE-8T (System B)

- **IP الافتراضي**: 192.168.1.100 (عدّله في `.env` DINSTAR_IP)
- **SIP Trunk**: وجه Dinstar إلى IP السيرفر (مثلاً 192.168.1.50) منفذ 5060
  - في Dinstar UI: SIP Server → 192.168.1.50:5060
  - Username: dinstar / Password: من .env AMI_PASSWORD
- **الشرائح**: أدخل 8 شرائح يمن موبايل/سبأفون/يو/واي تيليكوم
- **الاختبار**: http://localhost:8080/api/admin/dinstar/status → يجب أن يظهر 8 slots
- **إذا لا يوجد جهاز**: النظام يعمل بمحاكاة (Simulated) تلقائياً - يظهر إشارة عشوائية 70-95%

## 3. بناء تطبيق الأندرويد - RED Ultimate V2

- افتح `RED_Ultimate/app` بـ **Android Studio Ladybug+**
- `local.properties`: ضع `sdk.dir=/path/to/Android/Sdk`
- انتظر Gradle Sync (يحتاج إنترنت أول مرة لتحميل libs)
- إصلاحات Ultimate V2 التي تمت:
  - YemeniOperatorDetector تم إنشاؤه
  - RedMasterModule تم إصلاح addMigrations() → fallbackToDestructiveMigration()
  - MasterDeliveryEngine dispatchMessage تمت إضافته
  - Duplicate ChatDetailScreen تم توحيده
  - dependencies.gradle.kts تمت إضافة webrtc, mediasoup, room compiler, hilt, compressor
  - RedMediaTransporter تم إصلاح File(uri.path!!) → uriToFile safe via ContentResolver
- اضغط **Build → Build APK Debug**
- APK: `app/build/outputs/apk/debug/app-debug.apk`
- ارفع إلى MinIO: http://localhost:9001 → bucket red-apks → Upload
- وزع الرابط المحلي: http://localhost:9000/red-apks/app-debug.apk

## 4. الإدارة والتحكم - Master Panel Ultimate

- افتح http://localhost:80
- التبويبات 12 Ultimate:
  1. Dashboard Overview - إحصائيات حية + ECharts + WebSocket
  2. Master Command Center - 5 تبويب فرعي (Overview, Authority, Messaging, Dinstar PSTN, Media SFU, Security, Infra)
  3. User Authority - approve/reject/ban + search
  4. Approval Queue - pending users + batch actions
  5. DINSTAR UC2000 - 8 slots + signal + operator + reboot + restart slot
  6. GSM Monitor - retro monitor
  7. Dumin Advanced - telemetry hardware
  8. Live Monitor - MasterOverview Thunderbolt + GSM signal + DB
  9. Live & Conference - VoIP calls active + SFU metrics
  10. Messaging Center - Guaranteed delivery stats
  11. Infrastructure - PostgreSQL, Mongo, Redis, MinIO health
  12. Diagnostics - 4 systems: VoIP 4K SFU, PSTN Dumin Gateway, Messaging Sync, Storage MinIO S3
- أول مستخدم يسجل: يظهر PENDING → اضغط Approve → يمنح RED ID + GSM number
- Kill Switch: في User Authority → Ban → يرسل WIPE via Redis+WS

## 5. المراقبة والتشخيص

- Prometheus: http://localhost:9090 → targets up?
- Grafana: http://localhost:3001 (admin / .env) → datasource Prometheus → dashboard RED
- Backend /health: http://localhost:8080/health → يجب UP + services UP
- SFU /health: http://localhost:4000/health → workers 2, rooms 0, peers 0
- SFU /stats: http://localhost:4000/stats
- Logs: `docker compose logs -f backend|media-sfu|admin-panel|pstn-gateway|nginx`
- Audit: `cat RED_Ultimate/audit_check.py` + `python3 audit_check.py`

## 6. الأمان والصيانة Ultimate

- غيّر كل كلمات السر في .env قبل الإنتاج!
- JWT_SECRET: 64 حرف عشوائي → `openssl rand -base64 64`
- لا تفتح منافذ 5432, 27017, 6379 للإنترنت - اتركها على 172.28.0.0/16 فقط
- النسخ الاحتياطي:
  - Postgres: `docker exec red-db-sql pg_dump -U red_user red_sovereign > backup.sql`
  - Mongo: `docker exec red-db-nosql mongodump --username red_user --password *** --db red_sovereign`
  - MinIO: `mc mirror local/red-media ./backup-media/`
- التحديث: `docker compose pull && docker compose up -d --build`
- الإيقاف: `docker compose down` (+ `-v` لحذف البيانات - خطر!)

## 7. استكشاف الأخطاء Ultimate

| المشكلة | الحل |
|---------|------|
| Backend unhealthy | `docker compose logs backend` → check DB passwords, Mongo auth, Redis password |
| SFU fails to start | تأكد من منافذ 40000-40100 متاحة، `sysctl -w net.core.rmem_max=26214400` |
| Admin shows offline | `docker compose logs admin-panel` + check nginx `docker compose logs nginx` |
| Dinstar slots offline | Ping 192.168.1.100, check .env DINSTAR_IP, النظام يعمل simulation إذا فشل |
| App build fails | Invalidate Caches in Android Studio, check local.properties, Room KSP |
| 401 Login PENDING | وافق على المستخدم في Admin → User Authority → Approve |
| WS disconnect | Check nginx ws timeout 86400s, backend WebSocketConfig |

**بنيت بواسطة كبير مهندسي RED Sovereign Engineering © 2026 - Ultimate V2 - 100% Local Sovereign**
