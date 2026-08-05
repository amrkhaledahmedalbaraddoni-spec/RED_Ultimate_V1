# `benchmark/` — قياسات أداء التطبيق

وحدة **Android Benchmark** (Macrobenchmark) لقياس أداء التطبيق في سيناريوهات حقيقية (بدء التشغيل، المعالجة، البحث) — حزم `org.thoughtcrime.benchmark` (هوية Signal).

---

## 📁 المحتوى

```
benchmark/src/main/
├── AndroidManifest.xml     ← بدون Activity (وحدة قياس)
└── java/org/thoughtcrime/benchmark/
    ├── BenchmarkSetup.kt               ← تهيئة القياس العامة
    ├── BenchmarkMetrics.kt             ← تعريف المقاييس (Cold Startup, Frames, Memory)
    ├── StartupBenchmarks.kt            ← ⭐ بدء التشغيل البارد (Cold Start) — الأنسب لتقييم تحسينات
    ├── ConversationBenchmarks.kt       ← فتح محادثة والتنقل فيها
    ├── MessageProcessingBenchmarks.kt  ← معالجة الرسائل الواردة
    ├── GroupMessageProcessingBenchmarks.kt ← معالجة رسائل المجموعات
    ├── SearchBenchmarks.kt             ← البحث في المحادثات
    ├── ThreadDeletionBenchmarks.kt     ← حذف المحادثات
    └── UIDeviceExt.kt                  ← أدوات UI للـ benchmark
```

## 🧪 الاختبارات
| الاختبار | يقيس |
|---|---|
| `StartupBenchmarks` | زمن بدء التشغيل البارد (Cold Startup) |
| `ConversationBenchmarks` | سلاسة فتح/تصفح المحادثة (Frames) |
| `MessageProcessingBenchmarks` | سرعة معالجة الرسائل الواردة |
| `GroupMessageProcessingBenchmarks` | معالجة رسائل المجموعات الكبيرة |
| `SearchBenchmarks` | أداء البحث في قوائم كبيرة |
| `ThreadDeletionBenchmarks` | أداء حذف المحادثات |

## 🚀 التشغيل
```bash
./gradlew :benchmark:connectedBenchmarkAndroidTest
# يتطلب جهازًا معياريًا (root + نسخة release + إغلاق التطبيقات الأخرى)
```

## 🔗 العلاقة
- يستخدم `:app` كـ target
- مكمّل لـ `microbenchmark/` (قياسات دقيقة) و`baseline-profile/` (تحسينات بدء التشغيل)
