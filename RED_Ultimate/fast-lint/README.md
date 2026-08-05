# `fast-lint/` — أداة فحص سريع (خارجية)

محرك قواعد **Kotlin/Java AST** سريع — يستخدمه المطور يدويًا (عبر External Tool في IDE) لفحص ملف أو مجلد بكامل سياقه، مع **اختبارات وحدات** لكل قاعدة. مستقل تمامًا عن بناء التطبيق.

---

## 📁 المحتوى

```
fast-lint/
├── build.gradle.kts              ← بناء Java/Kotlin JVM
└── src/main/java/com/red/fastlint/
    ├── FastLint.kt               ← ⭐ نقطة الدخول (واجهة + محرك)
    ├── Lint.kt                   ← المحرك: يفحص نطاق ملف/مشروع
    ├── Finding.kt                ← نتيجة فحص (ملف + سطر + رسالة)
    ├── Rule.kt                   ← واجهة القاعدة (apply to AST)
    ├── AllRules.kt               ← تسجيل كل القواعد
    └── rules/
        ├── AlertDialogRule.kt            ← AlertDialog القديم
        ├── DatabaseReferenceRule.kt      ← استخدامات DB غير آمنة
        ├── ForegroundServiceRule.kt      ← خدمات أمامية خاطئة
        ├── LogNotSignalRule.kt           ← Log عادي بدل SignalLog
        ├── LogTagInlinedRule.kt          ← TAG داخل السطر (تضخيم)
        ├── StringResourceEscapingRule.kt ← رسائل بدون strings.xml
        └── VersionCodeRule.kt            ← رقم إصدار يدوي
```

## 🧪 الاختبارات (`src/test/`)
قاعدة لكل ملف اختبار: `AlertDialogRuleTest`, `DatabaseReferenceRuleTest`, `ForegroundServiceRuleTest`, `LogNotSignalRuleTest`, `LogTagInlinedRuleTest`, `StringResourceEscapingRuleTest`, `VersionCodeRuleTest` + `TestSupport.kt`.

## 🚀 الاستخدام
```bash
./gradlew test    # تشغيل اختبارات القواعد
# داخل Android Studio: External Tools → تشغيل FastLint على الملف المفتوح
```

## 🔗 العلاقة
- **أداة تطوير شخصية** — لا تدخل في سلسلة البناء (عكس `lintchecks/` الذي يُدمج تلقائيًا)
- قواعدها تشبه `lintchecks/` لكن أسرع وأخف (فحص ملف واحد)
