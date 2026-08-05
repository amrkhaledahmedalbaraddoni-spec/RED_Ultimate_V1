# `build-logic/` — منطق البناء القابل لإعادة الاستخدام

وحدات **Gradle Build Plugins** (معرفات `signal.*`) — تنظم كل قواعد البناء المشتركة في مكان واحد بدل تكرارها في كل وحدة. بنية **Signal الأصلية**.

---

## 📁 البنية

```
build-logic/
├── build.gradle.kts      ← الجذر (versionCatalog من gradle/)
├── settings.gradle.kts   ← include("plugins"), include("tools")
├── plugins/              ← بلجن الـ Gradle (src/main/java)
│   ├── ktlint.gradle.kts                 ← بلجن ktlint (فحص الكود)
│   ├── signal-library.gradle.kts         ← البلجن الأساسي للمكتبات
│   ├── signal-sample-app.gradle.kts      ← بلجن تطبيقات العينات (demo/)
│   ├── licenses.gradle.kts + Licenses.kt ← توليد قائمة التراخيص/الاعتماديات
│   ├── translations.gradle.kts           ← تنزيل ملفات الترجمة من Smartling
│   ├── dependency-verification.gradle.kts ← توليد verification-metadata.xml
│   └── UpdateVerificationMetadataTask.kt ← مهمة تحديث التحقق من التبعيات
└── tools/                ← أدوات Java مساعدة (org.signal.buildtools)
    ├── SmartlingClient.kt        ← عميل API لترجمة Smartling
    └── StaticIpResolver.kt       ← حل أسماء نطاقات → IPs ثابتة (للنسخة القابلة للتكرار)
```

---

## 🧩 البلجنات
| المعرف | التطبيق على | ماذا يفعل |
|---|---|---|
| `signal.library` | `core/*`, `lib/*` | إعدادات Kotlin/JVM المشتركة + معالجة الترجمات |
| `signal.sample-app` | `demo/*` | تكوين تطبيقات العينات |
| `signal.ktlint` | وحدات Kotlin | تطبيق ktlint |
| `signal.licenses` | التطبيق | توليد قائمة التراخيص المضمّنة |
| `signal.translations` | التطبيق | تنزيل الترجمات من Smartling أثناء البناء |
| (dependency-verification) | الجذر | تحديث `gradle/verification-metadata.xml` |

## 🛠️ `tools/` — أدوات البناء
| الأداة | الوظيفة |
|---|---|
| `StaticIpResolver` | يستبدل أسماء النطاقات بـ IP ثابت في الملفات النهائية (جزء من إعادة الإنتاج القابلة للتكرار) |
| `SmartlingClient` | تحميل ملفات الترجمة من منصة Smartling (عبر API + اختبارات) |

---

## 🔗 العلاقة
- يقرأ الإصدارات من `gradle/libs.versions.toml`
- يستخدمه: `app/`, `feature/*`, `lib/*`, `core/*`, `demo/*`
- يعمل مع `reproducible-builds/` (أداة StaticIP جزء من إعادة الإنتاج)
