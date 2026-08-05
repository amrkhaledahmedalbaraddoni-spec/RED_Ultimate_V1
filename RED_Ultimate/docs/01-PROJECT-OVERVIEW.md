# 01 — المشروع كاملًا: RED Ultimate

> وثيقة شاملة تشرح المشروع من الرؤية حتى الملفات. للملفات التفصيلية لكل مجلد راجع الفهرس في `README.md`.

---

## 1. ما هو RED Ultimate؟

**منظومة اتصالات "سيادية" كاملة** — مراسلة + مكالمات صوت/فيديو + مكالمات هاتف GSM — تعمل **محليًا على خادم خاص** دون الاعتماد على الإنترنت الخارجي أو خدمات جوجل. المشروع مبني على **فورك كامل من Signal-Android** (أشهر تطبيق مراسلة مشفر عالميًا) مع طبقة "RED" مضافة فوقه.

**نقطة البداية**: `app/` يحوي 3,768 ملف Signal أصلي (حزم `org.thoughtcrime.securesms`) + 31 ملف RED مخصص (حزم `com.red.sovereign`).

---

## 2. الأنظمة الثلاثة (فكرة المشروع المركزية)

| النظام | الوظيفة | التقنية | الملفات المسؤولة |
|---|---|---|---|
| **System A** | مكالمات صوت/فيديو عالية الجودة (VoIP) | WebRTC + خادم وسائط Mediasoup | `media-sfu/` (خادم) + `feature:camera`, `android/features/calls` (تطبيق) |
| **System B** | مكالمات هاتف GSM/PSTN يمنية | بوابة DINSTAR UC2000 (8 شرائح) + Asterisk PBX | `pstn-asterisk/` (خادم) + `backend-server` (تحكم AMI) + شاشة `PstnDialerScreen` |
| **System C** | رسائل مشفرة بتوصيل مضمون | WebSocket + Protobuf + Room + MongoDB/Redis | `backend-server` (خادم) + `shared-proto/` (الاتفاقية) + `app` (العميل) |

**الرابط بينها**: تطبيق واحد يعرضها كلها — المكالمات VoIP والـ GSM في نفس قائمة المكالمات، والرسائل كلها في شاشة محادثة واحدة.

---

## 3. الميزات الرئيسية

- ✅ تسجيل بموافقة إدارية (Approval Flow) — لا أحد يدخل دون إذن المدير
- ✅ قصص تنتهي 24 ساعة (تُرفع إلى MinIO)
- ✅ رسائل ذاتية التدمير (Burn)
- ✅ مزامنة الرسائل المفقودة (Sequence Sync عبر `SyncRequest` + أرقام تسلسل من Redis)
- ✅ لوحة تحكم إدارية كاملة (React) مع **KILL SWITCH** و**Remote Wipe** ومراقبة حية
- ✅ رفع وسائط إلى MinIO (S3 محلي)
- ✅ ثيم فاخر "AQYAL Sovereign" (ذهبي/أوبسيديان/أزرق ملكي)

---

## 4. الخريطة المعمارية (كل مكون يبني فين)

```
                            RED_Ultimate_V1-main/
                            └── RED_Ultimate/          ← جذر المشروع
    ┌────────────┬────────────┬────────────┬──────────────┬──────────────┐
    ▼            ▼            ▼            ▼              ▼              ▼
 التطبيقات    الخوادم     لوحات التحكم   البنية التحتية   وحدات الكود   الجودة والأداء
 ─────────   ─────────   ────────────   ─────────────   ────────────   ─────────────
 app/        backend-    admin_dashboard/ media-sfu/     core/ (7)     lintchecks/
 (الرئيسي)   server/     (النشطة)        pstn-asterisk/ lib/ (18)     fast-lint/
 android/    server/     admin-dashboard/ infrastructure/ feature/ (3) benchmark/
 (AQYAL)     (قديم)      (قديمة)         reproducible-   demo/ (12)    microbenchmark/
 app-android/                          builds/          shared-proto/  baseline-profile/
 (DevelopedChat)                       wire-handler/    gradle/        build-logic/
```

## 5. الهرمية (من يعتمد على من)

```
                       app/  (تطبيق الإنتاج — Signal + RED)
                         │
           ┌─────────────┼─────────────┐
           ▼             ▼             ▼
     feature/ (3)   lib/ (18)    core/ (7)
     (ميزات)        (مكتبات)     (أساسيات)
           │             │             │
           └─────────────┴─────────────┘
                     build-logic/ (بلجنات البناء)
                     gradle/ (الإصدارات)
```

**خارج الهرمية** (مستقلة): `backend-server` + `media-sfu` + `pstn-asterisk` + `admin_dashboard` (خوادم) — `demo/` (عينات مستقلة).

---

## 6. المكونات الثلاثة المهمة (قرارات معمارية)

| المكوّن | التفاصيل |
|---|---|
| **wire-handler/** | أداة build-time توليد كود Wire — تحويلة ذكية لترقية دالة في الكود المولّد دون إعادة توليد |
| **reproducible-builds/** | بناء APK متطابقة بايت-بايت (Code Transparency) — التحقق أن الموزَّع = المصدري |
| **build-logic/** | بلجنات `signal.*` من Signal الأصلية (ktlint, licenses, translations, verification) + أدوات (StaticIpResolver للبناء القابل للتكرار) |

---

## 7. الوضع الحالي (حسب التقارير)

| البند | الحالة |
|---|---|
| البناء الكامل | ❌ غير قابل حاليًا (فئات مرجعية مفقودة، وحدات غير مسجلة في settings) |
| `app/` | ✅ معمارية سليمة — يعمل كفورك Signal بطبقة RED |
| الخوادم | ✅ تعمل لوحدها (Spring Boot + node) |
| الأمان | ⚠️ `/api/**` مفتوح + أسرار افتراضية — يحتاج تقوية |
| الميزات المعلنة | ⚠️ بعضها محاكاة (تشفير كمومي، 4K/AV1) |

**للتقييم التفصيلي**: `TECHNICAL_REPORT_AR.md` + `VERIFICATION_REPORT_AR.md` (في جذر المستودع).
