#!/usr/bin/env sh
# Restore a RED backup. Destructive: refuses to run without RED_RESTORE_CONFIRM=yes.
set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
REPO_ROOT="$(CDPATH= cd -- "$SCRIPT_DIR/../.." && pwd)"
ENV_FILE="$REPO_ROOT/platform/config/.env"
COMPOSE_FILE="$REPO_ROOT/platform/compose/docker-compose.yml"

fail() { printf 'ERROR: %s\n' "$*" >&2; exit 1; }
[ $# -eq 1 ] || fail "usage: restore.sh data/runtime/backups/<timestamp>"
SOURCE="$1"
case "$SOURCE" in /*) ;; *) SOURCE="$REPO_ROOT/$SOURCE" ;; esac
[ -d "$SOURCE" ] || fail "backup directory not found: $SOURCE"
[ "${RED_RESTORE_CONFIRM:-}" = "yes" ] || fail "set RED_RESTORE_CONFIRM=yes to confirm an overwrite restore"
[ -f "$ENV_FILE" ] || fail "missing platform/config/.env"

dc() { docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" "$@"; }
env_value() { sed -n "s/^$1=//p" "$ENV_FILE" | tail -n 1; }
PG_USER="$(env_value POSTGRES_USER)"; PG_USER="${PG_USER:-admin}"
PG_DB="$(env_value POSTGRES_DB)"; PG_DB="${PG_DB:-red_sovereign}"
MONGO_USER="$(env_value MONGO_ROOT_USER)"; MONGO_USER="${MONGO_USER:-red_root}"
MONGO_PASS="$(env_value MONGO_ROOT_PASSWORD)"

printf 'Restoring PostgreSQL into %s ...\n' "$PG_DB"
dc exec -T db-postgres psql -U "$PG_USER" -d "$PG_DB" -v ON_ERROR_STOP=1 < "$SOURCE/postgres-$PG_DB.sql"

printf 'Restoring MongoDB ...\n'
dc exec -T db-mongo mongorestore --username "$MONGO_USER" --password "$MONGO_PASS" \
  --authenticationDatabase admin --oplogReplay --drop --archive < "$SOURCE/mongo.archive"

if [ -d "$SOURCE/minio" ]; then
  printf 'Restoring MinIO objects ...\n'
  MINIO_USER="$(env_value MINIO_USER)"; MINIO_USER="${MINIO_USER:-redadmin}"
  MINIO_PASS="$(env_value MINIO_PASSWORD)"
  docker run --rm --network red-sovereign_red-net -v "$SOURCE/minio:/backup:ro" \
    -e MC_HOST_local="http://$MINIO_USER:$MINIO_PASS@minio:9000" \
    minio/mc:latest sh -c '
      mc mirror --overwrite --quiet /backup/red-media local/red-media
      mc mirror --overwrite --quiet /backup/red-media local/red-avatars 2>/dev/null || true' \
    >/dev/null
fi

printf '\nRestore finished. Now verify (do not skip):\n'
printf '  1. log in to the admin dashboard and list approved accounts\n'
printf '  2. send a message between two devices and confirm ACK\n'
printf '  3. open a story and a media attachment\n'
printf '  4. confirm device certificates still verify\n'
