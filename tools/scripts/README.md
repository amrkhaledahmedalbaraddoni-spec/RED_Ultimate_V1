# tools/scripts/ — التشغيل والأتمتة

> **الحالة:** نشط. نقطة التشغيل اليومية هي `red.sh` فقط.

| السكربت | الغرض |
|---|---|
| `red.sh` | env / up / down / ps / logs / config / build / guard / backup / restore / key |
| `local-first-run.sh` | غلاف رقيق لتشغيل أول تجربة على Linux/macOS (`red.sh up`) |
| `local-first-run.ps1` | نفس التجربة على Windows PowerShell |
| `backup.sh` | نسخ PostgreSQL + MongoDB + Redis + MinIO إلى `data/runtime/backups/<UTC>` |
| `restore.sh` | استعادة تتطلب `RED_RESTORE_CONFIRM=yes` ثم قائمة تحقق يدوية |
| `verify-separation.sh` | غلاف لـ `tools/guard/separation_guard.py` (يُستخدم في CI/pre-commit) |
| `generate-local-identity-authority.sh` | مفاتيح سلطة هوية ECDSA P-256 في `platform/secrets/` |
| `build-android-local.ps1` · `install-android-local.ps1` | بناء/تثبيت APK من صورة التحقق |
| `configure-windows-lan.ps1` · `prefetch-android-crypto.ps1` | جدار حماية Windows · تحميل موثّق بـ SHA-256 لتبعيات libsignal |

## أمثلة

```bash
tools/scripts/red.sh env 192.168.1.50
tools/scripts/red.sh up 192.168.1.50
tools/scripts/red.sh guard --verbose
tools/scripts/backup.sh
RED_RESTORE_CONFIRM=yes tools/scripts/restore.sh data/runtime/backups/20261027T101500Z
```

## قواعد

1. لا `docker compose` مباشرة للعمليات اليومية: `red.sh` يضمن `--env-file` وملف compose الصحيحين.
2. كل السكربتات تحلّ جذر المستودع من موقعها (`../..`)، فلا تعتمد على مجلد العمل.
3. لا تكتب سرًّا في سكربت: الأسرار تأتي من `platform/config/.env` (مولَّد من العقد).
