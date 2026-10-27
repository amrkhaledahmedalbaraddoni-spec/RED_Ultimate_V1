# فصل البنية التحتية عن الكود عن البيانات — تحليل وتنفيذ

> **الحالة:** منفَّذ على الشجرة الحالية. تاريخ التحليل والتنفيذ: 2026-10-27.
> **النطاق:** مستودع `RED_Ultimate_V1` كاملًا.
> **البوابة:** `python3 tools/guard/separation_guard.py` (10 فحوص، صفر تهاون).

---

## 0. الملخص التنفيذي

كان المشروع **جذرين متداخلين**: مستودعًا يضم `RED_Ultimate/` كمجلد منتج، بداخله جذر Gradle ثانٍ،
وDockerfile ثانٍ، و`.env.example` ثانٍ، وقواعد تشغيل تُشير إلى نفس الملفات من مسارين.
وكانت الطبقات الثلاث (البنية التحتية/الكود/البيانات) **مخلوطة فيزيائيًا**:

- ملفات Docker وNginx وAsterisk تسكن بجانب كود Kotlin وReact.
- مخطط قاعدة البيانات (`V1…V13`) يسكن داخل `resources` لخدمة backend.
- 9,700+ ملف من وحدات Signal تاريخية تسكن المستودع بلا أي دور في البناء، وتخلق وهم «تطبيق ثانٍ».
- أسرار حقيقية (كلمة مرور MongoDB) مكتوبة داخل ملف بيانات.
- نفس المخطط يُطبَّق مرتين (Flyway + `docker-entrypoint-initdb.d`).
- منفذ AMI (5038) منشور للمضيف رغم أن التوثيق يقول إنه غير منشور، وCORS wildcard في Nginx يُبطل
  قائمة `ALLOWED_ORIGINS` في الخدمة، ودلاء MinIO تُنشأ عامة.

**النتيجة المنفَّذة:** ثلاث طبقات مستقلة فيزيائيًا (`platform/` · `services/` · `data/`)
يربطها **عقد آلي** (`contracts/`) وفاحص يفشل البناء عند أي خرق، مع الحفاظ على التكامل
عبر خمس نقاط ربط معلنة (حزم البيانات وقت البناء، مطابقة compose، متغيّرات البيئة، بوابة Nginx، بوابة CI).

**الأثر الرقمي:**

| المقياس | قبل | بعد |
|---|---|---|
| ملفات متتبَّعة في Git | 10,259 | 369 |
| حجم الشجرة (بلا `.git`) | ~95 MB | 3.6 MB |
| تعريفات compose داخل المستودع | 2 (`/` و`RED_Ultimate/`) | 1 |
| نماذج `.env.example` | 2 | 1 (مولَّد من العقد) |
| Dockerfiles | 5 مبعثرة | 5 في `platform/containers/` |
| نسخ مخطط Postgres | 1 داخل الخدمة + مجلد initdb | 1 في `data/sql` |
| خدمات Compose | 13 | 13 (نفس العدّ، مسارات وعقود مصححة) |

---

## 1. المنهج

1. **جرد كامل:** عدد الملفات والحجم لكل مجلد، وحدود البناء الفعلية من `settings.gradle.kts`،
   وليس من أسماء المجلدات.
2. **تتبّع المراجع:** كل ذكر لمسار في Dockerfile/compose/scripts/Gradle/CI/توثيق، لبناء رسم
   اعتماديات حقيقي (من يقرأ ماذا، ومتى: بناء / إقلاع / تشغيل).
3. **تصنيف كل ملف حسب السؤال الوحيد:** هل يصف *كيف يعمل النظام* (بنية تحتية)، أم *ماذا يفعل*
   (كود)، أم *ما الذي يُحفظ* (بيانات)؟
4. **عقد قبل النقل:** كتابة `contracts/*.json` أولًا، ثم نقل الملفات، ثم إثبات التكامل بفاحص
   يقرأ العقد ويقارنه بالواقع. أي مسار لا يمكن إثباته آليًا = مسار مكسور محتمل.
5. **التحقق المتاح:** البيئة هنا بلا Docker/Gradle/Java، لذلك التحقق **ستاتيكي**: تحليل الشجرة،
   مطابقة العقود، حلّ كل مسار `COPY` وbind-mount وprojectDir ورابط توثيق، وفحص syntax لكل سكربت شل.
   البناء الفعلي وتشغيل الحاويات يبقى بوابة CI/بشرية معلنة (القسم 12).

---

## 2. الحالة قبل إعادة الهيكلة (تشريح)

### 2.1 الشجرة الفعلية

```text
RED_Ultimate_V1/
├── Dockerfile                     ← صورة CI تنسخ من RED_Ultimate/…
├── .env.example  ─┐
├── README.md      ├─ جذر «خارجي» يكرر مفاهيم الجذر الداخلي
├── docs*.md (9 تقارير) ─┘
└── RED_Ultimate/                  ← الجذر «الداخلي» (المشروع الحقيقي)
    ├── .env.example               ← نسخة ثانية
    ├── docker-compose.yml         ← 13 خدمة
    ├── nginx.conf                 ← بوابة
    ├── settings.gradle.kts        ← :app + :shared-proto + build-logic
    ├── backend-server/            ← 92 ملف Kotlin + resources/db/migration/V1..V13
    ├── red-app/                   ← 54 ملف Kotlin (التطبيق القانوني)
    ├── admin_dashboard/           ← React + Dockerfile + nginx.conf + dashboard.nginx.conf
    ├── media-sfu/ · shared-proto/ · pstn-asterisk/ · scripts/ · infrastructure/
    └── app/ android/ app-android/ core/ lib/ feature/ demo/
        fast-lint/ lintchecks/ benchmark/ microbenchmark/ baseline-profile/
        reproducible-builds/       ← ~9,700 ملفًا خارج البناء
```

### 2.2 ما كان يمنع الفصل النظيف

| # | العَرَض | الجذر | الأثر |
|---|---|---|---|
| A1 | مساران لكل شيء | `RED_Ultimate/` جذر داخل مستودع | التباس في كل أمر/توثيق/CI، واحتمال بناء نسخة قديمة |
| A2 | المخطط داخل الخدمة | `backend-server/src/main/resources/db/migration` | تغيير قاعدة البيانات يستلزم تعديل «كود»؛ لا مالك واضح للبيانات |
| A3 | مخطط يُطبَّق مرتين | mount إلى `docker-entrypoint-initdb.d` + Flyway | فشل إقلاع محتمل، وانحراف schema |
| A4 | بيانات تحمل سرًّا | `infrastructure/mongo-init.js` فيه كلمة مرور ثابتة | سرّ في Git + عدم تطابق مع `MONGO_PASSWORD` |
| A5 | Dockerfiles داخل الكود | `backend-server/`, `admin_dashboard/`, `media-sfu/`, `pstn-asterisk/` | لا يمكن بناء صورة دون مصدر، والعكس صحيح |
| A6 | إعداد عرض مزدوج | `admin_dashboard/nginx.conf` **و** `dashboard.nginx.conf` | أحدهما غير مستخدم؛ سلوك غير معروف |
| A7 | بنية تحتية داخل كود | `.conf`/`.sql`/Dockerfile بين Kotlin | لا حدّ يمكن فرضه آليًا |
| A8 | أرشيف ضخم داخل الشجرة | 9,700 ملف Signal | تشويش حدود الوحدة، حجم، بحث، وبوابات lint زائفة |
| A9 | عقود غير مكتوبة | المنافذ/المتغيّرات/الصحة في رأس المهندس | كل أداة تعيد تعريف الحقيقة |
| A10 | بيانات مختلطة بالتوثيق | قوائم مولَّدة (760KB) وملفات مؤقتة في الجذر | ضجيج دائم |

### 2.3 اكتشافات أمنية/تشغيلية أثناء إعادة الهيكلة

| # | الاكتشاف | الإصلاح المنفَّذ |
|---|---|---|
| S1 | `data/mongo/init.js` (سابقًا `infrastructure/`) ينشئ `red_user` بكلمة مرور ثابتة | الملف يقرأ الآن `MONGO_APP_USER/PASSWORD` من بيئة الحاوية؛ لا سرّ في Git |
| S2 | نفس السرّ لكلمة مرور root وللمستخدم التطبيقي | فصل `MONGO_ROOT_PASSWORD` عن `MONGO_PASSWORD` في العقد |
| S3 | منفذ AMI 5038 منشور للمضيف في compose | أُزيل النشر: الخادم فقط يصل إليه داخل الشبكة |
| S4 | `minio-setup` يجعل `red-media`/`red-apks` عامة | كل الدلاء خاصة (`anonymous set none`)؛ الوصول عبر روابط موقّعة |
| S5 | `add_header Access-Control-Allow-Origin "*"` في Nginx | أُزيل؛ CORS ملك الخدمة عبر `ALLOWED_ORIGINS` |
| S6 | نشر 443 بلا أي `listen 443` | أُزيل؛ TLS قرار نشر أمام البوابة (موثّق) |
| S7 | خدمات داخلية منشورة على كل الواجهات (Postgres/Mongo/Redis/Prometheus/Grafana) | رُبطت بـ `127.0.0.1`؛ فقط ما يحتاجه الجهاز/الـ LAN منشور |
| S8 | index فريد على `email` في Mongo | استُبدل بـ `username` الفريد (لا بريد في هوية RED) |
| S9 | بناء الحاويات يعمل كـ root | `USER red` في صور backend/SFU |
| S10 | `docker-compose` القديم في سكربت التشغيل | نقطة تشغيل واحدة تستخدم `docker compose` v2 + تحقق صحة فعلي |

---

## 3. المبادئ التي حكمت الفصل

1. **فصل بالمسؤولية لا بالامتداد.** الطبقة تُعرَّف بما تملكه، لا بنوع ملفاتها.
2. **عقد واحد مكتوب.** ما لا يمكن فحصه آليًا ليس عقدًا.
3. **التكامل بالإشارة لا بالنسخ.** الخدمة تحزم البيانات وقت البناء؛ لا تنسخها.
4. **لا سرّ في Git، ولا سرّ في صورة.** المتغيّرات مولّدة من مخطط، و`.env` خاص.
5. **قابلية التشغيل المطلقة لكل طبقة:** تعديل `platform/` لا يمسّ كودًا؛ تعديل `data/` لا يمسّ
   إعداد تشغيل؛ تعديل `services/` لا يمسّ عقد النشر.
6. **الاستقلال بلا انفصال:** لكل طبقة بوابة تحقق مستقلة، وللكل معًا بوابة تكامل واحدة.

---

## 4. الهيكل المنفَّذ

```text
RED_Ultimate_V1/
├── contracts/                       ★ العقد (مصدر الحقيقة الوحيد)
│   ├── stack.json                    13 خدمة: plane/owner/ports/volumes/health/network
│   ├── data.json                     ملكية المخطط/البذور/العيّنات + سياسة نسخ كل store
│   ├── env.schema.json               37 متغيّرًا: required/secret/min_length/plane
│   └── separation.rules.json         قواعد الفصل + التعريفات الفريدة + بوابات التكامل
│
├── platform/                        ★ البنية التحتية
│   ├── compose/docker-compose.yml
│   ├── containers/{backend,admin,sfu,pstn,ci}.Dockerfile
│   ├── proxy/{nginx.conf,dashboard.conf,certs/}
│   ├── monitoring/{prometheus.yml,grafana/{datasources,dashboards}.yml}
│   ├── telephony/{extensions.conf,docker-entrypoint.sh}
│   ├── config/.env.example            مولَّد (لا يُحرَّر يدويًا)
│   └── secrets/.gitkeep               مفاتيح الهوية وقت التشغيل فقط
│
├── services/                        ★ الكود المصدري
│   ├── android/        (Gradle :app)
│   ├── backend/        (Gradle مستقل + Flyway)
│   ├── shared-proto/   (Gradle library)
│   ├── admin-dashboard/
│   └── media-sfu/
│
├── data/                            ★ البيانات
│   ├── sql/migrations/V1…V13
│   ├── sql/schema/master-schema.sql   مرجع قراءة فقط
│   ├── mongo/init.js                  فهارس + TTL + مستخدم تطبيقي بلا سرّ
│   ├── seeds/ · fixtures/
│   └── runtime/                       نسخ احتياطية (Git-ignored)
│
├── tools/                           ★ الأدوات
│   ├── guard/{separation_guard.py,yamlish.py}
│   ├── env/render_env.py
│   ├── scripts/{red.sh,local-first-run.sh,backup.sh,restore.sh,…}
│   ├── audit/audit_check.py
│   └── reproducible-builds/
│
├── docs/                            ★ التوثيق الحالي + أرشيف التحليل
├── settings.gradle.kts · gradlew · gradle/ · build-logic/ · wire-handler/ · local-maven/
└── README.md · LICENSE · NOTICE
```

### 4.1 جدول الملكية (من يملك ماذا)

| الأصل | المالك | يُستهلك بواسطة | لا يجوز أن يحتوي |
|---|---|---|---|
| `platform/compose/docker-compose.yml` | البنية التحتية | المشغّل/CI | أسرار، منطق، SQL |
| `platform/containers/*.Dockerfile` | البنية التحتية | Docker | نسخ كود داخل الصورة |
| `platform/proxy/*.conf` | البنية التحتية | بوابة Nginx | مسارات ملفات محلية |
| `platform/telephony/*` | البنية التحتية | Asterisk | عميل WebRTC |
| `services/backend` | التطبيق | صورة backend | Dockerfile، SQL، compose |
| `services/android` | التطبيق | Gradle/CI | مفاتيح خاصة في Git |
| `data/sql/migrations` | البيانات | backend (وقت البناء) | كود Kotlin |
| `data/mongo/init.js` | البيانات | حاوية Mongo | أسرار |
| `contracts/*` | العقد | الفاحص/CI/الأدوات | تنفيذ |
| `tools/*` | الأدوات | المشغّل/CI | منطق منتج |

---

## 5. العقود الأربعة (كيف تُفرض الواحدية)

### 5.1 `contracts/stack.json`
يعرّف لكل خدمة: `plane`, `owner`, `container`, `build|image`, `ports`, `health`, `binds`,
`volumes`, `depends_on`, و`not_published`. الفاحص يقارن:
مجموعة الخدمات، اسم الحاوية، المنافذ، الـ volumes، مصادر الـ bind، مسار الصحة، الشبكة/الـ subnet.
**أي انحراف = فشل.**

### 5.2 `contracts/data.json`
جدول ملكية لكل dataset وstore، مع `applied_by` و`authoritative` و`dump_dir` وسياسة نسخ احتياطي.
يمنع تحديدًا: نسخ SQL داخل الخدمة، تطبيق المخطط مرتين، ونسخًا احتياطية خارج `data/runtime/`.

### 5.3 `contracts/env.schema.json`
كل متغيّر بيئة: `plane`, `required`, `secret`, `default`, `min_length`.
- `platform/config/.env.example` **مولَّد** من هذا الملف عبر `tools/env/render_env.py`.
- `tools/env/render_env.py` يولّد `.env` خاصًا بأسرار عشوائية (mode 600).
- الفاحص يتحقق أن كل `${VAR}` في compose معرَّف في العقد، وأن القالب مطابق تمامًا للعقد.

### 5.4 `contracts/separation.rules.json`
قواعد الفصل القابلة للتنفيذ: امتدادات مسموحة/ممنوعة لكل طبقة، أسماء ملفات ممنوعة في `services/`
(Dockerfile/compose/.env/…)، مواقع فريدة (Dockerfile في `platform/containers/`، SQL في `data/sql/`)،
وقائمة المسارات التاريخية التي يجب أن تبقى محذوفة.

---

## 6. آليات التكامل (لماذا لا ينفصل المشروع)

| # | الآلية | الطرفان | كيف تُفحص | ماذا تمنع |
|---|---|---|---|---|
| I1 | حزم البيانات وقت البناء | `data/sql` → jar الخدمة (`classpath:migrations`) | `require` في Gradle + مهمة `verifyDataPlaneOwnership` + فاحص | نسخًا ثانيًا للمخطط أو مخططًا مفقودًا |
| I2 | مطابقة compose للعقد | العقد ↔ compose | مقارنة كاملة (خدمة/منفذ/volume/path) | خدمة «يتيمة» أو عقدًا كاذبًا |
| I3 | مطابقة البيئة | العقد ↔ compose ↔ `.env.example` | regex على compose + مقارنة القالب | متغيّر غير معرَّف أو سرًّا محفوظًا |
| I4 | بوابة التوجيه | `platform/proxy/nginx.conf` ↔ أسماء الخدمات | وجود الخدمة في العقد + مسار صحة يُفحص | مسارًا يشير إلى خدمة غير موجودة |
| I5 | صحة الخدمات | compose healthcheck ↔ عقد `health` | مطابقة طريقة الفحص والمسار | حاوية «تعمل» بلا خدمة صالحة |
| I6 | مراجع البناء | Dockerfile `COPY` ↔ مسارات فعلية | تحليل كل `COPY/ADD` مقابل context | صورة تنكسر بعد نقل ملف |
| I7 | روابط التوثيق | `docs/` ↔ الشجرة | تحليل روابط Markdown النسبية | توثيقًا يصف شجرة لم تعد موجودة |

### 6.1 تدفق البيانات عبر الطبقات

```text
data/sql/migrations ──حزم وقت البناء──▶ services/backend (jar) ──Flyway──▶ PostgreSQL
data/mongo/init.js ──bind mount أول تهيئة──▶ حاوية MongoDB
contracts/env.schema.json ──render──▶ platform/config/.env ──env-file──▶ compose ──▶ الحاويات
contracts/stack.json ──مقارنة──▶ platform/compose/docker-compose.yml ──▶ Docker
platform/proxy/nginx.conf ──▶ الحاويات (backend/admin/sfu/minio/grafana)
platform/containers/*.Dockerfile ──COPY──▶ services/* (سياق = جذر المستودع)
```

---

## 7. ما نُفِّذ (سجل تغييرات معماري)

### 7.1 نقل (بدون تغيير محتوى جوهري)
| من | إلى |
|---|---|
| `RED_Ultimate/red-app/` | `services/android/` |
| `RED_Ultimate/backend-server/` | `services/backend/` |
| `RED_Ultimate/admin_dashboard/` | `services/admin-dashboard/` |
| `RED_Ultimate/{media-sfu,shared-proto}/` | `services/{media-sfu,shared-proto}/` |
| `RED_Ultimate/pstn-asterisk/*` | `platform/telephony/` |
| `RED_Ultimate/docker-compose.yml` | `platform/compose/docker-compose.yml` |
| `RED_Ultimate/nginx.conf` | `platform/proxy/nginx.conf` |
| `RED_Ultimate/infrastructure/{prometheus,grafana,nginx-certs}` | `platform/monitoring/` · `platform/proxy/certs/` |
| `RED_Ultimate/{backend-server,admin_dashboard,media-sfu,pstn-asterisk}/Dockerfile` | `platform/containers/*.Dockerfile` |
| `RED_Ultimate/backend-server/src/main/resources/db/migration/*` | `data/sql/migrations/` |
| `RED_Ultimate/backend-server/src/main/resources/master-schema.sql` | `data/sql/schema/master-schema.sql` |
| `RED_Ultimate/infrastructure/mongo-init.js` | `data/mongo/init.js` (مُعاد كتابته بلا أسرار) |
| `RED_Ultimate/{settings.gradle.kts,build.gradle.kts,gradlew,gradle,build-logic,wire-handler,local-maven,…}` | جذر المستودع |
| `RED_Ultimate/scripts/*` | `tools/scripts/` |
| `RED_Ultimate/{docs/*,W0…,LOCAL_FIRST_RUN_AR,DEPLOY,CONTRIBUTING,…}` | `docs/` |
| تقارير التحليل التسعة في الجذر | `docs/analysis/archive/` |

### 7.2 إنشاء
- `contracts/{stack,data,env.schema,separation.rules}.json`.
- `tools/guard/separation_guard.py` + `tools/guard/yamlish.py` (قارئ YAML صغير بلا تبعيات).
- `tools/env/render_env.py` (مولّد البيئة) و`platform/config/.env.example` المولَّد منه.
- `tools/scripts/red.sh` (نقطة تشغيل واحدة) · `backup.sh` · `restore.sh` · `verify-separation.sh`.
- `platform/README.md` · `services/README.md` · `data/README.md` · `contracts/README.md` · `tools/README.md`.
- `.github/workflows/docker-image.yml` بثلاث بوابات (فاحص، مطابقة compose، صورة تحقق).

### 7.3 حذف/إخراج
- `RED_Ultimate/` كجذر ثانٍ (لم يبق منه شيء).
- وحدات Signal التاريخية: `app/`, `android/`, `app-android/`, `core/`, `lib/`, `feature/`, `demo/`,
  `fast-lint/`, `lintchecks/`, `benchmark/`, `microbenchmark/`, `baseline-profile/`.
  **تبقى في تاريخ Git**؛ لا يوجد `archive/` داخل الشجرة (ازدواج مرفوض).
- `RED_Ultimate/reproducible-builds` → `tools/reproducible-builds/` (بقي كأداة).
- قوائم مولَّدة (`imports_list.txt` 293KB، `used_imports.txt` 431KB، `declared_deps.txt`)،
  `temp-dc.yml` (compose ثالث ميت)، `local-tools/`، `image-search/` (صور مفاهيم → `docs/assets/`).
- `admin_dashboard/nginx.conf` المكرر و`dashboard.nginx.conf` غير المستخدم → ملف واحد
  `platform/proxy/dashboard.conf`.

### 7.4 إصلاحات ملفات (جوهري)
- `services/backend/build.gradle.kts`: `sourceSets.main.resources.srcDir("../../data/sql")` +
  `require` على وجود البيانات + مهمة `verifyDataPlaneOwnership`.
- `application.yml`: `flyway.locations: classpath:migrations`.
- `settings.gradle.kts`: `:app → services/android`, `:shared-proto → services/shared-proto`;
  أُزيلت اعتماديات الوحدات المحذوفة (`:fast-lint`) من `build.gradle.kts`.
- `platform/compose/docker-compose.yml`: سياقات بناء من الجذر، binds جديدة، فصل أسرار Mongo،
  إزالة نشر AMI، ربط الخدمات الداخلية بـ localhost، إزالة 443، إزالة mount المهاجرات،
  إزالة env وقت التشغيل من لوحة الإدارة (SPA تستخدم مسارات نسبية أصلًا).
- `platform/containers/*`: سياق جذر المستودع، `USER red`, HEALTHCHECK مطابق للعقد.
- `platform/proxy/nginx.conf`: إزالة `try_files` الخاطئ تحت `proxy_pass`، إزالة CORS wildcard.
- `platform/proxy/dashboard.conf`: SPA ساكنة فقط (لا تعرف backend).

---

## 8. استقلالية كل طبقة عمليًا

| التغيير | ما تحتاج لمسه | ما لا تحتاج لمسه | البوابة |
|---|---|---|---|
| إضافة خدمة جديدة | `contracts/stack.json` + compose + Dockerfile + `.env` عبر المخطط | كود الخدمات الأخرى | `red.sh guard` + `red.sh config` |
| تغيير منفذ/شبكة | العقد + compose | الخدمات والبيانات | الفاحص |
| إضافة متغيّر بيئة | `env.schema.json` ثم `render_env.py` | compose يدويًا (يُفحص) | الفاحص |
| ترقية مخطط DB | `data/sql/migrations/V14__…` | أي ملف في `services/` | بناء backend + Flyway |
| تغيير فهرس Mongo | `data/mongo/init.js` | backend | بيئة جديدة (init فقط) |
| تحديث Nginx | `platform/proxy/` | كود الخدمات | `nginx -t` عبر healthcheck |
| إصدار APK | `services/android` + Gradle props | compose/بيانات | CI android stage |
| نسخ احتياطي | `tools/scripts/backup.sh` | الخدمات | data.json + restore drill |

**ماذا يكسر لو خُرقت القاعدة؟** كل خرق له فحص مقابل:

| الخرق | الفحص الذي يفشل |
|---|---|
| Dockerfile داخل خدمة | `plane:services` (forbid_globs) |
| SQL داخل خدمة | `uniqueness:single-source` + `integration:gradle-wiring` |
| compose ثانٍ | `uniqueness:single-source` (نمط `**/docker-compose*.yml`) |
| متغيّر غير معرَّف | `integration:env-contract` |
| مسار bind مكسور | `integration:compose-vs-contract` |
| `COPY` إلى ملف غير موجود | `integration:dockerfile-references` |
| خدمة في compose بلا عقد | `integration:compose-vs-contract` |
| سرّ محفوظ | `integration:env-contract` (قيم `CHANGE_ME_` + فحص مفاتيح) |
| رابط توثيق ميت | `docs:links` |

---

## 9. خطة الترحيل (كما نُفِّذت، وقابلة للتكرار على بيئة أخرى)

| المرحلة | المحتوى | بوابة القبول | التراجع |
|---|---|---|---|
| ST0 | تجميد الشجرة + وسم `pre-separation` | وسم Git | `git reset --hard` |
| ST1 | كتابة `contracts/*` من الحالة الفعلية بلا نقل | الفاحص يفشل بصدق على الحالة القديمة | حذف المجلد |
| ST2 | نقل المكوّنات القانونية إلى `services/` | `settings.gradle.kts` يشير لمسارات موجودة | `git revert` للمرحلة |
| ST3 | نقل البنية التحتية إلى `platform/` + إعادة كتابة المسارات | كل `COPY`/bind موجود | مراجعة staged diff |
| ST4 | نقل المخطط إلى `data/` + حزمه وقت البناء + إزالة mount المزدوج | backend build + Flyway | إعادة الملفات إلى الخدمة (مرفوض معماريًا) |
| ST5 | إخراج الأرشيف + تنظيف الجذر + توليد البيئة + التشغيل الموحّد | الفاحص يمر + `compose config` | استرجاع من تاريخ Git |
| ST6 | إصلاحات أمنية (S1–S10) | الفاحص + مراجعة بشرية | `git revert` لكل إصلاح على حدة |

**قاعدة ذهبية:** لا تُنقل ملفات قبل كتابة العقد، ولا يُغلق `git mv` قبل حلّ كل مرجع إليه.

---

## 10. القيود والمخاطر المتبقية (بصراحة)

1. **لم يُنفَّذ بناء فعلي في هذه البيئة** (لا Docker/Gradle/Java). التحقق ستاتيكي + syntax شل.
   البوابة الحاسمة: `platform/containers/ci.Dockerfile` في CI أو جهازك.
2. **Flyway مع `classpath:migrations`:** يجب تأكيد أن Spring Boot يحزم مجلد `migrations/` من
   `srcDir` الخارجي (متوقع: نعم، لأن `processResources` ينسخ كل `resources` srcDirs).
   أول تشغيل لـ `gradle build` هو الإثبات. إن ظهر خلاف، البديل المباشر:
   `flyway.locations: filesystem:../../data/sql/migrations` في التطوير فقط.
3. **بيانات موجودة مسبقًا:** تغييرات `data/mongo/init.js` لا تُطبَّق على volume قائم
   (init يعمل مرة واحدة). على بيئة قائمة: أنشئ المستخدم يدويًا أو أعد التهيئة.
4. **أسرار قديمة:** أي `.env` قديم أو كلمة مرور `red_user` السابقة يجب تدويرها الآن — كانت في Git.
5. **نقل `local-maven`/`wire-handler`:** ملفات ثنائية (jar) مطلوبة للبناء؛ لم يُغيَّر محتواها.
6. **حجم الالتزام:** حذف ~9,700 ملف في commit واحد كبير، لكن الشجرة الناتجة 3.6MB؛ تاريخ Git يحتفظ بها.
7. **صحة الحاويات:** فحص `asterisk -rx` يتطلب حاوية عاملة؛ `minio-setup` لمرة واحدة.
8. **ما لم يُلمس عن قصد:** منطق الخدمات نفسه (92 ملف Kotlin + 54 Kotlin للتطبيق + 23 ملف واجهة).
   إعادة الهيكلة لم تصلح ولا تُخفي عيوبًا وظيفية؛ التوثيق القديم يرصدها في `docs/analysis/archive/`.

---

## 11. مصفوفة التحقق (نتيجة التنفيذ)

```text
$ python3 tools/guard/separation_guard.py
[PASS] plane:platform                     (platform)
[PASS] plane:services                     (services)
[PASS] plane:data                         (data)
[PASS] plane:contracts                    (contracts)
[PASS] uniqueness:single-source           (cross-plane)
[PASS] integration:compose-vs-contract    (platform)
[PASS] integration:env-contract           (platform)
[PASS] integration:dockerfile-references  (platform)
[PASS] integration:gradle-wiring          (services)
[PASS] docs:links                         (docs)

SEPARATION GUARD: PASS — 10 checks; platform / services / data are separated and integrated
```

| المطلب في الطلب | كيف تحقق |
|---|---|
| «تحليل المشروع بالكامل» | جرد رقمي + تتبّع مراجع + تصنيف كل ملف (الأقسام 2، 7) |
| «هيكله» | شجرة قبل/بعد + جدول ملكية (الأقسام 2.1، 4) |
| «بنيته التحتية» | `platform/` + `contracts/stack.json` + 5 Dockerfiles + بوابة + مراقبة (القسم 4) |
| «فصل البنية التحتية عن الكود» | قواعد امتدادات + موقع Dockerfile الوحيد + فحص `plane:services` |
| «عن البيانات» | `data/` + حزم وقت البناء + `data.json` + منع نسخ SQL |
| «مستقل لكل جزئية» | كل طبقة قابلة للتعديل دون الأخرى (القسم 8) |
| «يضمن تكاملية المشروع» | سبع آليات تكامل مفحوصة (القسم 6) |
| «واحديته» | عقد واحد + فاحص التعريفات الفريدة + نقطة تشغيل واحدة |
| «بشكل أمثل» | إزالة الازدواج، تقليل 96% من الملفات، إغلاق 10 ثغرات تشغيلية |

---

## 12. الخطوات التالية المقترحة (مرتبة بالأثر)

1. **تشغيل البوابة على جهاز فيه Docker:** `tools/scripts/red.sh up 192.168.1.50` ثم `red.sh guard`.
   إن فشل حزم Flyway، طبّق بديل القسم 10.2.
2. **تدوير الأسرار القديمة** (Mongo/MinIO/Asterisk/TURN) قبل أي مشاركة للمستودع.
3. **restore drill** إلزامي: `red.sh backup` ثم استعادة على بيئة منفصلة.
4. **فصل `services/android` إلى وحدات Gradle** (`:core`, `:crypto`, `:feature:*`) — الخطوة التالية
   الطبيعية بعد فصل الطبقات، لأن ملفًا واحدًا بـ 54 ملف Kotlin ما زال يخالف حدود الوحدة.
5. **فتح `services/backend` على وحدات** (auth / messaging / social / pstn) بدل حزمة واحدة كبيرة.
6. **توليد فهارس Mongo من العقد** أو نقل فهارس إلى code-based migration عند نمو المخطط.
7. **إضافة فحص توقيع الصور (cosign) وتوليد SBOM** في CI بعد نجاح البناء.
8. **TLS أمام البوابة** (شهادات في `platform/proxy/certs/`) قبل أي استخدام خارج الشبكة الموثوقة.

---

## 13. ملحق: أوامر مرجعية

```bash
# العقد والبيئة
python3 tools/env/render_env.py --example        # تحديث القالب من المخطط
tools/scripts/red.sh env 192.168.1.50            # .env خاص (mode 600)

# التشغيل والتحقق
tools/scripts/red.sh guard --verbose
tools/scripts/red.sh config
tools/scripts/red.sh up 192.168.1.50
BUILD_ANDROID=1 tools/scripts/red.sh up 192.168.1.50

# البيانات
tools/scripts/red.sh backup
RED_RESTORE_CONFIRM=yes tools/scripts/restore.sh data/runtime/backups/<stamp>

# البناء المنفصل لكل طبقة
cd services/backend && gradle clean build
./gradlew :app:assembleDebug -PRED_SERVER_URL=http://192.168.1.50:80 --dependency-verification strict
cd services/admin-dashboard && npm ci && npm run build
cd services/media-sfu && npm ci && npm run check

# صورة التحقق الكاملة (كل الطبقات)
docker build . --file platform/containers/ci.Dockerfile
```
