# تشغيل RED محليًا — النسخة التجريبية الأولى

هذه نسخة **تجريبية محلية وليست إصدار إنتاج**. بوابة البناء تتحقق من backend وAndroid
ولوحة الإدارة وSFU وإعداد Asterisk، لكن WebRTC على هاتفين وDINSTAR الفيزيائي
ما زالا يحتاجان اختبار أجهزة.

## المتطلبات

- جهاز بذاكرة 16GB مفضلة (8GB حد أدنى، وقد يكون بناء Android بطيئًا).
- مساحة فارغة 25GB تقريبًا لأول تنزيل للصور وAndroid SDK.
- Docker Desktop على Windows، أو Docker Engine + Compose v2 على Linux.
- تخصيص 6GiB على الأقل لـ Docker (8GiB موصى بها).
- OpenSSL على Linux/macOS؛ على Windows يبحث السكربت في Git for Windows ثم يستخدم
  حاوية Alpine مؤقتة تلقائيًا إن لم يجده.
- عنوان IPv4 ثابت لجهاز الخادم في الشبكة المحلية، مثل `192.168.1.50`.
- Python 3.9+ (لتوليد البيئة وفاحص الفصل؛ لا يحتاج أي حزمة خارجية).

## Linux / macOS — الطريق الواحد

```bash
cd RED_Ultimate_V1

# 1) بيئة خاصة + مفاتيح سلطة الهوية (تُنشأ مرة واحدة ولا تُستبدل)
tools/scripts/red.sh env 192.168.1.50

# 2) بناء وتشغيل مع انتظار صحة حقيقية
BUILD_ANDROID=1 tools/scripts/red.sh up 192.168.1.50

# أو بلا بناء APK (أسرع)
tools/scripts/red.sh up 192.168.1.50
```

`tools/scripts/local-first-run.sh 192.168.1.50` غلاف لنفس الأمر.

## Windows PowerShell

```powershell
cd RED_Ultimate_V1
Set-ExecutionPolicy -Scope Process Bypass
.\tools\scripts\local-first-run.ps1 -ServerIp 192.168.1.50 -BuildAndroid
```

## ما يفعله التشغيل

1. يتحقق من Docker وCompose وPython وOpenSSL.
2. يولّد `platform/config/.env` بأسرار عشوائية من `contracts/env.schema.json`
   (mode 600، غير محفوظ في Git)، ولا يستبدل ملفًا موجودًا.
3. ينشئ سلطة هوية ECDSA P-256 محلية في `platform/secrets/` دون استبدال مفاتيح موجودة.
4. يشغّل `docker compose config --quiet` قبل أي بناء.
5. يبني ويشغّل 13 خدمة (PostgreSQL، MongoDB، Redis، MinIO، backend، SFU، TURN، Asterisk،
   لوحة الإدارة، Nginx، Prometheus، Grafana، MinIO bootstrap).
6. ينتظر `/health` و`/sfu-health` فعليًا بدل ادعاء نجاح مبكر.
7. عند `BUILD_ANDROID=1` يبني صورة التحقق ويستخرج `local-artifacts/red-app-debug.apk`
   مضبوطًا على عنوان الخادم.

## الوصول

```text
لوحة الإدارة: http://IP-الخادم/
حالة backend: http://IP-الخادم/health
حالة SFU:     http://IP-الخادم/sfu-health
MinIO Console: http://127.0.0.1:9001  (محلي فقط)
```

استخدم `RED_HTTP_PORT` في `platform/config/.env` إن كان المنفذ 80 محجوزًا.

## بعد التشغيل

```bash
tools/scripts/red.sh guard          # الطبقات مفصولة ومتكاملة
tools/scripts/red.sh ps             # حالة الحاويات
tools/scripts/red.sh logs backend   # سجلات
tools/scripts/red.sh backup         # نسخة احتياطية إلى data/runtime/backups
```

أول حساب يسجّل من التطبيق يظهر `PENDING` في لوحة الإدارة؛ لا يعمل قبل **Approve**.

## تثبيت APK

```bash
adb install -r local-artifacts/red-app-debug.apk
```

## عند الشبكات البطيئة

```powershell
.\tools\scripts\prefetch-android-crypto.ps1   # تحميل موثّق SHA-256 إلى local-maven/
```

محتوى `local-maven/` مستثنى من Git، ولا يُقبل أي artifact لا يطابق checksums المثبتة.

## استكشاف الأخطاء

| العَرَض | السبب المرجّح | الحل |
|---|---|---|
| `DB_PASSWORD is required` | لا يوجد `.env` صالح | `tools/scripts/red.sh env 192.168.1.50` |
| backend غير صالح | مخطط/قاعدة لم تُهيأ | `tools/scripts/red.sh logs backend` |
| شاشة بيضاء في اللوحة | SPA لا يصل لـ `/api` | تحقق من Nginx (`/nginx-health`) |
| فشل `separation guard` | ملف في الطبقة الخطأ | اقرأ الرسالة؛ كل خرق يشير للملف والقاعدة |
| تعذر الاتصال من الهاتف | عنوان الخادم مختلف | أعد بناء APK بنفس `RED_SERVER_URL` |
