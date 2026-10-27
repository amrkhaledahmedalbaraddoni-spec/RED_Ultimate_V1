# 04 — التطبيقات

## التطبيق القانوني الوحيد

| المسار | الحالة | الاستخدام |
|---|---|---|
| `services/android/` (Gradle `:app`) | قانوني | بناء APK، اختبار الجهاز، الإصدار |

حزمة التطبيق: `com.red.sovereign`. الواجهة خمسة مقاصد: نبض RED (منشورات)، المحادثات،
مركز الإنشاء، سجل المكالمات الموحّد، وهاتف DINSTAR الذهبي (قناة منفصلة).

## البناء

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug \
  -PRED_SERVER_URL=http://SERVER_IP:80 \
  -PRED_TARGET_ABI=arm64-v8a \
  --dependency-verification strict
```

عنوان الخادم يُحزم وقت البناء في `BuildConfig.RED_SERVER_URL`؛ لا يوجد إعداد خادم في وقت التشغيل.
التوقيع المحلي يستخدم `services/android/signing/red-debug.p12` (مفتاح تصحيح عام فقط)؛
الإصدار يتطلب مفتاحًا خاصًا خارجيًا.

## ملكية الكود داخل التطبيق

| الحزمة | المسؤولية |
|---|---|
| `com.red.sovereign/auth` | تسجيل، انتظار الموافقة، شهادة الجهاز |
| `com.red.sovereign/crypto` | هوية libsignal، PreKeys، تشفير السجلات بـ Keystore |
| `com.red.sovereign/core` | الشبكة، الجلسات، التسليم، القوالب |
| `com.red.sovereign/social` · `stories` · `groups` | المنشورات والحالات والمجموعات |
| `com.red.sovereign/calls` | WebRTC + إشارات `/ws/calls` |
| `com.red.sovereign/media` | الرفع المصادق إلى MinIO والضغط |
| `com.red.sovereign/ui` · `theme` | هوية AQYAL العربية |

## المصادر التاريخية (خارج المستودع)

`app/` (فورك Signal)، `android/` (نموذج AQYAL)، `app-android/` (نموذج DevelopedChat)،
و`core/ lib/ feature/ demo/` ووحدات lint/benchmark **أُخرجت من الشجرة** ضمن فصل الطبقات.
سبب الإخراج: كانت 9,700+ ملف خارج `settings.gradle.kts`، تخلق جذرًا ثانيًا وتشويشًا على
حدود الوحدة. تبقى في تاريخ Git وتُستخرج منها الملفات المفيدة **بمراجعة ترخيص** ثم تُنقل
مع اختبارات إلى `services/android/`.

## بوابات لا تُتجاوز

- لا تُعلن ميزة مكتملة قبل بنائها وتشغيلها على جهاز حقيقي.
- E2EE يحتاج هاتفين وشبكتين لاختبار TURN فعليًا.
- PSTN يحتاج عتاد DINSTAR وشريحة يمنية فعلية.
- كل ملف يُنقل من الأرشيف يحتاج إدخالًا في سجل الترخيص/الاعتماديات.
