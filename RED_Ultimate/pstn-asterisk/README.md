# `pstn-asterisk/` — بوابة الهاتف (System B)

إعدادات **Asterisk PBX** — الجسر بين التطبيق (WebRTC) وشبكة الجوال اليمنية عبر بوابة **DINSTAR UC2000**. مسؤول عن **System B** (مكالمات GSM/PSTN).

---

## 📁 الملفات

| الملف | الوظيفة |
|---|---|
| `Dockerfile` | صورة `andrius/asterisk` + نسخ الإعدادات + المنافذ |
| `extensions.conf` | منطق التوجيه (Dialplan) |
| `manager.conf` | واجهة إدارة AMI (مستخدم `red_admin`) |
| `pjsip.conf` | إعدادات SIP الرئيسية |
| `pjsip_dinstar.conf` | نسخة بديلة مبسطة (احتياطية) |

## 🔌 المنافذ
| المنفذ | الاستخدام |
|---|---|
| 5060/udp + tcp | SIP (الوسيط والبوابة) |
| 5038 | AMI (تحكم الخادم عبر asterisk-java) |
| 8088 | HTTP/AJAM |

---

## 🔄 منطق التوجيه (`extensions.conf`)
```
[from-internal]  ← المكالمات الصادرة: Dial(PJSIP/${EXTEN}@dumin-trunk)
                  (كل رقم → جسر Dumin/Dinstar GSM)
[from-pstn]      ← المكالمات الواردة من Dinstar: → PJSIP/webrtc-client
                  (إلى تطبيق المستخدم)
```

## 🎛️ الإعدادات الرئيسية (`pjsip.conf`)
- **`dinstar-gateway`**: الجهاز الفيزيائي عند `sip:192.168.1.100:5060` — الترميزات `g729, alaw, ulaw, gsm` (g729 لأداء أفضل في اليمن)
- **`webrtc-client`**: نقطة نهاية التطبيق — `webrtc=yes`, `dtls_auto_self_signed=yes`, `ice_support=yes`, الترميزات `opus, vp9, av1`، مصادقة `webrtc-client/red_secure_pass`
- **AMI**: `red_admin` / `red_secret_123` (صلاحيات all)

---

## 🔗 كيف يعمل (التدفق الكامل)
```
المستخدم (تطبيق RED)
   → WebRTC (opus/vp9) عبر webrtc-client
   → Asterisk يترجم للترميزات (g729/alaw)
   → DINSTAR UC2000 (192.168.1.100)
   → شبكة الجوال اليمنية (يمن موبايل / سبأفون)
```
الخادم `backend-server` يتحكم في Asterisk عبر AMI (`DefaultManagerConnection` + `OriginateAction`).

## 🚀 التشغيل
```bash
# عبر Docker Compose:
docker-compose up pstn-gateway
# يحتاج DINSTAR متصلًا على 192.168.1.100 مع شرائح SIM
```

## ⚠️ ملاحظات
- كلمة مرور AMI افتراضية مكشوفة في الملف — يجب نقلها إلى `.env`
- `g729` ملكية فكرية — يحتاج ترخيصًا في بعض المناطق
