# `media-sfu/` — محرك مؤتمرات الفيديو (System A)

خادم وسائط **Mediasoup (SFU)** — يدير مكالمات الفيديو والصوت بين مستخدمي التطبيق. الجزء المسؤول عن **System A** (VoIP عالي الجودة).

---

## 📊 أرقام

| المكوّن | القيمة |
|---|---|
| التقنية | Mediasoup 3.12 (WebRTC SFU) + ws 8.13 |
| المنفذ | **4000** (WebSocket) + **40000–40100/udp** (RTP) |
| اللغة | Node.js 22 |
| العمال | 2 Workers (توزيع الغرف Round-Robin) |

---

## 🧱 الترميزات المدعومة (في `server.js`)
- `audio/opus` (48kHz، قناتان)
- `video/VP8`
- `video/VP9` (profile-id 2)
- `video/H264` (packetization-mode 1)

> **ملاحظة**: لا يوجد AV1 في الترميزات الفعلية رغم الادعاءات في ملفات أخرى — الحالي يدعم حتى VP9/H264.

---

## 🔌 بروتوكول WebSocket (رسائل JSON)

| الرسالة | الوظيفة |
|---|---|
| `join` | إنشاء/الانضمام لغرفة + إنشاء WebRtcTransport + إرجاع rtpCapabilities |
| `connectTransport` | ربط DTLS |
| `produce` | بث وسيط (audio/video) + إشعار بقية الأعضاء (`newProducer`) |
| `consume` | الاشتراك في وسيط نظير آخر |
| `leave` | تنظيف الموارد |

عند إغلاق الاتصال: تنظيف تلقائي، وحذف الغرفة إذا أصبحت فارغة.

---

## 🚀 التشغيل
```bash
npm install        # يتطلب أدوات C++ (python3, build-essential)
node server.js
# أو عبر Docker: docker-compose up media-sfu
```

## 🔗 العلاقة
- يستدعيه التطبيق (System A) للتفاوض على وسائط المكالمات
- يعمل مع `coturn` (TURN على :3478) لعبور NAT
- في `docker-compose.yml` مكشوف على 4000 + نطاق UDP
