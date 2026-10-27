# platform/ — البنية التحتية (Infrastructure plane)

> **القاعدة:** هذا المجلد يصف *كيف* يُبنى المشروع ويُشغَّل ويُعرَض ويُراقَب.
> لا يحتوي أي كود منتج، ولا أي مخطط بيانات، ولا أي سرّ حقيقي.

## المحتويات

| المسار | الغرض |
|---|---|
| `compose/docker-compose.yml` | تعريف 13 خدمة وشبكة `red-net` (172.28.0.0/16) والـ volumes |
| `containers/` | `backend.Dockerfile`, `admin.Dockerfile`, `sfu.Dockerfile`, `pstn.Dockerfile`, `ci.Dockerfile` |
| `proxy/nginx.conf` | البوابة الوحيدة: `/api`, `/ws`, `/sfu`, `/minio`, `/grafana`, SPA |
| `proxy/dashboard.conf` | تقديم SPA ساكن لمنصة الإدارة (لا يعرف عناوين backend) |
| `proxy/certs/` | شهادات TLS (غير محفوظة في Git) |
| `monitoring/prometheus.yml` + `monitoring/grafana/` | القياس واللوحات |
| `telephony/` | إعداد Asterisk + entrypoint الخاص بـ DINSTAR (صوت فقط) |
| `config/.env.example` | **مولَّد** من `contracts/env.schema.json` |
| `secrets/` | مفاتيح سلطة الهوية (وقت التشغيل فقط، غير محفوظة) |

## قواعد ثابتة

1. لا يُضاف Dockerfile خارج `platform/containers/` (إلا أداة مستقلة في `tools/reproducible-builds/`).
2. لا يُشغَّل `docker compose` مباشرة بلا `--env-file`؛ استخدم `tools/scripts/red.sh`.
3. كل منفذ/volume/خدمة هنا يجب أن يطابق `contracts/stack.json`؛ الفاحص يفشل عند الاختلاف.
4. الأسرار لا تُكتب في أي ملف هنا: `.env` و`secrets/` غير محفوظين في Git.

## التحقق

```bash
python3 tools/guard/separation_guard.py
docker compose --env-file platform/config/.env -f platform/compose/docker-compose.yml config --quiet
```
