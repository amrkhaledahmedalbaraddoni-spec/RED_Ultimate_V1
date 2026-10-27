# أرشيف التحليل (قبل إعادة الهيكلة)

هذه التقارير كُتبت عندما كان المشروع في `RED_Ultimate/` مع وحدات Signal داخل المستودع،
قبل فصل الطبقات الثلاث. تُحفظ لأنها تسجل: الأخطاء المكتشفة، القرارات، ومبررات الإصلاحات.

| الملف | ما يستفاد منه اليوم |
|---|---|
| `TECHNICAL_REPORT_AR.md` | جرد أعطال البناء والأمان التي أُصلحت لاحقًا |
| `FULL_PROJECT_UNDERSTANDING_AR.md` | فهم تفصيلي للمكوّنات القديمة |
| `VERIFICATION_REPORT_AR.md` · `VERIFICATION_UNIFIED_AR.md` | بوابات التحقق المطلوبة |
| `UNIFIED_INTEGRATION_AR.md` · `ARCHITECTURE_DECISION_AR.md` | قرارات الدمج والبنية |
| `ACTION_PLAN_AR.md` | خطة الإصلاح المرحلية |
| `FINAL_SUMMARY.md` · `DEPENDENCY_POLICY.md` | ملخص الحالة وسياسة الاعتماديات |
| `LEGACY-README-AR.md` | دليل `RED_Ultimate/` القديم (أُخرج من الجذر) |

> ⚠️ المسارات والإحصاءات داخل هذه الملفات **تاريخية** ولا تُطابق الشجرة الحالية.
> المرجع الحالي هو [`../../ARCHITECTURE_SEPARATION_AR.md`](../../ARCHITECTURE_SEPARATION_AR.md).

## ما أُخرج من المستودع في إعادة الهيكلة

وحدات Signal التاريخية (`app/` 6,956 ملفًا، `lib/`, `feature/`, `core/`, `demo/`,
`app-android/`, `android/`, `fast-lint/`, `lintchecks/`, `benchmark/`, `microbenchmark/`,
`baseline-profile/`) أُزيلت من الشجرة وتبقى في تاريخ Git عند
`RED_Ultimate/…` في الوسم/الالتزام السابق لإعادة الهيكلة. استرجاع ملف محدد:

```bash
git log --diff-filter=D --name-only -- 'RED_Ultimate/lib/**' | head
git show <commit>:RED_Ultimate/lib/network/src/main/java/…/SomeFile.kt > /tmp/SomeFile.kt
```
