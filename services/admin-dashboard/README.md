# services/admin-dashboard/ — لوحة الإدارة القانونية

> **الحالة:** نشط — تُبنى في CI وDocker Compose

## الوظيفة

تطبيق React 19 + TypeScript/Vite/Ant Design لموافقة الحسابات والأجهزة، ضبط صلاحية DINSTAR وحدودها، عرض الصحة وسجل التدقيق. يتصل فقط بواجهات backend عبر Nginx؛ لا يتصل بقواعد البيانات مباشرة.

## المحتوى

`src/` الصفحات وعميل API، الصورة في `platform/containers/admin.Dockerfile`، وتقديم SPA في `platform/proxy/dashboard.conf`؛ الـ SPA يستخدم مسارات نسبية `/api` و`/ws` فقط.

## العلاقة بباقي المشروع

- راجع [`../../settings.gradle.kts`](../../settings.gradle.kts) لمعرفة ما يدخل البناء فعلًا؛ وجود المصدر لا يعني أنه مفعّل.
- الحدود القانونية موثقة في [`../../docs/W0_MODULE_BOUNDARIES.md`](../../docs/W0_MODULE_BOUNDARIES.md).
- خريطة النظام الكاملة في [`../../docs/01-PROJECT-OVERVIEW.md`](../../docs/01-PROJECT-OVERVIEW.md).

## التحقق

لا تُعلن ميزة هذا المجلد مكتملة إلا إذا دخلت بوابة البناء المناسبة واختبار runtime/جهازها. أسرار `.env` و`secrets/` ومفاتيح Android الخاصة لا تُحفظ في Git.
