# tools/ — أدوات التشغيل والفحص

| الأداة | الغرض |
|---|---|
| `guard/separation_guard.py` | فاحص الفصل والتكامل (stdlib فقط، بلا Docker) |
| `env/render_env.py` | توليد `platform/config/.env(.example)` من `contracts/env.schema.json` |
| `scripts/red.sh` | نقطة التشغيل الوحيدة (env/up/down/ps/logs/guard/config/build/backup) |
| `scripts/local-first-run.sh` | تجربة أولى كاملة على Linux/macOS (تستدعي `red.sh`) |
| `scripts/local-first-run.ps1` | نفس التجربة على Windows PowerShell |
| `scripts/backup.sh` | نسخ PostgreSQL/MongoDB/Redis/MinIO إلى `data/runtime/backups` |
| `scripts/restore.sh` | استعادة مع تحقق إلزامي |
| `scripts/generate-local-identity-authority.sh` | مفاتيح سلطة هوية ECDSA P-256 محلية |
| `scripts/build-android-local.ps1` · `install-android-local.ps1` · `configure-windows-lan.ps1` · `prefetch-android-crypto.ps1` | مساعدات Windows/شبكة/APK |
| `audit/audit_check.py` | تدقيق سريع على بنية المستودع |
| `reproducible-builds/` | أدوات مقارنة APK (خارج مسار البناء) |

## الاستخدام

```bash
python3 tools/guard/separation_guard.py --verbose
python3 tools/env/render_env.py --example
tools/scripts/red.sh up 192.168.1.50
tools/scripts/backup.sh
```

> لا تُشغّل `docker compose` مباشرة للعمليات اليومية؛ `red.sh` يضمن
> `--env-file` الصحيح وملف compose الصحيح والتحقق من الصحة.
