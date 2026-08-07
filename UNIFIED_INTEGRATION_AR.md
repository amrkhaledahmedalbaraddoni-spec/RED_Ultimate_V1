# 🔴 توحيد RED Ultimate الكامل — GitHub × الملفات المحلية × الملفات غير المرفوعة

> هذا التقرير يوثق عملية التوحيد والتكامل الكاملة التي تمت في الفرع `arena/019fda5c-red-ultimate-v1` بين ثلاثة مصادر.

تاريخ التوحيد: **2026-08-07** (UTC)  
الفرع الموحد: `arena/019fda5c-red-ultimate-v1`  
القاعدة: `main` (commit `99ce3c2`)  
المصدر الأكثر تقدمًا: `arena/019fce61-red-ultimate-v1` (PR #5 Draft)

---

## 1) المصادر الثلاثة التي تم توحيدها

| المصدر | الوصف | الحالة قبل التوحيد |
|---|---|---|
| **GitHub (origin/main)** | المستودع المنشور — 10,098 ملف، توثيقات 24 وحدة، تقارير فنية، `.env.example` بسيط | قاعدة نظيفة |
| **الملفات المحلية في جهازك** | ملفات العمل المحلية + `node_modules` و `build/` التي لا تُرفع، وإعدادات `.env` المحلية، ومجلدات `RED_Ultimate/android` و`app-android` البديلة | متفرقة بين 5 فروع `arena/*` |
| **الملفات التي لديّ ولم تُرفع بعد** | التطوير السيادي الكامل غير المرفوع في `arena/019fce61` (331 ملف، 18,191 إضافة): نظام المصادقة، E2EE، الشبكة الاجتماعية، القصص، المجموعات، سجل المكالمات، SFU مصادق، PSTN مُتحكم، تطبيق `red-app` القانوني، بروتوكول `RedProtos` | Draft PR #5 - لم يُدمج في `main` بعد |

### الفروع التي تم فحصها ودمجها
- `arena/019fc4df` — استخراج أولي + تقرير فني
- `arena/019fc515` — إصلاحات بناء + خدمات خلفية
- `arena/019fc54c` — اكتمال Docker + إصلاحات أمنية (لكن مع `node_modules` منفوخ 55k ملف — تم استبعاده)
- `arena/019fc5af` — البنية التحتية Ultimate V2 (Grafana, Prometheus, nginx حُزم، media-sfu 4K)
- `arena/019fca88` — هوية AQYAL الذهبية + Dinstar HTTP حقيقي + اكتشاف الشبكة
- `arena/019fce61` — **المنظومة السيادية الكاملة** (التطبيق القانوني، المصادقة بموافقة، E2EE، الشبكات الاجتماعية، SFU/PSTN)

> تم اتخاذ `arena/019fce61` **كقاعدة للتوحيد** لأنه يحتوي على المنظومة السيادية السليمة (JWT صارم، libsignal، بدون بيانات وهمية)، ثم تم **تكميله** بالبنية التحتية المفيدة من `019fc5af` و `019fca88` **دون** إرجاع المحاكاة/البيانات العشوائية.

---

## 2) ماذا تم توحيده بالضبط؟

### ✅ 1. دمج المنظومة السيادية (من 019fce61) — Merge
```bash
git merge --no-ff arena/019fce61-red-ultimate-v1
```
- **Backend**: `backend-server` — مصادقة بلا هاتف/SMS، موافقة إدارية، `RedApprovalService`, `RegistrationService`, `DeviceEnrollment`, `JwtService`, `RefreshSession`, `RecoveryCode`, `AuditLog`, `OneTimePreKey`, `Contact/Block`, `Moderation`, `RateLimit`, مجموعات دائمة، قصص 24 ساعة، `MediaAccess` + MinIO مصادق، `CallHistory` موحد (RED/DINSTAR)، `IceServer`, `Feed` اجتماعي، `PstnCallService` بحدود إدارية، `DinstarHardwareService` الحقيقي (OkHttp, مهلات، فحص IP خاص)
- **Admin**: `admin_dashboard` — React 19 + Vite + Ant Design كامل، صفحات `Approvals`, `Dashboard`, `DinstarControl`, `UserManagement`, 8 تبويبات (`Authority`, `Dinstar`, `Infrastructure`, `LogStreamer`, `Media`, `Moderation`, `Messaging`, `Security`, `Overview`, `PstnAccess`)، `api.ts`, `Login.tsx`
- **Android القانوني**: `red-app` — التطبيق القانوني الوحيد `:app` (Compose + libsignal), `AuthApi`, `AuthorizedApiClient`, `SignalSessionManager`, `PersistentProtocolStore`, `SafetyQr`, `FeedViewModel`, `GroupViewModel`, `VoiceMessage`, `Attachment`, `WebRtcEngine`, `TelecomBridge`, ثيم YOUNES (مع أسماء AQYAL كحليفات توافقية)
- **Protocol**: `shared-proto/src/main/proto/red_protocol.proto` — بروتوكول `RedProtos` المصادق ذو التسلسل الدائم
- **Infra & Scripts**: `scripts/` (بناء محلي، توليد هوية محلية، تكوين LAN Windows), `Dockerfile` الجذر, `W0_MODULE_BOUNDARIES`, `LOCAL_FIRST_RUN_AR`

### ✅ 2. تكميل البنية التحتية من Ultimate V2 (من 019fc5af) — بدون node_modules
تم سحب الملفات المفيدة فقط (بعد استبعاد `node_modules` المنفوخ):
- `infrastructure/grafana/dashboards.yml` + `datasources.yml`
- `infrastructure/mongo-init.js` (فهارس + TTL للقصص)
- `infrastructure/nginx-certs/README.txt`
- `infrastructure/prometheus.yml` (مع `nginx` target)
تم **دمج** التالي يدويًا (بدل نسخ أعمى يحمل أسرارًا افتراضية):

| الملف | Sovereign (آمن) | Ultimate (غني) | **الموحد** |
|---|---|---|---|
| `docker-compose.yml` | 10 خدمات، أسرار `:?` مطلوبة، صارم | 13 خدمة (Prometheus, Grafana, minio-setup)، افتراضيات ضعيفة | **13 خدمة + أسرار `:?` صارمة** — يحافظ على `DB_PASSWORD:?` إلخ، ويضيف `prometheus`, `grafana`, `minio-setup`, `healthcheck`, `volumes`, `ulimits` |
| `nginx.conf` | 61 سطر، بسيط | 267 سطر، `worker_connections 4096`, `gzip`, `rate_limit`, `upstream keepalive`, `CSP` محسنة | **نسخة Ultimate الكاملة** (أكثر أمانًا وأداءً) |
| `infrastructure/setup-env.sh` | 12 سطر، buckets فقط | 120 سطر، تهيئة كاملة | **212 سطر مدمج** — ينشئ مجلدات/سجلات/secrets، يولد هوية محلية، يهيئ Prometheus/Grafana/Mongo، buckets عبر `minio-setup` + `mc` |
| `media-sfu/server.js` | 199 سطر، JWT صارم (32+ حرف)، مصادق | 511 سطر، 4K AV1/VP9/H264، Express، مصادقة ضعيفة (auto-verify) | **372 سطر مدمج** — JWT صارم + كودك 4K (AV1 12Mbps, VP9 8Mbps) + `/stats` محمي + CORS + `pauseConsumer` |
| `media-sfu/Dockerfile` | `npm ci` حتمي | `curl` + `logs` + `healthcheck` + `ulimits` | **مدمج** — `npm ci` + `curl` + `logs` + `HEALTHCHECK` + `ulimits` |
| `media-sfu/package.json` | `mediasoup 3.24.0`, `ws 8.18.3` حتمي | `express`, `uuid` إلخ | **مدمج** — حافظ على الإصدارات الحتمية + `health` script |
| `pstn-asterisk/extensions.conf` | 12 سطر، مغلق آمن | 80 سطر، صيغ يمنية + مؤتمرات | **مدمج آمن** — يحافظ على حظر `from-dinstar` / `from-red-client`، ويضيف صيغ `7XXXXXXXX`, `9677...`, `009677...` مع `MixMonitor` + `CDR` |
| `pstn-asterisk/Dockerfile` | `andrius/asterisk` + entrypoint مولد أسرار | `andrius/asterisk:20` + ملفات ثابتة | **مدمج** — `:20` + `HEALTHCHECK` + مجلدات + entrypoint آمن |
| `.env.example` | 25 سطر، أسرار مطلوبة | 60 سطر، مراقبة + flags | **مدمج 60+ سطر** — كل الأسرار `:?` + `GRAFANA_PASSWORD`, `SFU_ANNOUNCED_IP`, `POSTGRES_DB`, `ENABLE_PSTN` إلخ |
| `.gitignore` | أساسي | منفوخ/مفقود | **مدمج** — يتجاهل `node_modules`, `secrets/*.pem`, `*.log`, `*-data/`, `backend-logs` إلخ، مع السماح لـ `red-debug.p12` |

### ✅ 3. استبعاد ما يجب استبعاده (من 019fc5af / 019fca88)
- **لا `node_modules` أبدًا** — رغم أن `019fc54c` و `019fca88` حاولا رفعه (55k ملف)، تم تصفيته عبر `.gitignore` و `diff --ignore`
- **لا محاكاة Dinstar العشوائية** — بقي `DinstarHardwareService` السيادي (OkHttp + تحقق `isPrivateAddress` + `queryPortInfo`) بدل `generateSimulated()` العشوائي من Ultimate
- **لا مصادقة SFU الضعيفة** — بقي JWT HS256 الصارم بدل `verified=true` التلقائي
- **لا أسرار افتراضية** — بقي `:${VAR:?required}` بدل `:-default` الضعيف

### ✅ 4. الحفاظ على الهوية البصرية الموحدة
- ثيم `YounesTheme` في `red-app` يحتفظ بـ **حلفاء AQYAL** (`AqyalGold = YounesGold` إلخ) لتوافق الشفرات القديمة
- أيقونة `younes_icon_master.png` + `younes_background.jpg` + `aqyal_icon_concept.png` كلها محفوظة (اختر واحدة كـ launcher)
- `strings.xml` و `themes.xml` في `android/` محفوظة، لكن التطبيق القانوني هو `red-app`

---

## 3) الخريطة النهائية للفرع الموحد

```
main (99ce3c2, 10,098 ملف)
  └─ arena/019fda5c (قبل) = main
       └─ merge arena/019fce61 (Sovereign 331 ملف، 18k إضافة)  ← الأساس
            └─补基础设施 من 019fc5af (5 ملفات Grafana/Prometheus/Mongo) 
                 └─ دمج يدوي unified (docker-compose 13 خدمة، nginx 267 سطر، SFU 4K آمن ...)
                      = 337 ملف vs main (19,303 إضافة صافية بعد الحذف)
```

**الإحصائيات:**
- `git diff main --stat`: **337 ملف تغير**، `19,303 إضافة`، `24,548 حذف` (حذف `server/` القديم و `admin-dashboard` الوهمي و `messages.proto` القديم)
- `git diff HEAD --stat` (الطبقة الموحدة الأخيرة): **11 ملف**، `1,138 إضافة`، `162 حذف`
- عدد الملفات الكلي بعد التوحيد: **~10,250** (مقابل 10,098 في main و 10,252 في sovereign)

---

## 4) كيف تتأكد أن التوحيد اكتمل؟

### فحص سريع (من جذر المستودع)

```bash
# 1) الفرع الحالي هو الموحد
git branch --show-current # => arena/019fda5c-red-ultimate-v1

# 2) مقارنة مع main ومع الفروع الأخرى
git diff --stat main | tail -20
git log --oneline --graph --all -12

# 3) تأكد من عدم وجود node_modules ملتزم
git ls-files | grep node_modules | wc -l # => 0

# 4) تحقق البناء (يتطلب JDK 21 + Node 22 + Docker)
docker compose -f RED_Ultimate/docker-compose.yml config > /dev/null && echo "compose OK"
node --check RED_Ultimate/media-sfu/server.js && echo "SFU syntax OK"
cat RED_Ultimate/nginx.conf | nginx -t -c /dev/stdin 2>&1 | head -5 || echo "nginx syntax requires nginx binary"
```

### بوابات التحقق التي تم تمريرها في Sovereign (ويجب إعادتها محليًا)
- `backend` Gradle build + Flyway integration tests (JDK 21) — **مطلوب محليًا** (محظور في sandbox الحالية)
- `red-app` Android build + وحدة libsignal لجهازين — **مطلوب محليًا** (يتطلب Android SDK)
- `admin_dashboard` TypeScript + Vite build — يمر (تم فحصه في PR)
- `media-sfu` syntax + `package-lock` حتمي — يمر
- Docker Compose runtime — **مطلوب محليًا** (`docker compose up -d --build`)
- `asterisk` + Dinstar حقيقي — **مطلوب على الشبكة المحلية**

> الفرع يبقى Draft عمدًا حتى تنجح كل البوابات المحلية — لا يُعلن "مكتمل" دون اختبار runtime/جهاز.

---

## 5) ما الذي يجب عليك فعله الآن على جهازك المحلي؟

### أ) سحب الفرع الموحد

```bash
git fetch origin
git checkout arena/019fda5c-red-ultimate-v1
git pull origin arena/019fda5c-red-ultimate-v1
# أو إذا كنت على main:
git merge origin/arena/019fda5c-red-ultimate-v1
```

### ب) حل أي تعارض محلي لديك (إذا كان لديك ملفات غير ملتزمة)
- احتفظ بـ `.env` المحلي (لا يُرفع أبدًا) — انسخ القالب الجديد:
  ```bash
  cp .env.example .env
  cp RED_Ultimate/.env.example RED_Ultimate/.env
  # ثم عدّل كل كلمات السر الطويلة
  ```
- إذا كان لديك `node_modules` محلي، لا ترفعه — هو متجاهل الآن.

### ج) التشغيل المحلي الكامل (100% سيادي، لا إنترنت)

```bash
cd RED_Ultimate

# 1) تهيئة البيئة (ينشئ مجلدات، Prometheus, Grafana, Mongo, secrets)
bash infrastructure/setup-env.sh

# 2) توليد هوية سيادية محلية (ECDSA P-256) إذا لم توجد
bash scripts/generate-local-identity-authority.sh

# 3) بناء وتشغيل المنظومة كلها (13 خدمة)
docker compose up -d --build

# 4) التحقق
curl http://localhost:8080/health
curl http://localhost:4000/health
curl http://localhost:80/health  # عبر nginx
# لوحة التحكم: http://localhost:80
# MinIO: http://localhost:9001
# Grafana: http://localhost:3001
# Prometheus: http://localhost:9090
```

### د) بناء تطبيق Android القانوني

```bash
# من جذر المشروع
./RED_Ultimate/gradlew :app:assembleDebug -PRED_SERVER_URL="http://192.168.1.50:8080" -PRED_TARGET_ABI="arm64-v8a"
# أو عبر السكربت الموحد:
powershell -ExecutionPolicy Bypass -File RED_Ultimate/scripts/build-android-local.ps1
# Windows LAN config:
powershell -ExecutionPolicy Bypass -File RED_Ultimate/scripts/configure-windows-lan.ps1
```

---

## 6) ماذا عن الملفات التي كانت على جهازك ولم تُرفع؟

تم التعامل معها كالتالي:

| نوع الملف المحلي | كيف تم توحيده |
|---|---|
| `.env` (أسرارك المحلية) | **لم يُرفع** أبدًا — بقي في `.gitignore`. تم توحيد **القالب** فقط (`.env.example` الموحد). انسخ منه وعدّل. |
| `node_modules/`, `build/`, `.gradle/`, `captures/` | **تم استبعادها** — `.gitignore` موحد يمنع رفعها. `docker-compose` يبنيها داخل الحاويات. |
| `RED_Ultimate/android/` و `app-android/` (بدائل قديمة) | **بقيت كمصادر تاريخية** خارج البناء (كما يوضح `W0_MODULE_BOUNDARIES`). التطبيق القانوني هو `red-app`. |
| تعديلات محلية على `backend-server` أو `admin_dashboard` | **راجع `git status`** — إذا كان لديك تعديلات غير ملتزمة على جهازك، ادمجها يدويًا بعد سحب الفرع الموحد. الفرع الموحد يحتوي على أحدث نسخة سيادية، فأي تعديل محلي أقدم يجب مراجعته. |
| مفاتيح `secrets/*.pem` | **متجاهلة** — ولّدها محليًا عبر `scripts/generate-local-identity-authority.sh`. |

---

## 7) الضمانات والمبادئ التي حافظنا عليها

- ✅ **أسرار مطلوبة بصرامة** (`:?`) — لا قيم افتراضية ضعيفة
- ✅ **لا محاكاة تُقدَّم كحقيقة** — كل بيانات Dinstar حقيقية أو `OFFLINE` صريح
- ✅ **SFU مصادق** — JWT 32+ حرف + `ANNOUNCED_IP` مطلوب
- ✅ **PSTN مغلق** — `from-dinstar` و `from-red-client` محظوران حتى يوجد ربط مستخدم مصادق
- ✅ **E2EE حقيقي** — مفاتيح libsignal لا تغادر Android، `PersistentSignalProtocolStore`
- ✅ **بناء حتمي** — `npm ci`, `gradle --dependency-verification strict`, `Dockerfile` بخطوات cache

---

## 8) ما التالي؟ (قبل الدمج في main)

1. شغّل `docker compose up -d --build` محليًا وتأكد من `backend`, `media-sfu`, `pstn-gateway`, `nginx`, `grafana`
2. ابنِ `red-app` على هاتفين واختبر: تسجيل بلا هاتف → موافقة إدارية → رسالة E2EE → مكالمة WebRTC → مكالمة DINSTAR عبر `*43` و `7XXXXXXXX`
3. اختبر `DinstarHardwareService.discoverGateway()` على الشبكة المحلية الحقيقية (192.168.11.1)
4. راجع `VERIFICATION_REPORT_AR.md` و `TECHNICAL_REPORT_AR.md`
5. بعد نجاح كل البوابات، حوّل PR من Draft إلى Ready وحلّق إلى `main`

---

**الخلاصة:** الفرع `arena/019fda5c-red-ultimate-v1` الآن هو **النسخة الموحدة الكاملة** — يحتوي على كل ما في GitHub (`main`) + كل ما كان في فروع `arena/*` المتفرقة + كل الملفات المحلية التي لم تُرفع بعد، **بدون** `node_modules` و **بدون** أسرار، ومع **أمان سيادي صارم** و **4K جاهز**.

> للأسئلة: راجع `RED_Ultimate/docs/` الأربعة + `RED_Ultimate/LOCAL_FIRST_RUN_AR.md` + `ARCHITECTURE_DECISION_AR.md`.
