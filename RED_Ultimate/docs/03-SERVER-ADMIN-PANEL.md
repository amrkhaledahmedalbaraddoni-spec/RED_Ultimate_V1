# 03 — السيرفر + لوحة الإدارة + تدفق البيانات

> شكل الخادم الرئيسي (backend-server)، كل API وWebSocket، ولوحة الإدارة (admin_dashboard) والتدفق الكامل للبيانات داخلها.

---

## 1. السيرفر الرئيسي — `backend-server` (Spring Boot :8080)

### البنية (58 ملف Kotlin في حزمتين)

```
com.red.server/*            ← النسخة النشطة (RED Sovereign)
├── auth/AuthController         ← /api/auth/register, /login, /admin/approve
├── api/AdminMasterController   ← /api/master/admin/* (نظام المراقبة)
├── api/admin/RedMasterController ← /api/master/v1/* (لوحة القيادة)
├── controllers/                ← AdminController, DinstarController, AdminMonitorController, DuminTelemetryController, HealthController
├── messaging/                  ← MessageService (dedup+تسلسل+تخزين), AdvancedMessageService, DeleteService
├── services/                   ← 9 خدمات: MasterStats, MasterOrchestration, IronSync, RedSecurity (KILL SWITCH/Wipe), Search, Story, Dinstar*, Core, StorageMonitor
├── pstn/                       ← DinstarMasterService, LoadBalancer, EventListener, PstnManager
├── websocket/                  ← 5 معالجات (تفاصيل بالأسفل)
├── database/                   ← MongoEntities, RedisManager
└── config/                     ← SecurityConfig, WebSocketConfig, RedisSequenceGenerator

com.developedchat/*         ← النسخة القديمة (لا تزال موجودة)
├── auth/AuthController         ← /api/auth/register, /login, /status + /api/admin/approve/{userId}
├── core/delivery/              ← MessageService, SyncService
├── core/websocket/             ← ChatWebSocketHandler
├── core/pstn/                  ← DuminGatewayService, PstnRelayService
└── core/storage/StorageController ← /api/media/upload, /download/{fileId}
```

### ⚙️ الإعدادات (application.yml)
| الإعداد | القيمة |
|---|---|
| المنفذ | 8080 |
| PostgreSQL | `db-postgres:5432/red_sovereign` (Flyway V1/V2, ddl-auto: validate) |
| MongoDB | `db-mongo:27017/red_sovereign` |
| Redis | `cache-redis:6379` |
| MinIO | `minio:9000`, bucket `red-media` |
| JWT | سر + انتهاء 24 ساعة (86400000 ms) |
| DINSTAR | `192.168.1.100:80` + auth-token |

---

## 2. كل الـ API endpoints

### المصادقة والموافقات
| الطريقة | المسار | الوظيفة |
|---|---|---|
| POST | `/api/auth/register` | تسجيل مستخدم (يبدأ PENDING) |
| POST | `/api/auth/login` | تسجيل الدخول (JWT) |
| POST | `/api/auth/admin/approve` | موافقة المدير |
| GET | `/api/master/v1/auth/pending` | قائمة المنتظرين |
| POST | `/api/master/v1/auth/action` | موافقة/رفض/حظر |

### لوحة القيادة (Master)
| الطريقة | المسار | الوظيفة |
|---|---|---|
| GET | `/api/master/v1/stats/realtime` | **إحصائيات حية** (كل الأنظمة) |
| GET | `/api/master/v1/hardware/dinstar/slots` | حالة 8 شرائح SIM |
| POST | `/api/master/v1/security/wipe` | **مسح جهاز عن بعد** |
| GET | `/api/master/v1/media/active-calls` | المكالمات النشطة |

### الإدارة (Admin)
| الطريقة | المسار | الوظيفة |
|---|---|---|
| GET | `/api/admin/users/pending` | طلبات الموافقة |
| POST | `/api/admin/users/update-status` | تحديث حالة مستخدم |
| GET | `/api/admin/monitor/stats` | إحصائيات المراقبة |
| GET | `/api/admin/stories/monitor` | مراقبة القصص |
| POST | `/api/admin/security/kill-switch` | **إيقاف النظام كاملًا** |
| GET | `/api/admin/dumin/telemetry` | تيليمتري عتاد System B |

### DINSTAR (بوابة الهاتف)
| الطريقة | المسار | الوظيفة |
|---|---|---|
| GET | `/api/admin/dinstar/status` | حالة البوابة |
| GET | `/api/admin/dinstar/discover` | اكتشاف جهاز |
| POST | `/api/admin/dinstar/reboot` | إعادة تشغيل البوابة |
| POST | `/api/admin/dinstar/config/sip` | إعداد SIP |
| POST | `/api/admin/dinstar/dial` | طلب اتصال GSM |

### وسائط + صحة
| الطريقة | المسار | الوظيفة |
|---|---|---|
| POST | `/api/media/upload` | رفع وسائط → MinIO |
| GET | `/api/media/download/{fileId}` | تحميل وسيط |
| GET | `/health` | صحة الخادم (مستعملة في docker-compose) |

### WebSockets (4 مسارات)
| المسار | الوظيفة |
|---|---|
| `/ws/chat` | **الرسائل اللحظية** (ChatWebSocketHandler) |
| `/ws/master` | بروتوكول RED Master (RedMasterHandler) |
| `/ws/red` | عام (RedWebSocketHandler) |
| `/ws/admin/logs` | **بث سجلات الخادم للوحة الإدارة** (AdminLogHandler) |

> كلها `setAllowedOrigins("*")` — بدون مصادقة WebSocket.

---

## 3. لوحة الإدارة — `admin_dashboard` (React 19 + Ant Design)

### الشكل العام
- **القائمة الجانبية** (6 صفحات): Dashboard، Master Control، User Management، DINSTAR Control، Live Monitor، Diagnostics
- **Master Control** = قشرة 7 تبويبات: Overview، Authority، Messaging، Dinstar، Media SFU، Security، Infrastructure
- **التحديث الحي**: `setInterval` كل 2–5 ثوانٍ (polling) + WebSocket للسجلات

### كل صفحة وبياناتها
| الصفحة | ما تعرضه | مصدر البيانات |
|---|---|---|
| **Dashboard** | إحصائيات + رسم حركة + صحة الخادم | `GET /api/admin/monitor/stats` كل 5 ثوانٍ |
| **Master Control → Overview** | حالة النظام + PostgreSQL/MongoDB/Redis/MinIO | `GET /api/master/v1/stats/realtime` |
| **Master Control → Authority** | موافقات (Approve/Reject منفصلان) | `GET /api/master/v1/auth/pending` + `POST /api/master/v1/auth/action` |
| **Master Control → Messaging** | معدل التسليم + زمن الاستجابة | `GET /api/master/v1/stats/realtime` |
| **Master Control → Dinstar** | 8 شرائح + Manual Sync | `GET /api/master/v1/hardware/dinstar/slots` كل 5 ثوانٍ |
| **Master Control → Security** | **KILL SWITCH** + **Remote Wipe** | `POST /api/admin/security/kill-switch` + `/api/master/v1/security/wipe` |
| **Master Control → LogStreamer** | **سجلات حية** (شاشة سوداء/خضراء) | `ws://{host}:8080/ws/admin/logs` |
| **User Management** | جدول موافقات + Modal تأكيد | `GET /api/admin/users/pending` + `POST /api/admin/users/update-status` |
| **DINSTAR Control** | 8 بطاقات SIM + Reboot + سجل مكالمات | `GET /api/admin/dinstar/status` + `POST /api/admin/dinstar/reboot` |
| **Live Monitor** | "Command Center": نظام C + سلطة + نظام B + DB | `GET /api/master/v1/stats/realtime` كل 3 ثوانٍ |
| **Diagnostics** | تشخيص VoIP/PSTN/Messaging/MinIO | `GET /api/master/v1/stats/realtime` |
| **DuminAdvanced** | تيليمتري عتاد System B | `GET /api/admin/dumin/telemetry` كل 5 ثوانٍ |

---

## 4. تدفق البيانات في لوحة الإدارة (رحلة طلب واحد)

```
لوحة الإدارة (React في المتصفح)
   │
   │ fetch("/api/master/v1/stats/realtime") كل 3-5 ثوانٍ
   ▼
nginx (:80 → admin_panel nginx.conf)
   │ location /api/ → proxy_pass http://backend:8080
   ▼
backend-server (Spring Boot)
   │ MasterStatsService → يستدعي:
   ├── PostgreSQL (عدد المستخدمين، حالة Dinstar)
   ├── MongoDB (عدد الرسائل)
   ├── Redis (نشاط اللحظات)
   ├── MinIO (مساحة الوسائط)
   └── media-sfu + pstn-gateway (صحة الأنظمة)
   │ يجمعها في JSON واحد
   ▼
لوحة الإدارة تعرضها في بطاقات ورسم ECharts

السجلات الحية (مسار منفصل):
الخادم (AdminLogHandler) --push--> WebSocket /ws/admin/logs --> LogStreamerTab
```

### تدفق "موافقة مستخدم" (أهم عملية إدارية)
```
1) مستخدم يسجل → users.status = PENDING (PostgreSQL)
2) لوحة الإدارة: GET /api/admin/users/pending → الجدول يظهر
3) المدير يضغط Approve → POST /api/admin/users/update-status (status=APPROVED)
4) المستخدم يسجل دخول → /api/auth/login → JWT → التطبيق يعمل
```

### تدفق "KILL SWITCH" (أقوى عملية)
```
لوحة الإدارة → SecurityTab → زر Kill Switch
   → POST /api/admin/security/kill-switch
   → RedSecurityService يفعل الحالة الحرجة
   → يمنع كل الرسائل والمكالمات (التبديل الفعلي بحاجة اكتمال)
```

---

## 5. خوادم الدعم (دورها في التدفق)

| الخادم | المنفذ | الدور |
|---|---|---|
| **nginx** | 80 | بوابة كل شيء: `/api/` و`/ws/` → backend |
| **media-sfu** (Mediasoup) | 4000 + 40000-40100/UDP | وسائط مكالمات الفيديو (System A) |
| **coturn** | 3478 | اختراق NAT للمكالمات |
| **pstn-gateway** (Asterisk) | 5060 + 5038 | جسر WebRTC ↔ شرائح GSM (System B) — يتحكم به backend عبر AMI |

---

## 6. تناقضات معروفة (للإصلاح)

1. **لوحة `Approvals.js`** تستدعي `/api/admin/pending-users` — غير موجود (الصحيح `/api/admin/users/pending`)
2. **SecurityTab** يستدعي wipe بـ `{userId}` في المسار — الخادم ينتظر `?userId=`
3. **لا مصادقة**: كل `/api/**` وكل WebSockets مفتوحة (`*` origins) — JWT موجود لكنه غير مفعّل
4. **ثلاث نسخ من اللوحة**: `admin_dashboard/` (نشطة)، `admin-dashboard/` (ثابتة قديمة)، لوحة مراقبة مختلفة في `android/` التطبيق
