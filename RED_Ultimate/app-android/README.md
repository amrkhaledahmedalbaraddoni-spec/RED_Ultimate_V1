# `app-android/` — تطبيق DevelopedChat (النسخة الأقدم)

النسخة الأولية/البسيطة من رؤية RED — "DevelopedChat" — بنظام **موافقة إدارية كامل** وحزم `com.red.*` و`com.red.feature.*`. حُوّلت لاحقًا إلى النسخة السيادية في `android/`.

> **ملاحظة**: هذا المجلد **غير مسجّل** في `settings.gradle.kts`.

---

## 📁 البنية

```
app-android/
├── app/                    ← MainAppNavigation (غير مستخدم)
├── core/                   ← DeliveryEngine (object بسيط)
└── features/
    ├── app/                ← NavGraph + MainActivity (حزمة com.developedchat)
    ├── core/               ← النماذج، WebSocket، الشبكة، العمال
    └── feature/            ← auth, chat, pstn, stories
```

## `features/core/` — الأساسيات
| الملف | الوظيفة |
|---|---|
| `Models.kt` | نماذج Moshi: `UserStatus` (PENDING/APPROVED/REJECTED/BANNED)، `User`، `AuthResponse` |
| `delivery/DevelopedWebSocketClientImpl.kt` | عميل OkHttp WebSocket: Bearer auth + فك Protobuf (ChatMessage أو MessageAck) + إعادة اتصال |
| `di/NetworkModule.kt` | Hilt: BASE_URL `http://192.168.1.50:8080/api/` + Retrofit/Moshi |
| `utils/DevelopedLogger.kt` | غلاف Log بـ TAG `RED_System` |
| `workers/StoryCleanupWorker.kt` | حذف القصص منتهية الصلاحية كل **15 دقيقة** (WorkManager) |

## `features/feature/auth/` — نظام الموافقة الإدارية (8 ملفات)
| الملف | الوظيفة |
|---|---|
| `AuthApi.kt` | Retrofit: `auth/register`, `auth/login`, `auth/status`, `admin/approve/{userId}` |
| `AuthViewModel.kt` | حالات: Idle/Loading/Pending/Authenticated/Rejected/Banned/Error |
| `WelcomeScreen.kt` | شعار DC + أزرار Register/Login |
| `LoginScreen.kt` / `RegisterScreen.kt` | النماذج |
| `PendingApprovalScreen.kt` | شاشة انتظار بساعة رملية |
| `PermissionRequestScreen.kt` | طلب 9 صلاحيات دفعة واحدة |
| `StatusScreens.kt` | Rejected + Banned |

**دورة الموافقة**: Register → PENDING → المدير يوافق من لوحة التحكم → Login → التطبيق يعمل.

## `features/feature/chat/` — المحادثات
| الملف | الوظيفة |
|---|---|
| `ChatListScreen.kt` | قائمة محادثات (بيانات وهمية: Engineer Team, Admin, Dumin Gateway) |
| `ChatDetailScreen.kt` | المحادثة: فقاعات + `DeliveryStatusIcon` (SENDING→SENT→DELIVERED→READ→FAILED) |
| `ChatViewModel.kt` | Hilt: Flow من Room + إرسال عبر MessageDeliveryManager |

## `features/feature/pstn/` — المكالمات الهاتفية
| الملف | الوظيفة |
|---|---|
| `PstnCallScreen.kt` | واجهة المكالمة (برتقالية + زر إنهاء) |
| `PstnModels.kt` | Room: `PstnCallLog` + `PstnDao` + `PstnDatabase` v1 |
| `PstnViewModel.kt` | `makeCall` → `duminApi.startCall` → **polling كل ثانية** → RINGING/ACTIVE/ENDED → حفظ السجل |

## `features/feature/stories/` — القصص
| الملف | الوظيفة |
|---|---|
| `CameraCaptureScreen.kt` | تصوير عبر **CameraX** (ImageCapture + تبادل عدسة) |
| `StoryViewerScreen.kt` | عارض قصص (5 ثوانٍ لكل قصة + Progress Bar) |

---

## 🔗 العلاقة بباقي المشروع
- يستخدم `MessageDeliveryManager` من `android/core/delivery`
- يحتاج `REDDatabase` من `app/` (غير موجودة هنا)
- يتصل بـ `backend-server` (192.168.1.50:8080) — نفس الخادم

## ⚠️ حالة الاكتمال
- **نظام الموافقة**: مكتمل منطقيًا (أفضل نسخة)
- **المحادثات/القصص**: وهمية/ناقصة — شاشات مستوردة غير موجودة داخل المجلد
- **غير قابل للبناء وحده**
