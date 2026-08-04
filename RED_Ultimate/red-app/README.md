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

## Navigation and call separation

The five bottom destinations are:

1. Posts: stories, For You/Following/Yemen filters, composer and rich-post action layout.
2. Chats: private chats and groups as internal tabs.
3. Central Create action: post/thread, 24-hour story, live broadcast or audio Space.
4. Unified Calls: one future log for RED voice/video, groups, live, Spaces and PSTN, each with a route badge.
5. Phone: an isolated gold DINSTAR area with keypad, favourites, PSTN history and contacts; voice only.

RED-to-RED voice/video uses WebRTC and requires no SIM or phone number. DINSTAR is a separate, administrator-authorized PSTN voice route. The UI deliberately disables media actions until their engines are connected instead of simulating success.

## Current boundary

Registration, approval, real libsignal enrollment keys, secure local secrets, rotating sessions, foreground WebSocket transport, durable local ciphertext storage with SENT/DELIVERED/READ ACKs, synchronized local/public feed and authorized PSTN dialing are implemented. The feed supports cursor pagination, transparent Following/Yemen scopes, follow/unfollow, threads/replies, quotes, polls and idempotent reactions at the API layer; synchronized media UI remains gated. The current SQLite message store contains ciphertext plus routing metadata; SQLCipher protection for metadata is still gated. Authenticated streaming uploads to MinIO and 24-hour story metadata/view cleanup are implemented, while the Android image/video renderer is still gated. Full libsignal session stores, safety-number UI, plaintext view-model cache and WebRTC/SFU media remain gated work items; incomplete controls are visibly disabled.
