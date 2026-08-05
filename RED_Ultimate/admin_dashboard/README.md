# `admin_dashboard/` — لوحة التحكم الإدارية (النسخة الكاملة النشطة)

لوحة تحكم React تسمح للمدير بمراقبة المنظومة وإدارتها: الموافقة على المستخدمين، التحكم ببوابة Dinstar، الإحصائيات الحية، الأمان (Kill Switch)، وسجلات النظام.

> **النسخة النشطة والمعتمدة** (النسخة `admin-dashboard` ذات الواصلة هي نموذج قديم).

---

## 📊 أرقام

| المكوّن | القيمة |
|---|---|
| Framework | React 19 |
| UI | Ant Design 5.22 (antd) |
| الرسوم | ECharts 5.5 + echarts-for-react |
| الشبكة | `fetch` الأصلي (لا axios) |
| النشر | Dockerfile (node:22-alpine + serve :3000) |

---

## 🖥️ الصفحات (6 في القائمة الجانبية)

| الصفحة | الوظيفة | الـ API |
|---|---|---|
| **Dashboard** (`Dashboard.tsx`) | إحصائيات حية + رسم Traffic + صحة الخادم (تحديث 5 ثوانٍ) | `GET /api/admin/monitor/stats` |
| **Master Control** (`MasterLayout.tsx`) | قشرة 7 تبويبات: Overview, Authority, Messaging, Dinstar, Media SFU, Security, Infrastructure | — |
| **User Management** (`UserApproval.tsx`) | جدول موافقات المستخدمين (Approve/Reject/Ban + Modal تأكيد) | `GET /api/admin/users/pending` + `POST /api/admin/users/update-status` |
| **DINSTAR Control** (`DinstarControl.tsx`) | مركز قيادة بوابة UC2000: 8 بطاقات SIM + Reboot + سجل مكالمات | `GET /api/admin/dinstar/status` + `POST /api/admin/dinstar/reboot` |
| **Live Monitor** (`MasterOverview.tsx`) | "Command Center": نظام C + سلطة + نظام B + قاعدة بيانات (3 ثوانٍ) | `GET /api/master/v1/stats/realtime` |
| **Diagnostics** (`Diagnostics.js`) | تشخيص VoIP/PSTN/Messaging/MinIO | `GET /api/master/v1/stats/realtime` |

## 📑 التبويبات (`pages/tabs/`)
| التبويب | الوظيفة | الـ API |
|---|---|---|
| `OverviewTab` | حالة النظام + البنية التحتية (Mongo/Postgres/Redis) | `/api/master/v1/stats/realtime` |
| `AuthorityTab` | موافقات (Approve/Reject منفصلان) | `/api/admin/users/pending` + `approve/reject` |
| `MessagingTab` | مركز الرسائل: معدل تسليم، زمن استجابة | `/api/master/v1/stats/realtime` |
| `DinstarTab` | حالة 8 شرائح + Manual Sync (5 ثوانٍ) | `/api/master/v1/hardware/dinstar/slots` |
| `SecurityTab` | مركز الأمن: **KILL SWITCH** + **Remote Wipe** | `/api/master/v1/security/wipe/{userId}` |
| `LogStreamerTab` | **بث سجلات حية عبر WebSocket** (أسود/أخضر) | `ws://{host}:8080/ws/admin/logs` |

## 📄 صفحات أخرى
| الصفحة | الوظيفة |
|---|---|
| `Approvals.js` | نسخة أبسط من الموافقات (قديمة) |
| `DuminAdvanced.tsx` | تيليمتري عتاد System B (5 ثوانٍ) — `GET /api/admin/dumin/telemetry` |
| `DinstarMonitor.js` | شاشة ثابتة Mock (لا fetch) |
| `DuminMonitor.js` | شاشة ثابتة Mock |
| `MasterControl.tsx` | إحصائيات + 8 فتحات Dinstar (3 ثوانٍ) |
| `components/LiveMonitor.js` | 3 بطاقات مراقبة حية (2 ثانية) |

---

## 🔌 الاتصال بالخادم

```
React (fetch + setInterval كل 2-5 ثوانٍ)
  → /api/... عبر nginx → backend-server:8080
WebSocket: /ws/admin/logs مباشرة على المنفذ 8080
```

- كل المسارات نسبية `/api/...` — في الإنتاج يمر عبر nginx
- **لا مصادقة في الواجهة** حاليًا

---

## 🚀 التشغيل
```bash
npm install
npm start          # تطوير (يحتاج CORS أو nginx)
# أو عبر Docker (من الجذر): docker-compose up admin-panel
```

## ⚠️ ملاحظات فنية
1. بعض الـ endpoints غير مطابقة للخادم: `Approvals.js` يستدعي `/api/admin/pending-users` (غير موجود — الصحيح `/api/admin/users/pending`)
2. `SecurityTab` يستدعي wipe بمسار مختلف عن الخادم (`{userId}` مقابل `?userId=`)
3. لا مصادقة — أي شخص يعرف العنوان يتحكم بالنظام
