# `lintchecks/` — قواعد Lint مخصصة

مجموعة من **قواعد Android Lint مخصصة** (Detectors) تفحص الكود أثناء البناء — تكشف الأنماط الخطرة والأخطاء الشائعة في مشاريع Signal/RED. حزمة `org.signal.lint`.

---

## 📁 المحتوى

```
lintchecks/src/main/java/org/signal/lint/
├── Registry.kt                          ← ⭐ يسجل كل الـ Detectors للـ lint
├── AlertDialogBuilderDetector.kt        ← منع استخدام AlertDialog التقليدي
├── BlockingGetDetector.kt               ← منع استدعاءات .get() المحظورة على Flow/LiveData
├── CardViewDetector.kt                  ← منع استخدام CardView (بدل Compose)
├── RecipientIdDatabaseDetector.kt       ← منع تخزين RecipientId مباشرة في قاعدة البيانات
├── ThreadIdDatabaseDetector.kt          ← منع تخزين ThreadId مباشرة
├── SignalLogDetector.kt                 ← إجبار استخدام Log الخاصة بـ Signal بدل android.util.Log
├── SystemOutPrintLnDetector.kt          ← منع System.out.println في كود الإنتاج
├── StartForegroundServiceDetector.kt    ← منع سوء استخدام startForegroundService
└── VersionCodeDetector.kt               ← منع كتابة VersionCode يدويًا
```

## 🧩 القواعد
| القاعدة | تكشف |
|---|---|
| `BlockingGet` | حظر `blockingGet`/`.get()` على سلاسل التزامن (تجميد UI) |
| `SignalLog` | استخدام `android.util.Log` بدل `SignalLog` (إخفاء معلومات) |
| `SystemOutPrintLn` | طباعة `System.out` في الإنتاج |
| `AlertDialogBuilder` | AlertDialog قديم بدل Material/Compose |
| `CardView` | CardView بدل واجهات Compose الحديثة |
| `RecipientIdDatabase` / `ThreadIdDatabase` | تخزين معرفات داخلية في DB بصورة غير آمنة |
| `StartForegroundService` | استدعاءات خطرة للخدمات الأمامية |
| `VersionCode` | أرقام إصدار يدوية مكررة |

## 🚀 الاستخدام
```bash
# تلقائي أثناء أي build:
./gradlew lint
# أو للوحدة المحددة:
./gradlew :lintchecks:test    # اختبارات القواعد نفسها
```

## 🔗 العلاقة
- يضيفه `build-logic` (بلجن `signal.library`) تلقائيًا لكل الوحدات
- `fast-lint/` أداة منفصلة للاستخدام الشخصي داخل المحرر — هذه تُدمج في البناء
