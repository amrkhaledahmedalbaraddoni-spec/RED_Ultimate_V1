# `core/` — الوحدات الأساسية (7 وحدات)

المكتبات الأساسية المشتركة للمشروع — **شيفرة Signal الأصلية** (حزم `org.signal.*`) دون تعديل يُذكر. يستخدمها التطبيق الرئيسي `app/` وكل الوحدات الأخرى.

---

## 🗂️ الوحدات

| الوحدة | النوع | الغرض | الملفات الهامة |
|---|---|---|---|
| **`:core:models`** | Android | نماذج البيانات القابلة للتسلسل (وسائط، مجلدات) | `media/Media.kt`, `UriSerializer.kt` |
| **`:core:models-jvm`** | JVM | نماذج JVM للتشفير والمفاتيح | `AccountEntropyPool.kt`, `MasterKey.kt`, `ServiceId.kt`, `backup/BackupId.kt` |
| **`:core:network`** | JVM + Wire | نواة الشبكة: URLs، TLS، WebSocket | `NetworkResult.kt`, `config/SignalServiceConfiguration.kt`, `rest/`, `websocket/` |
| **`:core:serialization`** | JVM | محولات تسلسل المفاتيح (kotlinx) | `ECPublicKeyToBase64Serializer.kt`, `KEMPublicKeyToBase64Serializer.kt` |
| **`:core:ui`** | Android + Compose | مكونات Compose والثيمات المشتركة | `compose/theme/SignalTheme.kt`, `compose/Buttons.kt`, `fonts/SignalSymbols.kt` |
| **`:core:util`** | Android | أدوات شاملة (تنفيذ، تشفير، Billing، SQLite) | `concurrent/SignalExecutors.java`, `crypto/AttachmentSecret.java`, `billing/BillingApi.kt` |
| **`:core:util-jvm`** | JVM | أدوات JVM نقية (اختبارية) | `Base64.kt`, `Hex.java`, `crypto/DeviceNameCipher.kt`, `UuidUtil.kt`, `E164Util.kt` |

---

## 📌 ملاحظات فنية
- وحدات JVM: `java-toolchain = 21`
- تستخدم بلجن `ktlint` + توليد بروتوكول Wire (`generateMainProtos`)
- `core/network` يولّد كودًا من `protowire/WebSocketResources.proto`
- `core/util` يحوي JNI: `jniLibs/libnative-utils.so`

---

## 🔗 العلاقة
- تستخدمها وحدة `app/` عبر `:core:util`, `:core:ui` (في `dependencies.gradle.kts`)
- وحدات `lib/*` تعتمد عليها جميعًا
