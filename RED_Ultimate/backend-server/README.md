# `backend-server/` — الخادم الرئيسي (RED Sovereign)

الخادم المركزي للمنظومة — **Spring Boot 3.4 + Kotlin 2.1 + Java 21**، منفذ **8080**. "العقل" الذي يدير: المصادقة، الموافقات، الرسائل، المكالمات الهاتفية، المراقبة، والأمان.

---

## 📊 أرقام

| المكوّن | القيمة |
|---|---|
| Framework | Spring Boot 3.4.0 |
| Kotlin / Java | 2.1.0 / 21 |
| قواعد البيانات | MongoDB + PostgreSQL (Flyway) + Redis |
| التخزين | MinIO (bucket `red-media`) |
| بوابة الهاتف | Dinstar UC2000 (افتراضي 192.168.1.100) |
| JWT | jjwt 0.12.6 |

---

## 🗂️ بنية الكود (`src/main/kotlin/com/red/server/`)

### الحزم الرئيسية
| الحزمة | المحتوى |
|---|---|
| `api/` | `AdminMasterController`, `admin/RedMasterController` |
| `auth/` | `AuthController` (register/login/approve) + `RedApprovalService` (موافقات) |
| `config/` | `SecurityConfig`, `WebSocketConfig`, `RedisSequenceGenerator` |
| `controllers/` | `DinstarController`, `AdminController`, `AdminMonitorController`, `DuminTelemetryController`, `HealthController` |
| `infrastructure/dinstar/` | `DinstarMasterClient` (عميل بوابة Dinstar) |
| `messaging/` | `MessageService`, `AdvancedMessageService`, `DeleteService` |
| `pstn/` | `DinstarMasterService`, `DinstarLoadBalancer`, `DinstarEventListener`, `PstnManager` |
| `services/` | `DinstarHardwareService`, `IronSyncService`, `MasterStatsService`, `RedSecurityService`, `SearchService`, `CoreService` |
| `storage/` | `StorageMonitorService` |
| `websocket/` | `RedMasterHandler`, `ChatWebSocketHandler`, `RedWebSocketHandler`, `AdminLogHandler`, `TypingHandler`, `CallWebSocketHandler` |

### أيضًا: حزمة `com.developedchat.*` (الجيل القديم)
`admin/MonitorController`, `auth/AuthController`, `core/pstn/DuminGatewayService`, `core/pstn/PstnRelayService`, `core/storage/StorageController`, `core/websocket/ChatWebSocketHandler` — نسخ قديمة/متوازية من نفس المفاهيم.

---

## 🔌 كل الـ API Endpoints

### المصادقة والموافقات
| الطريقة | المسار | الوظيفة |
|---|---|---|
| POST | `/api/auth/register` | تسجيل → PENDING |
| POST | `/api/auth/login` | دخول → JWT (إذا APPROVED) |
| GET | `/api/auth/status` | حالة المستخدم |
| GET | `/api/admin/users/pending` | قائمة الانتظار |
| POST | `/api/admin/users/update-status?userId=&status=` | موافقة/حظر/رفض |

### Dinstar (بوابة الهاتف GSM)
| الطريقة | المسار | الوظيفة |
|---|---|---|
| GET | `/api/admin/dinstar/status` | حالة 8 منافذ SIM |
| POST | `/api/admin/dinstar/dial` | بدء مكالمة PSTN |
| GET | `/api/admin/dinstar/discover` | اكتشاف تلقائي للبوابة |
| POST | `/api/admin/dinstar/reboot` | إعادة تشغيل الجهاز |
| POST | `/api/admin/dinstar/config/sip` | تعديل SIP Trunk |

### المراقبة والإدارة
| الطريقة | المسار | الوظيفة |
|---|---|---|
| GET | `/api/master/v1/stats/realtime` | إحصائيات حية (Redis + Mongo + Postgres) |
| GET | `/api/admin/monitor/stats` | إحصائيات اللوحة |
| GET | `/api/admin/dumin/telemetry` | تيليمتري البوابة |
| POST | `/api/master/v1/security/wipe?userId=` | مسح جهاز عن بعد (Kill Switch) |
| POST | `/api/admin/security/kill-switch?userId=` | أمر Kill Switch |
| GET | `/api/master/v1/hardware/dinstar/slots` | حالة الشرائح |
| GET | `/health` | فحص Mongo/Redis/Postgres |

### WebSockets
| المسار | الوظيفة |
|---|---|
| `/ws/master` | البروتوكول الرئيسي (RedProtos): رسائل/ACK/مزامنة/كتابة/حذف |
| `/ws/chat` | رسائل ChatProtos (Dedup→Sequence→Save→ACK→Forward) |
| `/ws/red` | نفس منطق chat |
| `/ws/admin/logs` | بث سجلات حية للوحة التحكم |

---

## 📨 خوارزمية توصيل الرسالة (مهمة للفهم)
```
استقبال Protobuf عبر WebSocket
→ 1) Dedup (Redis، TTL 24h)
→ 2) Sequence (Redis INCR — red:seq:<conversationId>)
→ 3) حفظ في MongoDB (payload مشفر — E2EE)
→ 4) إرسال ACK للمرسل
→ 5) توجيه للمستلم (أو تخزين إن لم يكن متصلًا)
```

---

## 🗄️ قواعد البيانات (ثلاثية)

| قاعدة | الاستخدام |
|---|---|
| **MongoDB** | الرسائل (`messages`، `sequenceNumber` مفهرس) + القصص (`stories`، تنظيف كل دقيقة) |
| **PostgreSQL** | المستخدمون + المجموعات + منافذ Dinstar (Flyway: V1 + V2) |
| **Redis** | أرقام تسلسل + حضور + Pub/Sub (قنوات typing/wipe/kill-switch) + Dedup |

---

## 🚀 التشغيل
```bash
# محليًا:
./gradlew bootRun          # يحتاج Mongo + Postgres + Redis

# عبر Docker (مُستخدم في compose):
docker build -t red-backend .
```

## ⚠️ ملاحظات فنية (نقاط ضعف)
1. **تعارض `MessageDocument`**: 4 تعريفات بنفس الاسم (اثنان في نفس الحزمة `com.red.server.database`) — لا يُترجم بدون إصلاح
2. **`RedApprovalService.getPendingList()` غير معرّف** رغم استدعائه من 3 Controllers
3. **الأمان**: كل `/api/**` مفتوح (permitAll)، JWT غير مفعّل فعليًا، CORS `*`
4. `@Value("${ASTERISK_IP}")` بلا قيمة افتراضية — يمنع بدء التشغيل بدون env
5. إحصائيات كثيرة hardcoded (85,420 رسالة...) وليست حقيقية
