# `gradle/` — ملفات إصدارات التبعيات

ملفات تعريف **الإصدارات والنسخ** (Version Catalogs) — المركز الذي يُدار منه كل إصدارات المكتبات والأدوات، **مقسمة حسب الغرض**.

---

## 📁 الملفات

| الملف | الغرض |
|---|---|
| `libs.versions.toml` | **الرئيسي** — إصدارات التطبيق والوحدات الأساسية |
| `test-libs.versions.toml` | إصدارات مكتبات الاختبار |
| `lint-libs.versions.toml` | إصدارات أدوات الـ Lint |
| `benchmark-libs.versions.toml` | إصدارات وحدات قياس الأداء |
| `verification-metadata.xml` | تحقق Gradle من نزاهة الاعتماديات (dependency verification) |

## 📋 `libs.versions.toml` — سجل الإصدارات
أقسام:
- **Versions**: `agp = 9.2.1`, `kotlin = 2.2.20`, `kotlinx-coroutines`, `room = 2.8.2`, `hilt = 2.57.2`, `libsignal = 0.64.3`, `wire = 6.4.0`, `navigation-compose`, `ktor = 3.3.3`, `jackson = 2.18.0`, `minSdk = 30`, `targetSdk = 36`, `compileSdk = 37`, إلخ.
- **Libraries**: كل تبعية معرفة في فئات (androidx, compose, coroutines, google, signal, square...)
- **Plugins**: بلجن Gradle (android-application, kotlin-multiplatform, wire, kapt, ktlint, spotless...)

## 🔒 `verification-metadata.xml`
- تفعيل **التحقق من الاعتماديات** (Trusted keys + Ignored/Trusted artifacts) — طبقة أمان ضد هجمات سلسلة التوريد

---

## 🔗 العلاقة
- يقرؤها `settings.gradle.kts` و`build.gradle.kts` الجذري عبر `versionCatalogs` (كل ملف بفئة `libs`/`testLibs`/`lintLibs`/`benchmarkLibs`)
- `build-logic` يستخدم نفس التبعيات
