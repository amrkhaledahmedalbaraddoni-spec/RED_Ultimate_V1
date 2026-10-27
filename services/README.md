# services/ — الكود المصدري (Application plane)

> **القاعدة:** كود المنتج فقط. لا Dockerfile، لا nginx، لا compose، لا SQL، لا أسرار.

| الوحدة | النوع | البناء | الوصف |
|---|---|---|---|
| `android/` | Gradle `:app` (Kotlin/Compose) | `./gradlew :app:assembleDebug` | تطبيق RED، هوية libsignal محلية |
| `backend/` | Gradle مستقل (Spring Boot) | `cd services/backend && gradle build` | API + WebSockets + الموافقة + الوسائط |
| `shared-proto/` | Gradle library | يُضمَّن في الاثنين | `red_protocol.proto` — بروتوكول واحد |
| `admin-dashboard/` | Vite/React/TS | `npm run build` | SPA ساكنة تُقدَّم خلف البوابة |
| `media-sfu/` | Node/mediasoup | `npm ci && npm run check` | SFU لصوت/فيديو RED |

## علاقة الكود بالطبقات الأخرى

- **البناء:** `platform/containers/*.Dockerfile` تقرأ من هنا؛ لا صور تُبنى من داخل `services/`.
- **البيانات:** `services/backend/build.gradle.kts` يحزم `../../data/sql` كموارد
  (`classpath:migrations`). لا تُنسخ مهاجرة داخل الخدمة — الفاحص وبوابة Gradle يفشلان.
- **الإعداد:** كل متغيّر بيئة يمر عبر `contracts/env.schema.json` والـ compose، لا عبر ملفات محلية.
- **البروتوكول:** `shared-proto` مشترك مع Android عبر `settings.gradle.kts` في الجذر
  و`services/backend/settings.gradle.kts`.

## التحقق المحلي

```bash
# Android (من الجذر)
./gradlew :app:testDebugUnitTest -PRED_SERVER_URL=http://SERVER_IP --dependency-verification strict

# Backend + اختبارات
cd services/backend && gradle clean build

# لوحة الإدارة
cd services/admin-dashboard && npm ci && npm run build

# SFU
cd services/media-sfu && npm ci && npm run check
```
