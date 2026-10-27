# services/backend/ — خادم RED القانوني

> **الحالة:** نشط — Spring Boot/Kotlin/JVM 21

## الوظيفة

يوفر التسجيل دون هاتف، الموافقة الإدارية، JWT/refresh، شهادات الأجهزة، دليل الهوية وPQXDH، الرسائل، المنشورات، المجموعات، الوسائط والحالات، سجل المكالمات، وصلاحيات DINSTAR. يستخدم PostgreSQL وMongoDB وRedis وMinIO.

## المحتوى

`src/main/kotlin/com/red/server/` المصدر، `src/test/` اختبارات. المخطط (`V1..V13`) يعيش في `../../data/sql/migrations` ويُحزَّم وقت البناء؛ الصورة في `platform/containers/backend.Dockerfile`.

## العلاقة بباقي المشروع

- راجع [`../../settings.gradle.kts`](../../settings.gradle.kts) لمعرفة ما يدخل البناء فعلًا؛ وجود المصدر لا يعني أنه مفعّل.
- الحدود القانونية موثقة في [`../../docs/W0_MODULE_BOUNDARIES.md`](../../docs/W0_MODULE_BOUNDARIES.md).
- خريطة النظام الكاملة في [`../../docs/01-PROJECT-OVERVIEW.md`](../../docs/01-PROJECT-OVERVIEW.md).

## التحقق

لا تُعلن ميزة هذا المجلد مكتملة إلا إذا دخلت بوابة البناء المناسبة واختبار runtime/جهازها. أسرار `.env` و`secrets/` ومفاتيح Android الخاصة لا تُحفظ في Git.
