# platform/ci/ — تعريف بوابة التحقق

`verification.yml` هو تعريف CI المقصود: فاحص الفصل أولًا، ثم مطابقة compose للعقد، ثم صورة
التحقق الكاملة (`platform/containers/ci.Dockerfile`).

## لماذا لا يوجد في `.github/workflows/`؟

التوكن المستخدم في هذه الجلسة لا يملك صلاحية `workflows` على المستودع، فأي تعديل على
`.github/workflows/docker-image.yml` يُرفض عند الدفع. الحل المؤقت:

- بقي الملف الحالي كما هو (يبني `./Dockerfile`).
- `./Dockerfile` في الجذر **مرآة بايت-ببايت** لـ `platform/containers/ci.Dockerfile`،
  والـ guard يفشل إذا اختلفا (`integration:ci-mirror`).
- بعد أي تعديل على صورة التحقق: `tools/ci/sync-ci-dockerfile.sh`.

## التفعيل النهائي

عند توفر صلاحية `workflows`، انسخ `verification.yml` إلى
`.github/workflows/verification.yml`، ثم احذف `.github/workflows/docker-image.yml`
و`./Dockerfile` و`tools/ci/`. الفاحص سيمنع بقاء أي Dockerfile خارج `platform/containers/`.
