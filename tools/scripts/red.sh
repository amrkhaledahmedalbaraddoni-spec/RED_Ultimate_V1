#!/usr/bin/env sh
# RED single entry point. Every runtime action goes through this script so the
# compose file, the env file and the health checks can never drift apart.
set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
REPO_ROOT="$(CDPATH= cd -- "$SCRIPT_DIR/../.." && pwd)"
COMPOSE_FILE="$REPO_ROOT/platform/compose/docker-compose.yml"
ENV_FILE="$REPO_ROOT/platform/config/.env"
DEMO_COMPOSE="$COMPOSE_FILE"
HTTP_PORT_DEFAULT=80

fail() { printf 'ERROR: %s\n' "$*" >&2; exit 1; }
info() { printf '%s\n' "$*"; }
need() { command -v "$1" >/dev/null 2>&1 || fail "$1 is required"; }

python_bin() {
  command -v python3 >/dev/null 2>&1 && { echo python3; return; }
  command -v python >/dev/null 2>&1 && { echo python; return; }
  fail "python3 is required for env rendering and the separation guard"
}

ensure_env() {
  server_ip="${1:-}"
  if [ ! -f "$ENV_FILE" ]; then
    py="$(python_bin)"
    if [ -n "$server_ip" ]; then
      "$py" "$REPO_ROOT/tools/env/render_env.py" --server-ip "$server_ip"
    else
      "$py" "$REPO_ROOT/tools/env/render_env.py"
    fi
  fi
  [ -f "$ENV_FILE" ] || fail "missing $ENV_FILE"
  chmod 600 "$ENV_FILE" 2>/dev/null || true
}

ensure_identity() {
  [ -f "$REPO_ROOT/platform/secrets/red_identity_private_key.pem" ] && return 0
  "$REPO_ROOT/tools/scripts/generate-local-identity-authority.sh"
}

dc() {
  ensure_env
  docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" "$@"
}

http_port() {
  port="$(sed -n 's/^RED_HTTP_PORT=//p' "$ENV_FILE" 2>/dev/null | tail -n 1)"
  [ -n "$port" ] || port="$HTTP_PORT_DEFAULT"
  printf '%s' "$port"
}

wait_http() {
  label="$1"; url="$2"; attempts="${3:-60}"
  printf 'Waiting for %s' "$label"
  i=0
  while [ "$i" -lt "$attempts" ]; do
    if curl -fsS "$url" >/dev/null 2>&1; then printf ' PASS\n'; return 0; fi
    i=$((i + 1)); printf '.'; sleep 3
  done
  printf '\n%s did not become ready: %s\n' "$label" "$url" >&2
  dc ps >&2 || true
  dc logs --tail=120 backend >&2 || true
  return 1
}

command_guard() { py="$(python_bin)"; "$py" "$REPO_ROOT/tools/guard/separation_guard.py" "$@"; }

command_env() {
  py="$(python_bin)"
  server_ip="${1:-}"
  if [ -n "$server_ip" ]; then
    "$py" "$REPO_ROOT/tools/env/render_env.py" --server-ip "$server_ip" "$@" || true
  else
    "$py" "$REPO_ROOT/tools/env/render_env.py" "$@"
  fi
  info "Environment contract rendered at platform/config/.env (mode 600)."
  info "Regenerate the committed template with: python3 tools/env/render_env.py --example"
}

command_up() {
  server_ip="${1:-}"
  need docker
  docker info >/dev/null 2>&1 || fail "Docker daemon is not running"
  ensure_env "$server_ip"
  ensure_identity
  docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" config --quiet
  info "Compose configuration: PASS"
  docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" build
  docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" up -d
  # Nginx resolves upstream names at config load; refresh after replacements.
  docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" restart nginx
  port="$(http_port)"
  wait_http "backend health" "http://127.0.0.1:$port/health"
  wait_http "SFU health" "http://127.0.0.1:$port/sfu-health"

  if [ "${BUILD_ANDROID:-0}" = "1" ]; then
    [ -n "$server_ip" ] || fail "BUILD_ANDROID=1 requires the server IPv4: red.sh up 192.168.1.50"
    info "Building verified Android artifact image (downloads the Android SDK image on first run)..."
    mkdir -p "$REPO_ROOT/local-artifacts"
    docker build --file "$REPO_ROOT/platform/containers/ci.Dockerfile" --target android-artifact \
      --build-arg "RED_SERVER_URL=http://$server_ip:$port" \
      --output "type=local,dest=$REPO_ROOT/local-artifacts" "$REPO_ROOT"
    [ -f "$REPO_ROOT/local-artifacts/red-app-debug.apk" ] || fail "Android build finished without an APK"
    info "APK: $REPO_ROOT/local-artifacts/red-app-debug.apk"
  fi

  info ""
  info "RED is up."
  info "  Admin:      http://${server_ip:-127.0.0.1}:$port/"
  info "  Health:     http://${server_ip:-127.0.0.1}:$port/health"
  info "  SFU health: http://${server_ip:-127.0.0.1}:$port/sfu-health"
  info "  Credentials live only in platform/config/.env (never committed)."
}

usage() {
  cat <<'USAGE'
RED control script

  red.sh env [SERVER_IP]     render platform/config/.env from the contract
  red.sh up [SERVER_IP]      build + start + wait for real health (BUILD_ANDROID=1 builds APK)
  red.sh down                stop and remove containers (volumes are kept)
  red.sh ps | logs [SVC]     inspect the stack
  red.sh config              docker compose config --quiet
  red.sh build               rebuild images only
  red.sh guard [--json]      run the separation guard
  red.sh backup              dump PostgreSQL/MongoDB/Redis/MinIO into data/runtime/backups
  red.sh restore DIR         restore from a backup directory (requires RED_RESTORE_CONFIRM=yes)
  red.sh key                 generate the local identity authority keys (once)
USAGE
}

[ $# -ge 1 ] || { usage; exit 1; }
command="$1"; shift || true

case "$command" in
  env) command_env "$@" ;;
  up) command_up "$@" ;;
  down) dc down ;;
  ps) dc ps ;;
  logs) dc logs --tail=200 "$@" ;;
  config) ensure_env; docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" config --quiet; info "Compose configuration: PASS" ;;
  build) dc build ;;
  guard) command_guard "$@" ;;
  backup) "$REPO_ROOT/tools/scripts/backup.sh" "$@" ;;
  restore) "$REPO_ROOT/tools/scripts/restore.sh" "$@" ;;
  key) ensure_identity ;;
  -h|--help|help) usage ;;
  *) usage; fail "unknown command: $command" ;;
esac
