# RED_Ultimate_V1 — التوثيق الشامل للمستودع

هذا الملف هو **نقطة البداية** لفهم مستودع `RED_Ultimate_V1` بالكامل. المشروع الفعلي موجود داخل مجلد `RED_Ultimate/`، وتجد في كل مجلد فرعي ملف شرح (`README.md`) يوضح محتواه.

---

## 📋 نظرة عامة

**RED_Ultimate** هو **منظومة مراسلة ومكالمات "سيادية"** تعمل محليًا (دون الاعتماد على الإنترنت الخارجي أو خدمات جوجل)، مبنية على **فورك كامل من تطبيق Signal-Android** مع إعادة تسمية شاملة من `org.thoughtcrime.securesms` إلى `com.red.sovereign`.

### الأنظمة الثلاثة (الفكرة المركزية للمشروع)
| النظام | الوظيفة | التقنية |
|---|---|---|
| **System A** | مكالمات صوت/فيديو عالية الجودة (VoIP) | WebRTC + خادم وسائط Mediasoup |
| **System B** | مكالمات هاتف GSM/PSTN | بوابة DINSTAR UC2000 + Asterisk PBX |
| **System C** | رسائل مشفرة بتوصيل مضمون | WebSocket + Protobuf + Room + MongoDB/Redis |

### الميزات الرئيسية
- تسجيل المستخدمين بموافقة إدارية (Approval Flow)
- قصص تنتهي خلال 24 ساعة
- رسائل ذاتية التدمير (Burn)
- مزامنة الرسائل المفقودة (Sequence Sync)
- لوحة تحكم إدارية (React) لمراقبة النظام والموافقات و KILL SWITCH
- رفع الوسائط إلى MinIO
- مكالمات عبر شرائح GSM يمنية (يمن موبايل / سبأفون)

---

## 🗂️ محتويات جذر المستودع

| الملف/المجلد | الوصف |
|---|---|
| `RED_Ultimate/` | **المشروع الكامل** (انظر الفهرس أدناه) |
| `.github/workflows/` | سير عمل GitHub Actions (Deno lint + Docker build) |
| `.env` / `.env.example` | متغيرات البيئة المطلوبة (DB_PASSWORD، AMI_PASSWORD، TURN_SECRET، REDIS_PASSWORD) |
| `FINAL_SUMMARY.md` | الملخص النهائي للمشروع |
| `TECHNICAL_REPORT_AR.md` | التقرير الفني المفصل (بالعربية) — يتضمن نقاط الضعف وخطط الإصلاح |
| `VERIFICATION_REPORT_AR.md` | تقرير التحقق والاختبار |
| `declared_deps.txt` | قائمة الاعتماديات المعلنة |
| `imports_list.txt` / `used_imports.txt` | قوائم الاستيراد والاستخدام الشاملة |
| `image-search/` | صور أيقونات التطبيق المقترحة (Royal Gold & Obsidian) |

---

## 📚 فهرس التوثيقات (اقرأ بالترتيب)

### 0. التوثيقات الشاملة (docs/)
- [`RED_Ultimate/docs/01-PROJECT-OVERVIEW.md`](RED_Ultimate/docs/01-PROJECT-OVERVIEW.md) — **المشروع كاملًا**: الرؤية، الأنظمة الثلاثة، الخريطة المعمارية
- [`RED_Ultimate/docs/02-DATABASES.md`](RED_Ultimate/docs/02-DATABASES.md) — **قواعد البيانات**: PostgreSQL، MongoDB، Redis، MinIO + قواعد الجهاز + تدفق رسالة
- [`RED_Ultimate/docs/03-SERVER-ADMIN-PANEL.md`](RED_Ultimate/docs/03-SERVER-ADMIN-PANEL.md) — **السيرفر ولوحة الإدارة**: كل الـ API والـ WebSockets + تدفق البيانات
- [`RED_Ultimate/docs/04-APPS.md`](RED_Ultimate/docs/04-APPS.md) — **التطبيقات الثلاثة**: app/android/app-android ومقارنتها

### 1. المشروع الرئيسي
- [`RED_Ultimate/README.md`](RED_Ultimate/README.md) — قلب المشروع: نظام البناء، الوحدات، والأنظمة الثلاثة

### 2. تطبيقات Android
- [`RED_Ultimate/app/README.md`](RED_Ultimate/app/README.md) — التطبيق الرئيسي الكامل (Signal fork + RED)
- [`RED_Ultimate/android/README.md`](RED_Ultimate/android/README.md) — النسخة السيادية البديلة (ثيم AQYAL الذهبي)
- [`RED_Ultimate/app-android/README.md`](RED_Ultimate/app-android/README.md) — نسخة DevelopedChat الأقدم

### 3. الخوادم
- [`RED_Ultimate/backend-server/README.md`](RED_Ultimate/backend-server/README.md) — الخادم الرئيسي (Spring Boot)
- [`RED_Ultimate/server/README.md`](RED_Ultimate/server/README.md) — الخادم المصغّر (قشرة قديمة)

### 4. لوحات التحكم
- [`RED_Ultimate/admin_dashboard/README.md`](RED_Ultimate/admin_dashboard/README.md) — اللوحة الكاملة النشطة (React + Ant Design)
- [`RED_Ultimate/admin-dashboard/README.md`](RED_Ultimate/admin-dashboard/README.md) — نموذج قديم ثابت

### 5. البنية التحتية
- [`RED_Ultimate/media-sfu/README.md`](RED_Ultimate/media-sfu/README.md) — محرك مؤتمرات الفيديو (Mediasoup)
- [`RED_Ultimate/pstn-asterisk/README.md`](RED_Ultimate/pstn-asterisk/README.md) — بوابة الهاتف (Asterisk + Dinstar)
- [`RED_Ultimate/infrastructure/README.md`](RED_Ultimate/infrastructure/README.md) — سكربتات التهيئة
- [`RED_Ultimate/reproducible-builds/README.md`](RED_Ultimate/reproducible-builds/README.md) — البناء القابل للتكرار
- [`RED_Ultimate/wire-handler/README.md`](RED_Ultimate/wire-handler/README.md) — أداة توليد الكود (Wire)

### 6. وحدات الكود
- [`RED_Ultimate/core/README.md`](RED_Ultimate/core/README.md) — المكتبات الأساسية (7 وحدات)
- [`RED_Ultimate/lib/README.md`](RED_Ultimate/lib/README.md) — مكتبات Signal الأصلية (18 وحدة)
- [`RED_Ultimate/feature/README.md`](RED_Ultimate/feature/README.md) — وحدات الميزات (كاميرا، إرسال وسائط، تسجيل الحساب)
- [`RED_Ultimate/demo/README.md`](RED_Ultimate/demo/README.md) — التطبيقات التجريبية (12)
- [`RED_Ultimate/shared-proto/README.md`](RED_Ultimate/shared-proto/README.md) — البروتوكول المشترك (Protobuf)
- [`RED_Ultimate/gradle/README.md`](RED_Ultimate/gradle/README.md) — نظام البناء (Version Catalog)
- [`RED_Ultimate/build-logic/README.md`](RED_Ultimate/build-logic/README.md) — البلجنات المخصصة للبناء

### 7. الجودة والأداء
- [`RED_Ultimate/lintchecks/README.md`](RED_Ultimate/lintchecks/README.md) — فحوصات Lint مخصصة
- [`RED_Ultimate/fast-lint/README.md`](RED_Ultimate/fast-lint/README.md) — مدقق AST السريع
- [`RED_Ultimate/benchmark/README.md`](RED_Ultimate/benchmark/README.md) — قياسات الأداء الشاملة
- [`RED_Ultimate/microbenchmark/README.md`](RED_Ultimate/microbenchmark/README.md) — القياسات الدقيقة
- [`RED_Ultimate/baseline-profile/README.md`](RED_Ultimate/baseline-profile/README.md) — تحسينات بدء التشغيل

---

## 🔄 كيف تعمل المنظومة معًا (باختصار)

```
    هاتف المستخدم (تطبيق RED)
              │
      nginx (:80) ─── بوابة الدخول
      ┌────────────┼─────────────┐
      ▼            ▼             ▼
admin_panel   backend-server  /ws/ (WebSocket)
(لوحة التحكم)  (Spring Boot)   الرسائل اللحظية
                  │
   ┌──────┬───────┼───────┬─────────┬──────────┐
   ▼      ▼       ▼       ▼         ▼          ▼
PostgreSQL MongoDB Redis MinIO  media-sfu  pstn-gateway
(المستخدمون) (الرسائل) (كاش/إشعار) (وسائط)  (Asterisk)
                                        │
                                    DINSTAR UC2000
                                    (شرائح GSM اليمنية)
```

---

## ⚠️ ملاحظات مهمة (من التقرير الفني)
1. المشروع **غير قابل للبناء حاليًا** بالكامل — يوجد فئات مرجعية مفقودة ووحدات غير مسجلة في `settings.gradle.kts`
2. بعض الميزات المعلنة (4K/AV1، التشفير الكمومي) تنفيذها حاليًا محاكاة/Placeholder
3. الأمان يحتاج تقوية: `/api/**` مفتوح حاليًا وجميع الأسرار افتراضية
4. توجد نسخ متعددة لنفس المفاهيم (`android/` و`app-android/` و`app/`) — قرار معماري معلّق

للتفاصيل الكاملة راجع: `TECHNICAL_REPORT_AR.md` و `VERIFICATION_REPORT_AR.md`.
