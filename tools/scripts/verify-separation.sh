#!/usr/bin/env sh
# Thin wrapper used by CI and pre-commit hooks.
set -eu
SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
REPO_ROOT="$(CDPATH= cd -- "$SCRIPT_DIR/../.." && pwd)"
PY=python3
command -v "$PY" >/dev/null 2>&1 || PY=python
exec "$PY" "$REPO_ROOT/tools/guard/separation_guard.py" "$@"
