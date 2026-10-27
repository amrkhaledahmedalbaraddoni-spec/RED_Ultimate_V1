# W0 — RED canonical module boundaries

This file is the build and ownership gate for RED. A second implementation of any row below must not be added without an architecture decision.

| Capability | Canonical implementation | Status of alternatives |
|---|---|---|
| Android product | Gradle module `:app` mapped to `services/android/` | legacy `app/`, `android/`, `app-android/` and the Signal libraries are **removed from the tree** (git history only) |
| UI identity | AQYAL Arabic design system | Signal screens are reference material, not the product identity |
| HTTP/WebSocket backend | `services/backend/` | old `server/` and `com.developedchat` removed |
| Admin UI | `services/admin-dashboard/` | old `admin-dashboard/` removed |
| Messaging protocol | `services/shared-proto/src/main/proto/red_protocol.proto` | legacy `messages.proto`, `/ws/chat`, and `/ws/red` removed |
| Chat WebSocket | `/ws/master` using `RedProtos.RedRED` | no second ACK/message envelope allowed |
| Call signaling/media | `/ws/calls` + WebRTC + `services/media-sfu/` | SIP must not be exposed to Android clients |
| Yemeni PSTN | `platform/telephony/` → DINSTAR | voice only unless verified hardware evidence proves otherwise |
| Durable identities/approval | PostgreSQL in `services/backend/auth` | no in-memory account store |
| Media storage | local MinIO (private buckets, pre-signed URLs) | no Signal CDN dependency |

## Migration gates

1. Legacy `app/` stays outside the build until `services/android/` initializes, registers, waits for approval, and exchanges an encrypted message.
2. Useful AQYAL files are copied from `android/` into the canonical application with tests; `android/` itself is never included in `settings.gradle.kts`.
3. Legacy modules are no longer present in the tree; recover a file from git history, review its license, and add it with tests under `services/android/`.
4. Signal code is removed only after each selected library/schema has an explicit dependency or extraction record. Git history is the archive; do not duplicate thousands of files under an `archive/` folder.
5. The default build and CI may reference canonical modules only.

## Identity invariants

- No phone number, E.164 identity, SMS, or OTP is required.
- Account cryptographic address is an immutable UUID; RED ID is the public application identifier.
- Private identity keys are generated and stored on the client and never uploaded.
- Registration uploads public identity/pre-key material; all devices remain pending until administrator approval.
- Approval issues an Ed25519 device authorization certificate. Safety numbers and key-change warnings remain required; the certificate does not replace end-to-end identity verification.
