# `demo/` — تطبيقات العينات التجريبية (12 وحدة)

تطبيقات Android مصغّرة **تُشغَّل بأذرعها الخاصة** وتعرض وتختبر كل مكتبة من `lib/` عمليًا (نفس نمط Signal الرسمي). لكل واحدة `MainActivity` خاصة وتُبنى مستقلة.

---

## 🗂️ الوحدات (12)

| الوحدة | تعرض مكتبة | ما تشاهده عند تشغيلها |
|---|---|---|
| `:demo:apng` | `lib/apng` | معرض صور APNG متحركة (38 صورة اختبار + عارض) |
| `:demo:camera` | `feature/camera` | تجربة الكاميرا المدمجة |
| `:demo:contacts` | `lib/contacts` | استعراض جهات الاتصال والأذونات |
| `:demo:debuglogs-viewer` | `lib/debuglogs-viewer` | عارض السجلات وإرسالها |
| `:demo:device-transfer` | `lib/device-transfer` | محاكاة نقل الحساب بين جهازين |
| `:demo:donations` | `lib/donations` | واجهة التبرعات التجريبية |
| `:demo:image-editor` | `lib/image-editor` | محرر الصور (رسم/قص/ملصقات) |
| `:demo:paging` | `lib/paging` | قوائم الترقيم الكبيرة |
| `:demo:qr` | `lib/qr` | ماسح رموز QR |
| `:demo:registration` | `feature/registration` | **الأشمل**: سير التسجيل كاملًا بنماذج وهمية (`DemoNetworkController`, `DemoStorageController`, `FakeDeviceTransferRunner`, شاشة تصحيح `NetworkDebugOverlay`) |
| `:demo:spinner` | `lib/spinner` | أداة التشخيص (خادم ويب محلي) |
| `:demo:video` | `lib/video` | مشغّل ومعالج الفيديو |

---

## 🎯 الغرض
1. **اختبار واجهة** كل مكتبة دون إدراجها في التطبيق الرئيسي
2. **نموذج مرجعي** للمطورين لاستخدام الواجهة (API) صحيحًا
3. `:demo:registration` هو أهمها: يشغّل سير التسجيل الحقيقي ببيانات وهمية — مثالي لفهم تدفق الحساب

## 🚀 التشغيل
```bash
# مثال لتشغيل عينة التسجيل:
./gradlew :demo:registration:installDebug
```

## 📌 ملاحظات
- كلها **مسجلة** في `settings.gradle.kts` ومستقلة عن `app/`
- التطبيق الرئيسي **لا يستورد** من `demo/` (اتجاه واحد: demo يستخدم lib/feature)
