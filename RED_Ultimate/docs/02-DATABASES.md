# 02 — قواعد البيانات: كل مخزن، شكله، وتدفق البيانات بينه

> RED يستخدم **5 أنواع مخازن بيانات** — 3 على الخادم، و2 على جهاز المستخدم. هذه الوثيقة تشرح كل واحد بجداوله الحقيقية.

---

## 🗺️ الخريطة العامة

```
                    ┌─────────────────────────────────────┐
                    │          backend-server :8080       │
                    │   (يطبّق 5 خدمات: الرسائل، التوثيق،  │
                    │    DINSTAR، الأمان، المراقبة)        │
                    └───────┬──────────┬──────────┬───────┘
                            ▼          ▼          ▼
                    ┌─────────────┐ ┌─────────┐ ┌──────────┐
                    │ PostgreSQL  │ │ MongoDB │ │  Redis   │
                    │ (الهوية)    │ │(الرسائل)│ │(تسلسل+   │
                    │ :5432       │ │ :27017  │ │ حضور)    │
                    │ red_sovereign│ │ red_sovereign│ :6379 │
                    └─────────────┘ └─────────┘ └──────────┘
                            │
                    ┌───────▼────────┐   ┌─────────────┐
                    │     MinIO      │   │  media-sfu  │
                    │ (وسائط/نسخ)    │   │  (ذاكرة)    │
                    │ :9000          │   │  :4000      │
                    └────────────────┘   └─────────────┘

        على جهاز المستخدم:
        ┌──────────────────────────────────────────────┐
        │  app/      → RedMasterDatabase (Room v1)      │
        │              + SignalDatabase (Signal SQLite) │
        │  android/   → MasterDatabase (Room v2)        │
        │  app-android→ PstnDatabase (Room v1)          │
        └──────────────────────────────────────────────┘
```

---

## 1. PostgreSQL 16 (`red_sovereign`) — الهوية والسلطة

**الغرض**: المستخدمون، الموافقات، المجموعات، أجهزة DINSTAR. (يديره Spring Boot عبر JPA + Flyway migrations)

### الجداول (من `V1__Initial_Schema.sql`)

| الجدول | الأعمدة | الوظيفة |
|---|---|---|
| **users** | `id UUID PK`, `email UNIQUE`, `password_hash`, `full_name`, `status` (PENDING/APPROVED/BANNED), `role` (ADMIN/USER), `created_at` | حساب المستخدم + حالة الموافقة |
| **groups** | `id UUID PK`, `name`, `owner_id → users`, `created_at` | المجموعات |
| **group_members** | `group_id + user_id` (PK مركّب), `role` (OWNER/ADMIN/MEMBER) | العضوية والأدوار |
| **dinstar_slots** | `slot_index PK` (0-7), `operator`, `status` (IDLE/CALLING/ERROR), `signal_strength`, `balance` | مراقبة 8 شرائح GSM |

### الجداول الإضافية (من `V2__Dinstar_Master_Control.sql`)

| الجدول | الأعمدة | الوظيفة |
|---|---|---|
| **dinstar_config** | `id SERIAL`, `device_ip`, `api_port`, `username`, `password_hash`, `sip_server_ip`, `last_sync` | إعدادات بوابة DINSTAR |
| **dinstar_ports** | `port_index PK`, `sim_number`, `operator_name`, `is_enabled`, `total_calls`, `total_minutes`, `signal_threshold` | إحصائيات كل منفذ SIM |
| **dinstar_logs** | `id SERIAL`, `event_type` (RESTART/CONFIG_CHANGE/SMS_SENT), `description`, `created_at` | سجل عمليات (Audit) |

> ⚠️ يوجد نسختان من schema (V1/V2 = الأنشط + `master-schema.sql` = مخطط قديم). الـ Flyway يعتمد V1/V2.

---

## 2. MongoDB 8 (`red_sovereign`) — مخزن الرسائل الضخم

**الغرض**: كل الرسائل (System C) — اختير لنمو غير محدود وسرعة كبيرة. الحقول مطابقة لبروتوكول `shared-proto/messages.proto`.

### المجموعة `messages` (من `MongoEntities.kt` + `MessageService.kt`)

| الحقل | النوع | فهرس | الوصف |
|---|---|---|---|
| `_id` | String | PK | UUID v7 (مرتب زمنيًا) |
| `conversationId` | String | ✅ | معرف المحادثة (بحث سريع) |
| `senderId` | String | ✅ | المرسل |
| `type` | String | — | TEXT/IMAGE/VIDEO/AUDIO/FILE |
| `payload` | ByteArray | — | **المحتوى المشفر بالكامل** (E2E) |
| `sequenceNumber` | Long | ✅ | **حرج**: رقم التسلسل للمزامنة والترتيب |
| `isEdited` | Boolean | — | هل عُدّلت |
| `replyTo` | String? | — | رد على رسالة |
| `status` / `deliveredAt` / `readAt` | — | — | حالة التسليم/القراءة |

---

## 3. Redis 7 (`:6379`) — الذاكرة السريعة

**الغرض**: تسلسل الرسائل، الحضور، إشعارات pub/sub.

| المفتاح | النوع | الوظيفة |
|---|---|---|
| `red:seq:{conversationId}` | Counter | **مولّد أرقام التسلسل** (INCR) — أساس مزامنة الفجوات |
| `red:presence:{userId}` | String | الحضور (online/offline، 5 دقائق) |
| `red:messages:{receiverId}` | Pub/Sub | إشعار فوري عند وصول رسالة جديدة |

---

## 4. MinIO (S3 محلي) — الوسائط والنسخ

**الغرض**: تخزين الصور والفيديوهات والنسخ الاحتياطية.

| الدليل | الوصول | الوظيفة |
|---|---|---|
| `red-media` | **عام (public)** | الوسائط المرفوعة في المحادثات |
| `red-backups` | خاص | النسخ الاحتياطية |

> الاعتمادات الافتراضية: `redadmin`/`redsecret123` (عبر compose) — يوجد تعارض مع `infrastructure/setup-env.sh` الذي يستخدم `admin/password`.

---

## 5. قواعد جهاز المستخدم (Room/SQLite)

### `app/` — `RedMasterDatabase` (Room v1، 3 جداول)
| الجدول | الحقول | ملاحظة |
|---|---|---|
| **messages** | `id AUTO PK`, `uuid UNIQUE`, `conversationId`, `senderId`, `type`, `content` (نص/رابط MinIO), `status` (SENDING→SENT→DELIVERED→READ→FAILED), `timestamp`, `sequenceNumber`, `replyToId`, `isEdited`, `metadata` | **مرآة خادم MongoDB على الجهاز** — نفس حقول الـ proto |
| **groups** | `groupId PK`, `name`, `avatarUrl`, `ownerId`, `myRole` | المجموعات المحلية |
| **call_logs** | `id PK`, `remoteId`, `type` (VOIP_AUDIO/VIDEO/CONFERENCE/LIVE/PSTN), `direction`, `timestamp`, `duration`, `dinstarSlot` | **سجل موحد للمكالمات (A وB)** |

### `app/` — `SignalDatabase` (Signal الأصلية)
قاعدة Signal الكاملة (مئات الجداول: threads, recipients, messages، Room v2 مع Migrations حتى V164+) — تُدار بصورة منفصلة عن قاعدة RED.

### `android/` — `MasterDatabase` (Room v2)
`MessageEntity`, `ConversationEntity`, `PstnLogEntity`, `StoryEntity`, `StoryViewEntity` — النسخة السيادية AQYAL.

### `app-android/` — `PstnDatabase` (Room v1)
`PstnCallLog` + `PstnDao` — سجل مكالمات GSM فقط.

---

## 6. تدفق رسالة واحدة (من الإرسال حتى القراءة) — System C

```
1) المستخدم يرسل → التطبيق يكتب في Room المحلي (status=SENDING)
2) التطبيق يشفر payload (E2E) ويبني ChatMessage (proto)
3) WebSocket /ws/chat → backend-server
4) الخادم: يتحقق من التكرار (dedup) → INCR red:seq:{conv} → يحفظ في MongoDB
5) الخادم ينشر pub/sub على red:messages:{المستلم}
6) المستلم يصل له إشعار → يسحب الرسالة → يخزنها Room محليًا
7) المستلم يقرأ → ack (READ) → يحدّث Mongo + Room المحلي
8) أي فجوة → SyncRequest (from_sequence→to_sequence) → الخادم يعيد الأرقام الناقصة
```

**قاعدة ذهبية**: `sequenceNumber` هي نظام المزامنة — تولد من Redis، تُخزن في Mongo، وتُطابق في Room المحلي.
