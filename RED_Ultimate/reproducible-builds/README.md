# `reproducible-builds/` — البناء القابل للتكرار

بيئة تهدف إلى **إنتاج APK متطابقة بايت-بايت في كل بناء** — للتحقق أن التطبيق الذي يُوزَّع هو نفس الكود المصدري (Code Transparency).

---

## 📁 المحتوى

```
reproducible-builds/
├── Dockerfile          ← بيئة بناء معزولة (Ubuntu 22.04 مثبتة بشعار sha256)
├── README.md           ← شرح عملية التحقق (موجود سابقًا)
├── apkdiff/            ← أداة مقارنة APK (Python)
└── docker/             ← إعدادات المرآة (sources.list + apt.conf)
```

## 🐳 `Dockerfile` — بيئة البناء
- أساس `ubuntu:jammy-20230624` **بشعار sha256** (تثبيت محدد)
- مرآة `mirror.signalusers.org` بدل Ubuntu الرسمية
- تثبيت: `git, openjdk-21-jdk-headless, unzip, wget`
- **Android SDK**: platform 37.0، build-tools 36.0.0، NDK 28.0.13004108، cmake 3.22.1 (مع تحقق sha256)
- `ENV HOME=/tmp` — ضروري لتكرار النتائج
- بناء: `./gradlew bundlePlayProdRelease`

## 🔍 `apkdiff/` — أداة المقارنة
- `apkdiff.py`: يقارن حزمتي APK **بايت-بايت**
- يتجاهل: `META-INF/*` (التوقيعات) و `stamp-cert-sha256`
- معالجة خاصة: `AndroidManifest.xml` (عبر androguard، تجاهل Metadata التي يضيفها Play) و `resources.arsc` (عبر aapt2)
- النتيجة: "APKs match!" أو "APKs don't match!" + كود خروج
- يتطلب: Python 3.12 + `androguard` (يُدار عبر `uv`)

## 🔄 عملية التحقق (من README الأصلي)
```
1. البناء داخل Docker
2. توليد APKs من AAB عبر bundletool
3. سحب APKs من الجهاز عبر adb
4. مقارنة: apkdiff.py → "APKs match!"
5. اختياري: bundletool check-transparency
```

---

## 🔗 العلاقة
- يخدم تطبيق `app/` (com.red.sovereign) — التحقق أن الـ Play Store يوزع الكود الحقيقي
- يعتمد على إعدادات إعادة الإنتاج في `gradle.properties` (R8 محدد، configuration-cache)
