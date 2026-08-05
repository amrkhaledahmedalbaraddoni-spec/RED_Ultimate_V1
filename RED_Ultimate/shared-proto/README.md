# `shared-proto/` — بروتوكولات RED المشتركة (ProtoBuf)

تعريفات **ProtoBuf** الموحدة للاتصال بين التطبيق والخوادم — قلب بروتوكول المراسلة E2E والمزامنة.

---

## 📁 المحتوى

| الملف | الحزمة | Classname | الوظيفة |
|---|---|---|---|
| `messages.proto` | `com.red.proto` | `ChatProtos` | بروتوكول الرسائل الأساسي |
| `red_protocol.proto` | `com.red.sovereign.proto` | `RedProtos` | **الرسالة الموحدة** لكل العمليات (oneof) |
| `build.gradle.kts` | — | — | يبني البروتوكولات للوحدات الأخرى |

---

## 📜 `messages.proto` — الرسائل الأساسية
| الرسالة | الحقول |
|---|---|
| **`ChatMessage`** | `id` (UUID v7), `sender_id`, `receiver_id`, `conversation_id`, `type`, `payload` (المحتوى **المشفر**), `timestamp`, `sequence_number` (للمزامنة) |
| **`MessageAck`** | `message_id` + `status` (SENT / DELIVERED / READ) |

## 📜 `red_protocol.proto` — الرسالة الموحدة `RedRED`
رسالة واحدة بكل العمليات عبر `oneof signal`:
| العملية | الوظيفة |
|---|---|
| `ChatMessage` | إرسال رسالة (نفس البنية + `type` كنص: TEXT/IMAGE/VIDEO...) |
| `MessageAck` | تأكيد التسليم/القراءة (+ `sequence_number`) |
| `SyncRequest` | مزامنة مفقودة: `conversation_id` + نطاق `from_sequence → to_sequence` |
| `TypingRED` | إشعار "يكتب الآن" |
| `DeleteRED` | حذف رسالة (`for_everyone` للجميع) |

---

## 🔗 العلاقة
- **المطابقة مع الخادم**: حقول `ChatMessage`/`MessageAck` تطابق جداول MongoDB في `backend-server` (RedMessage) والرموز في `server/`
- يُستورد في التطبيق تحت `protowire/` (مجلدات الـ generated)
- `SyncRequest` هو أساس ميزة **Sequence Sync** (مزامنة الرسائل المفقودة)
