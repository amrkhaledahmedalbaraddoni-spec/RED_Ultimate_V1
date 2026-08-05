# `baseline-profile/` — مولّد تحسينات بدء التشغيل

وحدة تولّد **Baseline Profiles** تلقائيًا — ملفات AOT (تجميع مسبق) تسرّع بدء التشغيل والأداء العام للتطبيق (أسلوب Android/Google).

---

## 📁 المحتوى

```
baseline-profile/src/main/
├── AndroidManifest.xml
└── java/org/signal/baselineprofile/
    ├── BaselineProfileGenerator.kt   ← ⭐ يولّد الـ profile بقياس حقيقي
    └── BenchmarkSetup.kt             ← إعداد القياس (بدء التشغيل البارد)
```

## ⚙️ كيف يعمل؟
1. `BenchmarkSetup.kt` يجهّز قياس "بدء التشغيل البارد"
2. `BaselineProfileGenerator.kt` يشغّل التطبيق ويسجّل الفئات المُنفَّذة خلال البداية
3. النتيجة: ملف `baseline-prof.txt` تلقائي يُدمج في التطبيق

## 🚀 التشغيل
```bash
./gradlew :baseline-profile:generateBaselineProfile
# يتطلب جهازًا (نسخة release) — يولّد الملف ويعيد توليده عند تغيير الكود
```

## 🔗 العلاقة
- يُدمج في `app/` (لتحسين بدء التشغيل)
- يكمّل `benchmark/` و`microbenchmark/` (هذه تقيس، وهذه تحسّن)
