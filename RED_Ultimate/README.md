# RED_Ultimate — التوثيق الكامل للمشروع الرئيسي

> هذا الملف يشرح المشروع بالكامل. كل مجلد فرعي يحتوي `README.md` خاصًا به يشرح محتواه.

---

## 🏗️ ما هو هذا المجلد؟

هذا هو **قلب مشروع RED_Ultimate** — منظومة مراسلة ومكالمات "سيادية" تعمل محليًا (بدون إنترنت خارجي)، مبنية على **فورك كامل من Signal-Android** (~3,800 ملف مصدري) مع إعادة تسمية شاملة من `org.thoughtcrime.securesms` إلى `com.red.sovereign`.

تطبيق Android باسم **"RED"** — الإصدار `1.0.0-RED` — يحتوي الأنظمة الثلاثة:
- **System A**: مكالمات VoIP عالية الجودة (WebRTC + Mediasoup SFU)
- **System B**: مكالمات GSM/PSTN عبر بوابة DINSTAR UC2000 (شرائح يمنية)
- **System C**: رسائل E2E بتوصيل مضمون (WebSocket + Protobuf + Room)

---

## 📁 بنية المجلدات (24 مجلدًا)

### تطبيقات Android (3)
| المجلد | الوصف |
|---|---|
| [`app/`](app/README.md) | **التطبيق الرئيسي** — فورك Signal الكامل + طبقة RED Sovereign (38MB، 2540 ملف Kotlin) |
| [`android/`](android/README.md) | نسخة بديلة "سيادية" أصغر (Compose + Hilt، ثيم AQYAL الذهبي) — غير مسجلة في البناء |
| [`app-android/`](app-android/README.md) | نسخة "DevelopedChat" الأقدم مع نظام الموافقة الإدارية — غير مسجلة في البناء |

### الخوادم (2)
| المجلد | الوصف |
|---|---|
| [`backend-server/`](backend-server/README.md) | **الخادم الرئيسي** (Spring Boot 3.4 + Kotlin 2.1، منفذ 8080) — REST + WebSocket + تحكم Dinstar + MongoDB/Postgres/Redis |
| [`server/`](server/README.md) | خادم مصغّر قديم (SSL 8443، 5 ملفات فقط) |

### لوحات التحكم (2)
| المجلد | الوصف |
|---|---|
| [`admin_dashboard/`](admin_dashboard/README.md) | **اللوحة الكاملة النشطة** (React 19 + Ant Design + ECharts) |
| [`admin-dashboard/`](admin-dashboard/README.md) | نموذج قديم ثابت (2 ملفات، بيانات Mock) |

### البنية التحتية (5)
| المجلد | الوصف |
|---|---|
| [`media-sfu/`](media-sfu/README.md) | محرك مؤتمرات الفيديو (Mediasoup، منفذ 4000 + RTP 40000-40100) |
| [`pstn-asterisk/`](pstn-asterisk/README.md) | بوابة الهاتف (Asterisk PBX، SIP 5060 + AMI 5038) |
| [`infrastructure/`](infrastructure/README.md) | سكربت تهيئة MinIO + PostgreSQL |
| [`reproducible-builds/`](reproducible-builds/README.md) | بيئة بناء قابلة للتكرار (Docker + apkdiff) |
| [`wire-handler/`](wire-handler/README.md) | أداة توليد كود Wire المخصصة (build-time plugin) |

### وحدات الكود (7)
| المجلد | الوصف |
|---|---|
| [`core/`](core/README.md) | 7 وحدات أساسية (models, network, ui, util, serialization) |
| [`lib/`](lib/README.md) | 18 مكتبة Signal أصلية (libsignal-service الأكبر) |
| [`feature/`](feature/README.md) | 3 وحدات وظيفية (camera, media-send, registration) |
| [`demo/`](demo/README.md) | 12 تطبيقًا تجريبيًا (غير مضمنة في البناء) |
| [`shared-proto/`](shared-proto/README.md) | البروتوكول المشترك: ChatProtos + RedProtos |
| [`gradle/`](gradle/README.md) | نظام البناء (Version Catalogs + Verification) |
| [`build-logic/`](build-logic/README.md) | البلجنات المخصصة (signal-library, ktlint, licenses...) |

### الجودة والأداء (5)
| المجلد | الوصف |
|---|---|
| [`lintchecks/`](lintchecks/README.md) | فحوصات Android Lint مخصصة (8 كواشف) |
| [`fast-lint/`](fast-lint/README.md) | مدقق AST سريع (IntelliJ core) |
| [`benchmark/`](benchmark/README.md) | Macrobenchmark (7 سيناريوهات) |
| [`microbenchmark/`](microbenchmark/README.md) | قياسات دقيقة لـ libsignal-service |
| [`baseline-profile/`](baseline-profile/README.md) | تحسين بدء التشغيل |

---

## 🏗️ نظام البناء

| المكوّن | الإصدار |
|---|---|
| Gradle Wrapper | 9.4.1 |
| AGP (Android Gradle Plugin) | 9.2.1 |
| Kotlin | 2.2.20 |
| Compose BOM | 2026.06.01 |
| Hilt | 2.52 |
| libsignal-client | 0.99.1 |
| compileSdk / minSdk / targetSdk | 37 / 23 / 35 |
| Java/Kotlin target | 21 |

**الوحدات المسجلة في `settings.gradle.kts`**: `:app`، `:core:*` (7)، `:lib:*` (18)، `:feature:*` (3)، `:lintchecks`، `:fast-lint`، `:build-logic:tools`، `:benchmark`، `:microbenchmark`، `:shared-proto`.

**ملاحظة**: مجلدات `android/` و`app-android/` و`server/` و`backend-server/` و`admin_dashboard/` و`media-sfu/` و`pstn-asterisk/` **غير مسجلة** في بناء Gradle — تعمل كمشاريع مستقلة.

---

## 🔗 الملفات الجذرية الهامة

| الملف | الوظيفة |
|---|---|
| `docker-compose.yml` | تشغيل المنظومة كلها (10 خدمات) |
| `settings.gradle.kts` / `build.gradle.kts` | إعداد بناء المشروع |
| `gradlew` / `gradlew.bat` | مشغّل Gradle |
| `nginx.conf` | إعدادات الوكيل العكسي |
| `build-and-run.sh` | بناء وتشغيل Docker دفعة واحدة |
| `audit_check.py` | فحص اكتمال الأنظمة الثلاثة |
| `MASTER_GUIDE.md` | دليل ضبط جهاز DINSTAR UC2000 (عربي) |
| `MASTER_CHECKLIST.txt` | قائمة فحص المشروع |
| `lefthook.yml` | خطافات git (تنسيق قبل الدفع) |
| `pkcs11.config` | إعداد التشفير عبر بطاقة ذكية/HSM |

---

## ⚠️ الحالة الحالية

وفق التقرير الفني (`TECHNICAL_REPORT_AR.md`):
- **البنية والهيكل**: سليمة (إعادة تسمية كاملة، 30+ وحدة، نظام إصدارات)
- **البناء الكامل**: غير مكتمل — فئات مرجعية مفقودة بين الوحدات، أخطاء Manifest
- **الميزات**: بعضها محاكاة (4K/AV1، التشفير الكمومي، Kill Switch)
- **الأمان**: `/api/**` مفتوح، JWT غير مفعّل، أسرار افتراضية

---

## 🚀 التشغيل

```bash
# 1) تشغيل المنظومة (خوادم + قاعدة بيانات + لوحة تحكم)
./build-and-run.sh
# أو: docker-compose up -d --build

# 2) لوحة التحكم: http://localhost:80
# 3) الخادم:      http://localhost:8080
```

---

## 📚 وثائق إضافية
- [`MASTER_GUIDE.md`](MASTER_GUIDE.md) — دليل DINSTAR UC2000
- [`MASTER_CHECKLIST.txt`](MASTER_CHECKLIST.txt) — قائمة الفحص
- [`DEPLOY.md`](DEPLOY.md) — خطوات النشر
- [`CONTRIBUTING.md`](CONTRIBUTING.md) — إرشادات المساهمة

---

# (المحتوى الأصلي — README من فورك Signal)

RED is a simple, powerful, and secure messenger that uses your phone's data connection (WiFi/4G/5G) to communicate securely.

Millions of people use RED every day for free and instantaneous communication anywhere in the world. Send and receive high-fidelity messages, participate in HD voice/video calls, and explore a growing set of new features that help you stay connected.

RED's advanced privacy-preserving technology is always enabled, so you can focus on sharing the moments that matter with the people who matter to you.

## License
Copyright 2013 RED Messenger, LLC
Licensed under the GNU AGPLv3: https://www.gnu.org/licenses/agpl-3.0.html
