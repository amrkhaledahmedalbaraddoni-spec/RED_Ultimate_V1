# `feature/` — وحدات الميزات (3 وحدات)

وحدات الميزات الأكبر — طبقة فوق `core/` و`lib/`، يستخدمها التطبيق الرئيسي `app/`. كل وحدة مسؤولية واحدة واضحة.

---

## 🗂️ الوحدات

| الوحدة | النوع | الغرض | أبرز المحتوى |
|---|---|---|---|
| **`:feature:camera`** | Android + Compose | **كاميرا مدمجة** (بدل كاميرا النظام) لالتقاط الوسائط داخل التطبيق | `CameraScreen.kt`, `CameraDisplay.kt`, `CameraCaptureMode.kt`, `CameraDependencies.kt` |
| **`:feature:media-send`** | Android + Compose | **إرسال الوسائط** (صور/فيديو): معاينة، تحرير، قيد | `EditorState.kt`, `MediaConstraints.java`, `MediaSendActivityContract.kt`, `MediaSendDependencies.kt`, `CameraFragment.java` |
| **`:feature:registration`** | Android + Compose | **سير تسجيل الحساب الكامل** (هاتف → PIN → إنشاء الملف الشخصي) | `RegistrationActivity.kt`, `RegistrationFlowEvent.kt`, `NetworkController.kt`, `PersistedFlowState.kt`, `ContactSupportController.kt`, `PendingRestoreOption.kt` + 15+ شاشات (welcome, verificationcode, pinentry, devicetransfer...) |

---

## 🧪 `:feature:registration` — الشاشات
- سير Signal الكامل: ترحيب، إدخال رقم الهاتف، رمز التحقق، PIN، إنشاء الملف، استعادة النسخة الاحتياطية (محلية/سحابية)، نقل الجهاز
- 15 شاشة في `screens/` + مجلدات `test` لكل شاشة و`data/` للاتصال بالشبكة
- خط `MonoSpecial-Regular.otf` مخصص في `fonts/`

## 📌 ملاحظات فنية
- تتبع نمط Signal الحديث: `android + compose` مع Compose UI و DI عبر `*Dependencies.kt`
- تستخدم `:core:ui`, `:core:util`, `:core:models-jvm`
- كل الوحدات **مسجلة** في `settings.gradle.kts` وموجودة في `dependencies.gradle.kts`

## 🔗 العلاقة
- قاعدة الهرمية محفوظة: **feature → lib و core** (لا عكس)
- `:feature:registration` هو أكبر وحدة ميزات (سير الحساب الكامل) — بوابة دخول المستخدم
