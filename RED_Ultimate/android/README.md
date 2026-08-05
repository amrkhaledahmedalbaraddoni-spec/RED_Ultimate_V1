# `android/` — تطبيق RED السيادي (النسخة البديلة)

نسخة Android أصغر من نفس رؤية RED، مكتوبة بالكامل بـ **Jetpack Compose + Hilt**، بثيم فاخر **"AQYAL Sovereign"** (ذهبي/أوبسيديان). حزمة `com.red.sovereign`.

> **ملاحظة**: هذا المجلد **غير مسجّل** في `settings.gradle.kts` — يعمل كمرجع/نموذج، والبناء الرئيسي يتم من مجلد `app/`.

---

## 📁 البنية

```
android/
├── app/            ← نقطة الدخول (MainActivity، التنقل، الداشبورد)
├── core/           ← البنية التحتية (DB، الإرسال، الشبكة، الثيم)
└── features/       ← الشاشات (auth, calls, chat, explore, profile, pstn, stories)
```

## `app/` — نقطة الدخول
| الملف | الوظيفة |
|---|---|
| `MainActivity.kt` | نقطة الدخول — يعرض `RedMainDashboard` مباشرة |
| `MainAppNavigation.kt` | NavHost كامل (auth → main → chat/voip/pstn/settings) — **معرّف لكن غير مستخدم** |
| `RedDashboard.kt` | داشبورد قديم (5 تبويبات) |
| `RedMainDashboard.kt` | **الداشبورد الفعلي**: محادثات، مكالمات، لوحة اتصال ذهبية، استكشاف، إعدادات |
| `sovereign/MasterSystemOrchestrator.kt` | Hilt Singleton ينسق الأنظمة الثلاثة |
| `sovereign/RedConnector.kt` | ربط وهمي (Logging فقط) |

## `core/` — البنية التحتية
| الملف | الوظيفة |
|---|---|
| `database/MasterDatabase.kt` | **Room v2**: MessageEntity, ConversationEntity, PstnLogEntity, StoryEntity, StoryViewEntity |
| `delivery/BurnManager.kt` | رسائل ذاتية التدمير (BURNED بعد مؤقت) |
| `delivery/MessageDeliveryManager.kt` | إرسال: UUID + Room + ProtoBuf + WebSocket + ACK |
| `delivery/RedDeliveryEngine.kt` | محرك الإرسال السيادي (UUIDv7 + Exponential Backoff 5 مرات) |
| `delivery/SyncManager.kt` | طلب الفجوات في أرقام التسلسل |
| `di/StoryModule.kt` | Hilt Module (StoryDao + StoryRepository) |
| `linker/RedSystemLinker.kt` | يربط SECURE_MSG + HD_VOIP + PSTN_GSM |
| `network/MinioUploader.kt` | رفع الملفات إلى MinIO عبر OkHttp |
| `network/RedNotificationService.kt` | خدمة Foreground "RED Security Active" بدون FCM |
| `theme/RedTheme.kt` | ثيم AQYAL: ألوان ذهبية/أوبسيديان/أزرق ملكي + خلفية متدرجة متحركة |
| `utils/MediaCompressor.kt` | ضغط الصور (JPEG 85%) والفيديو (H.264 720p عبر Media3) |
| `utils/VideoTrimmer.kt` | قص الفيديو إلى 30 ثانية (حد القصص) |

## `features/` — الشاشات
| القسم | الملفات | الوظيفة |
|---|---|---|
| `auth/` | `RedSplashScreen.kt` | شاشة البداية (حرف R نابض، 3 ثوانٍ) |
| `calls/` (10) | `RedVoipMaster`, `VoipEngine`, `WebRtcSignaler`, `VideoCallScreen`, `RedCallLogScreen`, `RedCallForegroundService`, `LiveBroadcast*`, `ConferenceScreen` | System A: مكالمات WebRTC (STUN/TURN محلي 192.168.1.50:3478) + بث مباشر + مؤتمرات |
| `chat/` (9) | `RedChatListScreen`, `RedChatBubble`, `MediaBubble`, `GroupManager`, `VoiceRecorder`... | System C: قوائم محادثات بتبويبات + فقاعات + مجموعات Room + تسجيل OGG/Opus |
| `explore/` | `RedExploreScreen.kt` | استكشاف: بث مباشر + غرف صوتية (بيانات hardcoded) |
| `profile/` (6) | `ProfileScreen`, `ProfileApi`, `SettingsScreen`, `RedSettingsScreen`, `UpdateScreen`, `BackupScreen` | إعدادات فاخرة + بطاقة "خطي اليمني DINSTAR" الذهبية |
| `pstn/` (3) | `PstnEngine`, `PstnDialerScreen`, `RedDialButton` | System B: لوحة اتصال مزدوجة (VoIP أزرق / خط يمني ذهبي) |
| `stories/` (4) | `CreateStoryScreen`, `StoryListSection`, `StoryRepositoryImpl`, `StoryViewerScreen` | قصص 24 ساعة مع رفع MinIO |

---

## 🎨 الثيم "AQYAL Sovereign"
- `AqyalGold` `#F59E0B` — الذهب الملكي
- `AqyalDarkObsidian` `#030712` — الأسود
- `AqyalRoyalBlue` `#0F172A` — الأزرق الملكي
- `AqyalCyanGlow` `#38BDF8` — التوهج السماوي
- مكونات: `SovereignBackground` (خلفية متدرجة متحركة) + `AqyalEpicButton` (أزرار بحواف ذهبية)

---

## 🔗 العلاقة بباقي المشروع
- يحتاج فئات من `app/` (MasterDao, MessageEntity, RedWebSocketClient, ApprovalManager)
- يحتاج بروتوكولات من `shared-proto/`
- يتصل بـ `backend-server` على `192.168.1.50:8080`

## ⚠️ حالة الاكتمال
- **مكتمل الهيكل**: كل الشاشات موجودة
- **شبه كامل**: بعض الشاشات تعرض بيانات hardcoded (استكشاف، مجموعات، بحث)
- **غير قابل للبناء وحده**: يعتمد على فئات خارجية غير مسجلة في البناء
