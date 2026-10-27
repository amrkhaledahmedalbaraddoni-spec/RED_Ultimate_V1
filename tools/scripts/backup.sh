#!/usr/bin/env sh
# Backup every durable store declared in contracts/data.json.
# Output goes to data/runtime/backups/<UTC timestamp>/ which is never committed.
set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
REPO_ROOT="$(CDPATH= cd -- "$SCRIPT_DIR/../.." && pwd)"
ENV_FILE="$REPO_ROOT/platform/config/.env"
COMPOSE_FILE="$REPO_ROOT/platform/compose/docker-compose.yml"
STAMP="$(date -u +%Y%m%dT%H%M%SZ)"
TARGET="$REPO_ROOT/data/runtime/backups/$STAMP"

fail() { printf 'ERROR: %s\n' "$*" >&2; exit 1; }
[ -f "$ENV_FILE" ] || fail "missing platform/config/.env — run tools/scripts/red.sh env first"
command -v docker >/dev/null 2>&1 || fail "docker is required"

mkdir -p "$TARGET"
chmod 700 "$REPO_ROOT/data/runtime/backups" "$TARGET"

dc() { docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" "$@"; }
env_value() { sed -n "s/^$1=//p" "$ENV_FILE" | tail -n 1; }

PG_USER="$(env_value POSTGRES_USER)"; PG_USER="${PG_USER:-admin}"
PG_DB="$(env_value POSTGRES_DB)"; PG_DB="${PG_DB:-red_sovereign}"

printf '▶ PostgreSQL (%s)\n' "$PG_DB"
dc exec -T db-postgres pg_dump -U "$PG_USER" -d "$PG_DB" --clean --if-exists \
  > "$TARGET/postgres-$PG_DB.sql"
dc exec -T db-postgres pg_dumpall -U "$PG_USER" --globals-only > "$TARGET/postgres-globals.sql"

printf '▶ MongoDB\n'
MONGO_USER="$(env_value MONGO_ROOT_USER)"; MONGO_USER="${MONGO_USER:-red_root}"
MONGO_PASS="$(env_value MONGO_ROOT_PASSWORD)"
dc exec -T db-mongo mongodump --username "$MONGO_USER" --password "$MONGO_PASS" \
  --authenticationDatabase admin --oplog --archive > "$TARGET/mongo.archive"

printf '▶ Redis (AOF/RDB snapshot for counters)\n'
REDIS_PASS="$(env_value REDIS_PASSWORD)"
dc exec -T cache-redis redis-cli -a "$REDIS_PASS" --no-auth-warning SAVE >/dev/null
dc exec -T cache-redis sh -c 'cat /data/dump.rdb' > "$TARGET/redis.rdb"

printf '▶ MinIO (media/backups/apks buckets)\n'
MINIO_USER="$(env_value MINIO_USER)"; MINIO_USER="${MINIO_USER:-redadmin}"
MINIO_PASS="$(env_value MINIO_PASSWORD)"
# Object storage is mirrored with the official mc image on the compose network;
# the buckets are private, so the alias is authenticated for this run only.
docker run --rm --network red-sovereign_red-net \
  -v "$TARGET/minio:/backup" \
  -e MC_HOST_local="http://$MINIO_USER:$MINIO_PASS@minio:9000" \
  minio/mc:latest sh -c '
    mc mirror --overwrite --quiet local/red-media /backup/red-media
    mc mirror --overwrite --quiet local/red-backups /backup/red-backups
    mc mirror --overwrite --quiet local/red-apks /backup/red-apks
    mc mirror --overwrite --quiet local/red-avatars /backup/red-avatars' \
  >/dev/null

printf '▶ Identity authority + runtime configuration (secrets are NOT copied into Git)\n'
cp "$REPO_ROOT/platform/secrets/red_identity_public_key.pem" "$TARGET/" 2>/dev/null || true
cp "$ENV_FILE" "$TARGET/red.env.backup"
chmod 600 "$TARGET/red.env.backup"

cat > "$TARGET/MANIFEST.txt" <<MANIFEST
RED backup $STAMP
postgres-$PG_DB.sql    pg_dump of $PG_DB
postgres-globals.sql   roles and tablespaces
mongo.archive          mongodump --oplog
redis.rdb              counters/rate-limit state
minio/                 object storage mirror (private buckets)
red.env.backup         runtime configuration (contains secrets: store encrypted)
restore with: tools/scripts/restore.sh data/runtime/backups/$STAMP
MANIFEST

printf '\nBackup ready: %s\n' "${TARGET#$REPO_ROOT/}"
printf 'A backup is not valid until a restore drill passes on a separate environment.\n'
