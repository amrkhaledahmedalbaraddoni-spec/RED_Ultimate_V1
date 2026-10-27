#!/usr/bin/env sh
# First local run on Linux/macOS. Kept as a thin, verifiable wrapper so that
# documentation, CI syntax checks and operators all exercise one code path.
set -eu
SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
exec "$SCRIPT_DIR/red.sh" up "$@"
