# `lib/` — مكتبات Signal الأصلية (18 وحدة)

مكتبات عامة قابلة لإعادة الاستخدام — **شيفرة Signal الأصلية** (حزم `org.signal.*`) دون تعديل. توفر كل البنى المساعدة التي يعتمد عليها تطبيق RED.

---

## 🗂️ الوحدات (18)

| الوحدة | الغرض |
|---|---|
| **`:lib:libsignal-service`** | **الأكبر والأهم** — عميل خدمة Signal (JVM): كل بروتوكولات Signal في `src/main/protowire/` (SignalService.proto بـ 1019 سطر، Groups, StorageService, Provisioning, SVR2, CDSI, ResumableUploads). يستخدم Wire مع `schemaHandlerFactoryClass = "org.signal.wire.Factory"` (من wire-handler) |
| **`:lib:network`** | طبقة شبكة RED مبنية على libsignal-service |
| **`:lib:glide`** | دمج Glide 5 مع Compose لتحميل الصور (KSP) |
| **`:lib:apng`** | عرض صور PNG متحركة |
| **`:lib:archive`** | تنسيق أرشيف النسخ الاحتياطي (بروتوكول Wire) |
| **`:lib:billing`** | غلاف Google Play Billing 8.3 |
| **`:lib:blurhash`** | توليد placeholders ضبابية للصور |
| **`:lib:contacts`** | الوصول لجهات الاتصال والأذونات (ملف Groovy) |
| **`:lib:debuglogs-viewer`** | عرض/إرسال سجلات التصحيح |
| **`:lib:device-transfer`** | نقل الحساب بين الأجهزة |
| **`:lib:donations`** | واجهة التبرعات (Compose + Wallet) |
| **`:lib:image-editor`** | محرر الصور (رسم/قص/ملصقات) |
| **`:lib:paging`** | أدوات ترقيم صفحات القوائم الكبيرة |
| **`:lib:photoview`** | تحميل صورة مع تكبير/تصغير |
| **`:lib:qr`** | مسح رموز QR (CameraX + ZXing) |
| **`:lib:spinner`** | أداة تشخيص: خادم ويب محلي (nanohttpd) + SQLite + Handlebars |
| **`:lib:sticky-header-grid`** | شبكة RecyclerView برؤوس لاصقة |
| **`:lib:video`** | تشغيل ومعالجة الفيديو (Media3 + mp4parser) |

---

## 🔗 العلاقة
- كل الوحدات **مسجلة** في `settings.gradle.kts`
- `:lib:libsignal-service` هو العمود الفقري لاتصالات التطبيق بخوادم Signal/RED
