# `microbenchmark/` — قياس الأداء الدقيق للبروتوكول

وحدة **Microbenchmark** لقياس أداء **بروتوكول Signal** نفسه (تشفير/فك، توليد مفاتيح) بدقة نانوثانية — بدون تشغيل التطبيق كاملًا.

---

## 📁 المحتوى

```
microbenchmark/src/
├── androidTest/
│   ├── AndroidManifest.xml
│   └── java/org/signal/
│       ├── microbenchmark/ProtocolBenchmarks.kt     ← ⭐ قياسات البروتوكول
│       └── util/
│           ├── InMemorySignalServiceAccountDataStore.kt  ← تخزين حساب في الذاكرة (بدون قرص)
│           └── SignalClient.kt                           ← عميل Signal للاختبار
└── main/AndroidManifest.xml
```

## 🧪 `ProtocolBenchmarks.kt` — ماذا يقيس؟
- عمليات بروتوكول Signal الفعلية: توليد المفاتيح، التشفير، فك التشفير
- يستخدم `InMemorySignalServiceAccountDataStore` — حساب كامل في الذاكرة (أسرع وأدق من القرص)

## 🚀 التشغيل
```bash
./gradlew :microbenchmark:connectedAndroidTest
# يتطلب جهازًا معياريًا (root + نسخة release)
```

## 🔗 العلاقة
- يكمل `benchmark/` (قياسات التطبيق الواسعة) — هذا يركز على **بروتوكول التشفير**
- `benchmark-proguard-rules.pro` لحماية أسماء الفئات أثناء R8
