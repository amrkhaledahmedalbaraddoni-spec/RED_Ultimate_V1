# RED Android application

This directory is the canonical Gradle `:app` module. The legacy Signal fork in `../app/` is intentionally outside the build graph.

## Local server address

The debug default is `http://192.168.1.50`. Override it without editing source:

```bash
./gradlew :app:assembleDebug -PRED_SERVER_URL=http://YOUR_LOCAL_SERVER_IP
```

Debug builds permit cleartext HTTP for LAN development. Release builds disable cleartext and must use an HTTPS URL.

## Implemented flow

1. User enters display name, username and password; no phone, SIM, email, SMS or OTP.
2. The app generates a real libsignal identity key, signed EC pre-key and Kyber-1024 pre-key locally.
3. Private identity key and session tokens are encrypted with an AES-GCM key held by Android Keystore.
4. Only public key material is sent to `/api/auth/register`.
5. The account/device remain pending until the administrator approves and signs the device fingerprint.
6. Login uses the stored device ID and receives rotating access/refresh tokens.
7. `/ws/master` uses the shared binary `RedProtos.RedRED` protocol with an Authorization header.

## Current boundary

The registration/approval/session shell and WebSocket transport are implemented. Full libsignal session stores, safety-number UI, encrypted Room message persistence, background socket service, media SFU and PSTN call orchestration remain separate gated work items; the UI does not claim these are complete.
