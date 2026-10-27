#!/usr/bin/env sh
# Generate the local identity authority (ECDSA P-256) used to sign device
# authorization certificates. Private material stays under platform/secrets/,
# which is ignored by Git and mounted read-only into the backend container.
set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
REPO_ROOT="$(CDPATH= cd -- "$SCRIPT_DIR/../.." && pwd)"
OUT="$REPO_ROOT/platform/secrets"
PRIVATE="$OUT/red_identity_private_key.pem"
PUBLIC="$OUT/red_identity_public_key.pem"

command -v openssl >/dev/null 2>&1 || {
  echo "openssl is required" >&2
  exit 1
}

mkdir -p "$OUT"
umask 077
if [ -e "$PRIVATE" ] || [ -e "$PUBLIC" ]; then
  echo "Identity authority keys already exist; refusing to overwrite them." >&2
  exit 1
fi

openssl genpkey -algorithm EC -pkeyopt ec_paramgen_curve:P-256 -out "$PRIVATE"
openssl pkey -in "$PRIVATE" -pubout -out "$PUBLIC"
chmod 600 "$PRIVATE"
chmod 644 "$PUBLIC"
chmod 700 "$OUT"
printf 'Created local RED identity authority keys in %s\n' "$OUT"
printf 'Back up the private key securely; never commit or send it to a client.\n'
