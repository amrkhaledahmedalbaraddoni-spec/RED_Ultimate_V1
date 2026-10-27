# data/ — طبقة البيانات (Data plane)

> **القاعدة:** المخطط والبذور والعيّنات وسجلات النسخ الاحتياطي فقط.
> لا كود تطبيقي ولا إعداد تشغيل هنا.

```text
data/
├── sql/migrations/     Flyway V1…V13 — المصدر الوحيد للمخطط (append-only)
├── sql/schema/         master-schema.sql — مرجع قراءة فقط (لا يُطبَّق)
├── mongo/init.js       فهارس وTTL عند أول تهيئة MongoDB
├── seeds/              بذور تُطبَّق بقرار مشغّل (ليست تلقائية)
├── fixtures/           عيّنات للتطوير/الاختبار فقط (لا تدخل أي صورة إصدار)
└── runtime/            مخرجات النسخ الاحتياطي محليًا (غير محفوظة في Git)
```

## كيف تتكامل مع الخدمة دون نسخ

| البيانات | يملكها | تُستهلك كيف |
|---|---|---|
| `sql/migrations` | هذه الطبقة | `services/backend` يحزمها وقت البناء → Flyway `classpath:migrations` |
| `mongo/init.js` | هذه الطبقة | bind mount في `db-mongo` عند أول تهيئة فقط |
| `fixtures/*` | هذه الطبقة | تُقرأ من الاختبارات/العروض، لا تُحزَّم في الصور |
| `seeds/*` | هذه الطبقة | يُطبّقها المشغّل يدويًا |
| `runtime/backups/*` | هذه الطبقة | ناتج `tools/scripts/backup.sh` |

## قواعد صارمة

1. لا تُعدَّل مهاجرة مُطبَّقة؛ أضف `V14__…` وما بعده.
2. لا تُنسخ `*.sql` داخل `services/` (فاحص الفصل + مهمة `verifyDataPlaneOwnership` يفشلان).
3. لا تُركَّب مهاجرات في `docker-entrypoint-initdb.d`؛ Flyway هو مالك المخطط.
4. لا تُحفظ نسخ احتياطية أو مفاتيح في Git (`data/runtime/` مستثنى).
5. أول مسؤول يُنشأ من `RED_ADMIN_USERNAME/PASSWORD` عبر الخدمة، وليس كبذرة SQL.

انظر [`../contracts/data.json`](../contracts/data.json) لجدول الملكية والنسخ الاحتياطي.
