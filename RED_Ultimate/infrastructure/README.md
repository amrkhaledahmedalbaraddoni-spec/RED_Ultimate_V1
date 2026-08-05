# `infrastructure/` — سكربتات التهيئة

سكربت إعداد البيئة بعد تشغيل المنظومة لأول مرة.

---

## 📁 المحتوى

```
infrastructure/
└── setup-env.sh
```

## `setup-env.sh` — ماذا يفعل؟
1. **MinIO**: عبر عميل `mc`:
   - إضافة alias محلي `http://localhost:9000` (admin/password)
   - إنشاء الدلائل: `red-media` و `red-backups`
   - جعل `red-media` **عام (public)**
2. **PostgreSQL**: إنشاء قاعدة البيانات `red_sovereign`
3. رسالة تأكيد

---

## 🚀 الاستخدام
```bash
chmod +x setup-env.sh
./setup-env.sh        # بعد تشغيل docker-compose
```

## ⚠️ ملاحظات
- الاعتمادات هنا (`admin/password`) **لا تطابق** قيم compose الافتراضية (`redadmin/redsecret123`) — يحتاج ضبطًا قبل الاستخدام
- يفترض اسم خدمة Postgres هو `db` بينما compose يستخدم `db-postgres`
