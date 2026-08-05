# 04 — التطبيقات: الثلاثة وكل ما بداخلها

> المشروع يحوي **3 تطبيقات Android** بأسماء حزم مختلفة تطورت عبر الزمن. هذه الوثيقة تشرح كل واحد: مكانه، حزمه، ميزاته، وعلاقته بالباقي.

---

## 🏆 مقارنة سريعة

| | `app/` | `android/` | `app-android/` |
|---|---|---|---|
| **الاسم** | RED Sovereign (الرئيسي) | AQYAL Sovereign | DevelopedChat |
| **الحالة** | ✅ **الرئيسي والنشط** | بديل/نموذج | الأقدم |
| **الحزمة** | `com.red.sovereign` | `com.red.app` | `com.red.*` / `com.developedchat` |
| **الأساس** | **Signal-Android كامل** (3,768 ملف) | Compose + Hilt نظيف | Compose + Hilt بسيط |
| **الملفات** | ~3,800+ | ~90 | ~40 |
| **مسجل في settings** | ✅ | ❌ | ❌ |
| **قاعدة بيانات** | SignalDatabase + RedMasterDatabase | MasterDatabase (Room v2) | PstnDatabase |
| **قابل للبناء** | ❌ (ناقص فئات) | ❌ (غير مسجل) | ❌ (غير مسجل) |

---

## 1. `app/` — التطبيق الرئيسي (RED Sovereign)

**الفكرة**: فورك Signal كامل (كود أثبت جدارته عالميًا) + طبقة RED فوقه للتواصل مع خوادم المشروع.

### البنية (حزمتان)
```
org.thoughtcrime.securesms/*   ← Signal الأصلية (3,768 ملف)
│   ├── database/     ← SignalDatabase (Room، Migrations حتى V164)
│   ├── service/      ← الخدمات والاتصال بخادم Signal
│   ├── conversations/← شاشات المحادثات
│   └── ...           ← كل أجزاء Signal

com.red.sovereign/*            ← طبقة RED (31 ملف)
├── core/
│   ├── database/     ← RedMasterDatabase (messages/groups/call_logs) + MasterDao
│   ├── delivery/     ← MasterDeliveryEngine (الإرسال)
│   ├── di/           ← RedMasterModule (Hilt)
│   └── MasterFeatureSet.kt
├── features/
│   ├── calls/        ← RedVoipMaster (System A)
│   ├── chat/         ← ChatViewModel, RedChatScreen, RedChatDetail
│   ├── pstn/         ← DialPadScreen, PstnViewModel (System B)
│   └── stories/      ← StoryViewModel
├── security/         ← QuantumGuard, IdentityManager, RedIdentityManager
├── RedSovereignApp.kt (Application)، RedMainHost.kt، RedWebSocketClient.kt، RedMediaTransporter.kt، CallOrchestrator.kt، SyncEngine.kt، NotificationBridge.kt، RedPushService.kt، PermissionRequestScreen.kt، SovereignAuthScreens.kt
```

### الميزات الفريدة (طبقة RED)
| الملف | الوظيفة |
|---|---|
| `RedWebSocketClient.kt` | اتصال WebSocket بخادم الرسائل (/ws/chat) |
| `MasterDeliveryEngine.kt` | إرسال مضمون (Sequence + ACK) |
| `SyncEngine.kt` | طلب المفقود عبر SyncRequest |
| `QuantumGuard.kt` | "حارس كمومي" (مفهوم أمني — محاكاة) |
| `RedMediaTransporter.kt` | رفع/تحميل الوسائط عبر MinIO |
| `CallOrchestrator.kt` | تنسيق المكالمات VoIP |
| `DialPadScreen.kt` | لوحة اتصال GSM (DINSTAR) |
| `LuxuryChatBubble.kt` | فقاعات فاخرة (ثيم ذهبي) |

---

## 2. `android/` — نسخة AQYAL السيادية (البديلة)

**الفكرة**: إعادة بناء خفيفة من الصفر بالتصميم الفاخر "AQYAL Sovereign" (ذهبي/أوبسيديان/أزرق ملكي) — بدون عبء كود Signal.

### البنية
```
android/
├── app/                    ← MainActivity + RedMainDashboard (داشبورد 5 تبويبات)
│   └── sovereign/          ← MasterSystemOrchestrator (Hilt ينسق 3 أنظمة), RedConnector
├── core/
│   ├── database/           ← MasterDatabase (Room v2: Message/Conversation/PstnLog/Story/StoryView)
│   ├── delivery/           ← BurnManager, MessageDeliveryManager, RedDeliveryEngine, SyncManager
│   ├── di/                 ← StoryModule (Hilt)
│   ├── linker/             ← RedSystemLinker (يربط A+B+C)
│   ├── network/            ← MinioUploader, RedNotificationService (بدون FCM)
│   ├── theme/              ← RedTheme (AQYAL)
│   └── utils/              ← MediaCompressor (JPEG 85% / H.264 720p), VideoTrimmer (30 ثانية)
└── features/
    ├── auth/               ← RedSplashScreen
    ├── calls/ (10 ملفات)    ← RedVoipMaster, VoipEngine, WebRtcSignaler, VideoCallScreen, RedCallLogScreen, RedCallForegroundService, LiveBroadcast*, ConferenceScreen
    ├── chat/ (9)           ← RedChatListScreen, RedChatBubble, MediaBubble, GroupManager, VoiceRecorder
    ├── explore/            ← RedExploreScreen (بث + غرف صوتية)
    ├── profile/ (6)        ← ProfileScreen, ProfileApi, SettingsScreen, RedSettingsScreen, UpdateScreen, BackupScreen
    ├── pstn/ (3)           ← PstnEngine, PstnDialerScreen, RedDialButton
    └── stories/ (4)        ← CreateStoryScreen, StoryListSection, StoryRepositoryImpl, StoryViewerScreen
```

### الملاحظات
- **الأنظمة الثلاثة كلها حاضرة**: calls = A، pstn = B، chat = C
- أفضل تنفيذ لمفهوم "القصص" و"الدمج" (Linker)
- الثيم الفاخر هو أبرز ميزة (خلفية متدرجة متحركة)

---

## 3. `app-android/` — DevelopedChat (الأقدم)

**الفكرة**: النموذج الأول — تطبيق بسيط يركز على **نظام الموافقة الإدارية** (أفضل تنفيذ للمفهوم).

### البنية
```
app-android/
├── app/                   ← MainAppNavigation (غير مستخدم)
├── core/                  ← DeliveryEngine (object بسيط)
└── features/
    ├── app/               ← NavGraph + MainActivity (com.developedchat)
    ├── core/
    │   ├── delivery/      ← DevelopedWebSocketClientImpl (Bearer + Protobuf + إعادة اتصال)
    │   ├── di/            ← NetworkModule (BASE_URL http://192.168.1.50:8080/api/)
    │   ├── utils/         ← DevelopedLogger (TAG: RED_System)
    │   └── workers/       ← StoryCleanupWorker (حذف القصص كل 15 دقيقة)
    └── feature/
        ├── auth/ (8)      ← AuthApi, AuthViewModel, Welcome/Login/Register, PendingApprovalScreen (ساعة رملية), PermissionRequestScreen (9 صلاحيات), StatusScreens
        ├── chat/ (3)      ← ChatListScreen (بيانات وهمية), ChatDetailScreen (أيقونات Delivery), ChatViewModel
        ├── pstn/ (3)      ← PstnCallScreen, PstnModels (Room), PstnViewModel (polling كل ثانية)
        └── stories/ (2)   ← CameraCaptureScreen (CameraX), StoryViewerScreen (5 ثوانٍ/قصة)
```

### دورة الموافقة (أفضل تنفيذ في المشروع)
```
Register → PENDING → (المدير يوافق من اللوحة) → Login → يعمل التطبيق
Rejected/Banned → شاشات حالة خاصة
```

---

## 4. تطور المشروع (الخلاصة)

```
app-android/ (النموذج الأول: الموافقة) 
    → android/ (النسخة السيادية الفاخرة، الأنظمة الثلاثة) 
    → app/ (القرار النهائي: فورك Signal + طبقة RED)
```

**التوصية**: `app/` هو الخيار الصحيح للاستمرار (يرث أمان Signal واختباره)، مع استعارة أفكار ناضجة من `android/` (الثيم AQYAL، تنظيم القصص) و`app-android/` (سير الموافقة).
