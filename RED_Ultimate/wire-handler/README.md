# `wire-handler/` — أداة توليد الكود (Wire Schema Handler)

أداة **build-time** مخصصة (وليس خدمة تشغيل) — خطوة معالجة بعد توليد كود Kotlin بواسطة مكتبة **Wire** (ProtoBuf من Square) أثناء بناء مشروع Signal/RED.

---

## 📁 المحتوى

```
wire-handler/
├── lib/                ← وحدة Kotlin JVM (الحاوية الفعلية)
├── settings.gradle.kts ← rootProject.name = "wire-handler", include("lib")
├── README.md           ← التوثيق الأصلي
└── wire-handler-1.0.0.jar  ← الحزمة المبنية (5.3 KB)
```

## ⚙️ ماذا يفعل؟

| الملف | الوظيفة |
|---|---|
| `lib/build.gradle.kts` | Kotlin JVM 2.2.20 + `wire-schema 6.4.0` |
| `Handler.kt` | كلاس `Handler : SchemaHandler()` — يفتح كل ملف `.kt` مولّد و**يستبدل بايتات `countNonNull` بـ `countNonDefa`** (نفس الطول عمدًا → استبدال في الموقع دون تغيير أحجام الملفات) |
| `Factory.kt` | `SchemaHandler.Factory` يعيد `Handler()` |

## 🎯 الغرض
تحايل ذكي لترقية دالة في الكود المولّد **دون إعادة توليد كامل** — يستخدمه `libsignal-service` عبر:
```kotlin
wire { schemaHandlerFactoryClass = "org.signal.wire.Factory" }
```

## 🛠️ البناء
```bash
./gradlew build
mv lib/build/libs/wire-handler-1.0.0.jar .
# ثم حدّث مسار الجرة في build.gradle عند تغيير الإصدار
```

---

## 🔗 العلاقة
- يُبنى مرة واحدة ويُحمل في `buildscript classpath` بالجذر (`build.gradle.kts`)
- يؤثر على كل الوحدات التي تولّد كود Wire: `libsignal-service`، `core/util`، `core/network`
