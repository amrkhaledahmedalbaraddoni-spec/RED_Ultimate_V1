# RED Ultimate — منصة سيادية محلية (Messaging + Calls + PSTN)

RED منصة مراسلة ومكالمات **محلية أولًا**: لا رقم هاتف، لا SIM، لا بريد، لا SMS/OTP لإنشاء حساب.
ينشئ المستخدم `username` + كلمة مرور، ويبقى الحساب والجهاز **PENDING** حتى موافقة المسؤول.

> **هذا المستودع مبني على ثلاث طبقات منفصلة بعقد واحد:**
> `platform/` (البنية التحتية) · `services/` (الكود المصدري) · `data/` (البيانات والمخطط)
> والرابط بينها هو `contracts/` + فاحص آلي يمنع أي تسرّب بين الطبقات.

---

## 1. الخريطة — أين يوجد كل شيء

```text
RED_Ultimate_V1/
├── contracts/                 ★ العقد الوحيد بين الطبقات (JSON، مصدر الحقيقة)
│   ├── stack.json             الخدمات، المنافذ، الـ volumes، الصحة، الملكية
│   ├── data.json              ملكية المخطط/البذور/الـ fixtures والنسخ الاحتياطي
│   ├── env.schema.json        كل متغيّر بيئة + هل هو سرّي/مطلوب
│   └── separation.rules.json  قواعد الفصل القابلة للتنفيذ
│
├── platform/                  ★ البنية التحتية (لا كود تطبيقي، لا بيانات)
│   ├── compose/docker-compose.yml   13 خدمة، شبكة 172.28.0.0/16
│   ├── containers/                  backend/admin/sfu/pstn/ci Dockerfiles
│   ├── proxy/                       nginx.conf + dashboard.conf + certs/
│   ├── monitoring/                  prometheus.yml + grafana provisioning
│   ├── telephony/                   Asterisk + DINSTAR (صوت فقط)
│   ├── config/.env.example          مولَّد من env.schema.json
│   └── secrets/                     غير محفوظ في Git (مفاتيح سلطة الهوية)
│
├── services/                  ★ الكود المصدري فقط
│   ├── android/               :app — تطبيق RED (Compose)
│   ├── backend/               Spring Boot/Kotlin/JVM 21 — API + WebSocket
│   ├── shared-proto/          red_protocol.proto (بروتوكول واحد)
│   ├── admin-dashboard/       React/Vite/Ant Design (SPA)
│   └── media-sfu/             mediasoup SFU لصوت/فيديو RED
│
├── data/                      ★ البيانات فقط
│   ├── sql/migrations/        V1…V13 (Flyway، تُحزَّم في jar وقت البناء)
│   ├── sql/schema/            master-schema.sql (مرجع قراءة فقط)
│   ├── mongo/init.js          فهارس وTTL عند أول تهيئة
│   ├── seeds/ · fixtures/     بذور مقصودة · عيّنات للتطوير فقط
│   └── runtime/               نسخ احتياطية محلية (غير محفوظة في Git)
│
├── tools/                     ★ الأدوات (تشغيل، فحص، نسخ احتياطي)
│   ├── guard/separation_guard.py     فاحص الفصل والتكامل
│   ├── env/render_env.py             مولّد .env من العقد
│   └── scripts/red.sh                نقطة التشغيل الوحيدة
│
├── docs/                      التوثيق (تحليل المشروع + الأدلة)
├── settings.gradle.kts · gradlew · gradle/ · build-logic/ · wire-handler/   أدوات بناء الكود
└── README.md
```

## 2. ابدأ في 60 ثانية

```bash
# 1) توليد بيئة خاصة آمنة + مفاتيح سلطة الهوية
tools/scripts/red.sh env 192.168.1.50      # عنوان جهازك في الشبكة المحلية

# 2) بناء وتشغيل المنظومة (مع انتظار صحة الخدمات فعليًا)
tools/scripts/red.sh up

# 3) التحقق من أن الطبقات ما زالت مفصولة ومتكاملة
tools/scripts/red.sh guard

# 4) بناء APK مضبوطًا على نفس الخادم
BUILD_ANDROID=1 tools/scripts/red.sh up 192.168.1.50
```

| الواجهة | العنوان |
|---|---|
| لوحة الإدارة | `http://SERVER_IP/` |
| صحة الخادم | `http://SERVER_IP/health` |
| صحة SFU | `http://SERVER_IP/sfu-health` |
| MinIO Console | `http://127.0.0.1:9001` (محلي فقط) |

## 3. العقد: لماذا هذا الفصل لا يكسر المشروع

| الطبقة | تملك | لا تملك أبدًا |
|---|---|---|
| `platform/` | كيف تُبنى وتُشغَّل وتُعرَض وتُراقَب الخدمات | منطق العمل، المخطط، الأسرار داخل Git |
| `services/` | منطق العمل والواجهات والبروتوكول | Dockerfile، nginx، compose، SQL، أسرار |
| `data/` | المخطط، الفهارس، البذور، النسخ الاحتياطي | كود تطبيقي أو إعداد تشغيل |
| `contracts/` | الحقيقة الواحدة لكل خدمة/متغيّر/بيانات | تنفيذ أو حالة |

**التكامل مضمون بآليات قابلة للفحص، لا بالاتفاق الشفهي:**

1. **Data → Service (وقت البناء):** `services/backend/build.gradle.kts` يحزم `data/sql`
   كموارد (`classpath:migrations`)؛ لا توجد نسخة SQL واحدة داخل `services/`.
2. **Contract → Platform:** `docker-compose.yml` يجب أن يطابق `contracts/stack.json`
   خدمةً بخدمة، ومنفذًا بمنفذ، وvolume بـ volume.
3. **Contract → Environment:** كل `${VAR}` في compose مُعرَّف في `env.schema.json`،
   و`.env.example` مولَّد منه، ولا قيمة سرّية محفوظة.
4. **Contract → Edge:** Nginx يوجّه `/api`, `/ws`, `/sfu`, `/minio`, `/grafana`
   إلى خدمات معرّفة في العقد؛ لوحة الإدارة SPA ساكنة لا تعرف الـ backend.
5. **Guard:** `tools/guard/separation_guard.py` يفشل البناء عند أي خرق (تسرّب ملف،
   تكرار تعريف، مسار مكسور، متغيّر غير معرَّف، هجرة مكرّرة، رابط ميت).

## 4. مبادئ غير قابلة للكسر

- لا هاتف/SIM/بريد/SMS/OTP للتسجيل؛ الموافقة الإدارية إلزامية لكل جهاز.
- RED صوت/فيديو عبر WebRTC وSFU/TURN، وDINSTAR مسار PSTN منفصل تمامًا يعتمد شرائح SIM.
- مفاتيح libsignal الخاصة لا تغادر Android (AES-GCM + Keystore).
- المحتوى الاجتماعي العام **ليس** E2EE، ولا يُوصف كذلك.
- لا توصف ميزة بأنها مكتملة قبل البناء واختبار runtime/الجهاز المناسب.
- وحدة Signal التاريخية **أُخرجت من المستودع**؛ أرشيفها هو تاريخ Git فقط
  (انظر `docs/analysis/archive/`).

## 5. التوثيق

| الملف | المحتوى |
|---|---|
| [`docs/ARCHITECTURE_SEPARATION_AR.md`](docs/ARCHITECTURE_SEPARATION_AR.md) | **التحليل الكامل وخطة الفصل والتنفيذ والعقود** |
| [`docs/01-PROJECT-OVERVIEW.md`](docs/01-PROJECT-OVERVIEW.md) | نظرة معمارية ومكوّنات |
| [`docs/02-DATABASES.md`](docs/02-DATABASES.md) | توزيع البيانات وFlyway/المخطط |
| [`docs/03-SERVER-ADMIN-PANEL.md`](docs/03-SERVER-ADMIN-PANEL.md) | الخادم ولوحة الإدارة وتدفق الطلبات |
| [`docs/04-APPS.md`](docs/04-APPS.md) | تطبيق Android والمصادر المؤرشفة |
| [`docs/W0_MODULE_BOUNDARIES.md`](docs/W0_MODULE_BOUNDARIES.md) | حدود الوحدات القانونية |
| [`docs/LOCAL_FIRST_RUN_AR.md`](docs/LOCAL_FIRST_RUN_AR.md) | تشغيل Alpha محليًا خطوة بخطوة |
| [`docs/DEPLOY.md`](docs/DEPLOY.md) | نشر ونسخ احتياطي واستعادة |

## 6. بوابة التحقق

```bash
tools/scripts/red.sh guard     # الفصل + التكامل (سريع، بلا Docker)
tools/scripts/red.sh config    # docker compose config --quiet
tools/scripts/red.sh up        # بناء وتشغيل + انتظار /health و/sfu-health
python3 tools/env/render_env.py --example   # تحديث قالب البيئة من العقد
```

بوابة CI (`.github/workflows/docker-image.yml`) تشغّل الفاحص أولًا، ثم تبني صورة
`platform/containers/ci.Dockerfile` التي تختبر backend وAndroid (dependency verification صارم)
ولوحة الإدارة وSFU وإعداد Asterisk.

> ما لا تستطيع البوابة الآلية إثباته: هاتفان حقيقيان لـ E2EE/WebRTC، اختبار TURN بين شبكتين،
> عتاد DINSTAR/الشرائح اليمنية، ونسخ احتياطي/استعادة مثبت. هذه تبقى بوابات بشرية معلنة.

## 7. الترخيص

انظر [`LICENSE`](LICENSE) و[`NOTICE`](NOTICE). أجزاء تاريخية مستمدّة من Signal
تبقى تحت ترخيصها الأصلي في تاريخ Git.
