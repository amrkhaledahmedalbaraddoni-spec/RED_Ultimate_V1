# RED username registration and administrator approval

RED accounts never require a phone number, SIM card, OTP, or SMS.

## Required local environment

Set the following before startup (see the repository `.env.example`):

- `JWT_SECRET`: at least 32 random characters.
- `RED_ADMIN_USERNAME`: initial administrator username.
- `RED_ADMIN_PASSWORD`: at least 14 characters.
- Database, Redis, MongoDB, Asterisk and DINSTAR secrets.

The initial administrator is created only when the configured username does not already exist.

## 1. Register

`POST /api/auth/register`

```json
{
  "username": "ahmed.red",
  "password": "a-long-private-password",
  "displayName": "أحمد"
}
```

The response is HTTP `201` with a generated ID such as `RED-7K4M-82QX` and status `PENDING`. No access token is issued.

## 2. Login while pending

`POST /api/auth/login`

```json
{
  "username": "ahmed.red",
  "password": "a-long-private-password"
}
```

A pending account receives HTTP `423` and `ACCOUNT_PENDING_ADMIN_APPROVAL`. Rejected, suspended and banned accounts receive HTTP `403`.

## 3. Administrator approval

First log in with the bootstrap administrator. Use its Bearer token for these requests.

`GET /api/admin/users/pending`

`POST /api/admin/users/action`

```json
{
  "userId": "ACCOUNT-UUID-FROM-PENDING-LIST",
  "action": "APPROVED",
  "reason": null
}
```

Supported actions: `APPROVED`, `REJECTED`, `SUSPENDED`, `BANNED`. The approving administrator and approval timestamp are stored.

## 4. Login after approval

The same login request now returns HTTP `200`, a short-lived Bearer access token, and the account's RED ID.

Use `Authorization: Bearer <token>` with protected HTTP APIs and WebSocket upgrade requests. Only approved accounts can complete a RED WebSocket handshake.

## Messaging endpoints

- `/ws/master`: the only chat protocol; binary `RedProtos.RedRED` envelopes.
- `/ws/calls`: JSON WebRTC signaling only.
- `/ws/admin/logs`: administrator monitoring stream.

The removed `/ws/chat` and `/ws/red` endpoints used an incompatible legacy ACK protocol and must not be used by new clients.
