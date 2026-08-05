# `server/` — الخادم المصغّر (قشرة قديمة)

نسخة مصغّرة/تجريبية من مفاهيم الخادم الرئيسي — **5 ملفات فقط**، بدون `build.gradle.kts` (لا يمكن بناؤه كوحدة مستقلّة كما هو). يبدو كنموذج توثيقي مبكر لنفس أفكار `backend-server`.

---

## 📁 المحتوى

```
server/src/main/kotlin/com/red/server/
├── RedMasterServer.kt            ← نقطة الدخول (منفذ 8443 + SSL)
├── auth/RedApprovalService.kt    ← نسخة مطابقة لـ backend-server
├── delivery/DeviceManager.kt     ← تسجيل أجهزة متعددة لكل مستخدم
└── security/SecurityController.kt ← POST /api/admin/security/kill-switch/{userId}
```

| الملف | الوظيفة |
|---|---|
| `RedMasterServer.kt` | نقطة الدخول: يطبع شعار "POST-QUANTUM READY" (شعار تسويقي) |
| `auth/RedApprovalService.kt` | نفس منطق الموافقات (ConcurrentHashMap) |
| `delivery/DeviceManager.kt` | **مفهوم multi-device sync**: `ConcurrentHashMap<UserId, Set<DeviceId>>` |
| `security/SecurityController.kt` | Kill Switch (طباعة فقط — لا تنفيذ) |
| `application.properties` | منفذ **8443** مع SSL (keystore `red-keystore.p12` **مفقود**) |

---

## ⚠️ حالة الاكتمال
- **بدون build.gradle.kts** — لا يمكن بناؤه
- **SSL معطّل عمليًا** — الـ keystore غير موجود
- **كل الوظائف طباعة/محاكاة** — لا منطق حقيقي
- **الخلاصة**: نسخة قديمة تجريبية — `backend-server/` هو الخادم الحقيقي المعتمد

## 🔗 العلاقة
- يحمل نفس مفاهيم `backend-server` (الموافقة، Kill Switch) — يمكن حذفه أو استخدامه كمرجع
