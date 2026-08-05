# `app/` — التطبيق الرئيسي لتطبيق RED

هذا هو **أكبر وأهم تطبيق Android** في المشروع — فورك كامل من Signal-Android أُعيدت تسميته بالكامل إلى `com.red.sovereign`، مع طبقة RED Sovereign المخصصة.

---

## 📊 أرقام

| المقياس | القيمة |
|---|---|
| الحجم | ~46 MB (أكبر مجلد في المشروع) |
| ملفات مصدرية | 3,802 (2,540 Kotlin + 1,262 Java) |
| `applicationId` | `com.red.sovereign` |
| الإصدار | `1.0.0-RED` (versionCode 1) |
| SDK | compileSdk 35 / minSdk 26 / targetSdk 35 |
| Java/Kotlin | 21 |

---

## 🧱 البنية العامة

```
app/src/main/java/
├── com/red/sovereign/          ← نواة RED المخصصة (33 ملفًا)
├── org/conscrypt/              ← نسخة Conscrypt معاد تسميتها
└── org/thoughtcrime/securesms/ ← كود Signal الكامل (~3,768 ملف)
                                 (الملفات موجودة في هذا المسار لكن الحزمة
                                  المعلنة كلها com.red.sovereign.*)
```

### نواة RED Sovereign (`com.red.sovereign`)
| الملف | الوظيفة |
|---|---|
| `RedSovereignApp.kt` | فئة Application (Hilt) — نقطة بدء التطبيق |
| `core/MasterFeatureSet.kt` | مجمّع الأنظمة الثلاثة (A: VoIP، B: GSM، C: Messaging) |
| `core/crypto/QuantumGuard.kt` | مولد "بذرة كمومية" (SecureRandom 32-byte) |
| `core/database/RedMasterDatabase.kt` | **Room DB** `red_sovereign.db` — جداول messages/groups/call_logs |
| `core/delivery/RedDeliveryEngine.kt` | محرك الإرسال: UUIDv7 + ProtoBuf + WebSocket + إعادة محاولة أسية |
| `core/delivery/SyncEngine.kt` | مزامنة فجوات أرقام التسلسل |
| `core/network/RedWebSocketClient.kt` | عميل WebSocket (OkHttp + Bearer auth) |
| `core/utils/RedMediaTransporter.kt` | رفع الملفات إلى MinIO |
| `features/calls/RedVoipMaster.kt` | إدارة مكالمة WebRTC (System A) |
| `features/pstn/PstnViewModel.kt` | مكالمة GSM عبر Dinstar (System B) |
| `features/chat/RedChatScreen.kt` | شاشة المحادثة + فقاعات الرسائل |
| `features/chat/LuxuryChatBubble.kt` | فقاعة محادثة بتدرج لوني وعلامات ✓✓ |
| `network/RedPushService.kt` | خدمة Foreground "RED Security Engine" |
| `ui/RedMainHost.kt` | التنقل الرئيسي (Compose NavHost) |

### حزمة `developed/` (`com.red.sovereign.developed`، 8 ملفات)
- `DevelopedChatCore.kt` — `REDCore`: تهيئة الأنظمة
- `delivery/GuaranteedDelivery.kt` — التوصيل المضمون (Exponential Backoff)
- `pstn/DuminManager.kt` — الاتصال ببوابة Dumin/GSM (192.168.1.100)
- `voip/DevelopedVoipController.java` — مكالمات عبر Mediasoup SFU (AV1 + RNNoise)
- `voip/QualityController.kt` — ضبط جودة 4K/AV1/Opus
- `voip/UltraHDCall.kt` — محرك المكالمات فائقة الوضوح

---

## 🖥️ الشاشات الرئيسية

- **`MainActivity.kt`** (54KB) — الواجهة الرئيسية: Material3 ثلاثي الألواح + 4 تبويبات (CHATS/CALLS/STORIES/ARCHIVE)
- **`components/webrtc/v2/WebRtcCallActivity.kt`** (53KB) — شاشة المكالمة الكاملة (PiP، تفاعلات)
- **`calls/new/NewCallActivity.kt`** — منتقي المستلمين للمكالمات
- **`conversation/v2/ConversationActivity.kt`** — شاشة المحادثة

### الصلاحيات الرئيسية (Manifest)
كاميرا، ميكروفون، اتصالات، موقع، هاتف، خدمات Foreground (camera/microphone/phoneCall/remoteMessaging)، إشعارات، بلوتوث.

### الخدمات في الخلفية
- `service/KeyCachingService` — قفل التطبيق
- `service/webrtc/ActiveCallManager` — إدارة المكالمة النشطة
- `gcm/FcmReceiveService` + `FcmJobService` — جلب الرسائل
- `jobmanager/*` — المهام المجدولة (BootReceiver، تنظيف الرسائل، المفاتيح)

---

## 📦 الاعتماديات الرئيسية (`dependencies.gradle.kts`)

| المجموعة | المكونات |
|---|---|
| مشروع | `:lib:libsignal-service`, `:core:util`, `:core:ui`, `:lintchecks` |
| Signal | `libsignal-android 0.99.1`, `sqlcipher-android 4.17.0`, `ringrtc-android 2.70.0` |
| System A | Media3 (ExoPlayer) |
| System B | `asterisk-java 3.40.0` |
| System C | Room + kotlinx-serialization 1.9.0 |
| UI | navigation-compose 2.9.8, material 1.12.0 |

---

## 🔧 JNI / Native
`jni/utils/org_thoughtcrime_securesms_util_FileUtils.cpp`:
- `getFileDescriptorOwner` — التحقق من مالك الـ FD (fstat)
- `createMemoryFileDescriptor` — إنشاء memfd (نقل المرفقات عبر Binder)

---

## ⚠️ ملاحظات فنية
1. **الملفات المميزة الكبيرة**: `MessageTable.kt` (265KB)، `ConversationFragment.kt` (203KB)، `RecipientTable.kt` (192KB)
2. **خطأ Manifest**: سطر يتيم `android:allowBackup="false"` خارج وسم `<application>` (سطر 75) — يكسر صلاحية XML
3. طبقة RED تستورد فئات غير موجودة داخل الوحدة (MinioUploader، ChatProtos، YemeniOperatorDetector) — **التطبيق لا يُبنى وحده** بدون دمج الوحدات الأخرى
4. `baseline-prof.txt` / `startup-prof.txt` — 6.9MB لكل منهما (موجودان)

---

## 🔗 العلاقة بباقي المشروع
- يتصل بالخادم `backend-server` عبر WebSocket (`/ws/master`) و REST
- يستخدم البروتوكول من `shared-proto/`
- الوسائط تُرفع إلى MinIO عبر `android/core/network/MinioUploader`
