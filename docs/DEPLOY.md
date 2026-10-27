# DEPLOY — نشر ونسخ احتياطي

## 1. التحضير

```bash
tools/scripts/red.sh env 192.168.1.50     # يولّد platform/config/.env وأسرارًا عشوائية
tools/scripts/red.sh key                  # مفاتيح سلطة الهوية (إن لم تُنشأ)
tools/scripts/red.sh guard                # الطبقات سليمة قبل أي نشر
```

## 2. التشغيل

```bash
docker compose --env-file platform/config/.env \
  -f platform/compose/docker-compose.yml up -d --build
```

أو عبر الغلاف الذي ينتظر الصحة فعليًا: `tools/scripts/red.sh up 192.168.1.50`.

## 3. بناء APK للإصدار

- المسار القانوني: `services/android/` (Gradle `:app`).
- البناء الموثّق الكامل: `platform/containers/ci.Dockerfile` (dependency verification صارم).
- التوقيع: مفتاح إصدار خاص خارج المستودع — `red-debug.p12` للتصحيح المحلي فقط.

## 4. البيانات

- المخطط: `data/sql/migrations` (Flyway يطبّقه عند إقلاع backend).
- الفهارس: `data/mongo/init.js` (أول تهيئة فقط).
- النسخ: `tools/scripts/backup.sh` → `data/runtime/backups/<UTC>`.
- الاستعادة: `RED_RESTORE_CONFIRM=yes tools/scripts/restore.sh data/runtime/backups/<UTC>`.
- **لا تُعتبر النسخة صالحة قبل restore drill على بيئة منفصلة.**

## 5. قبل أي تعرّض للشبكة العامة

1. TLS أمام البوابة (شهادات في `platform/proxy/certs/`) وإغلاق `RED_HTTP_PORT` الخارجي.
2. تدوير كل الأسرار القديمة (كانت مكشوفة في تاريخ المستودع).
3. مراجعة منافذ `platform/compose/docker-compose.yml`: لا تُنشر 5038 (AMI) أبدًا.
4. التحقق من أن كل دلو MinIO خاص (`mc anonymous get local/red-media`).
