#!/usr/bin/env sh
# The GitHub App token used by this branch cannot modify .github/workflows, so the
# existing workflow still builds "./Dockerfile". Keep that path as a byte-identical
# mirror of the real definition and re-sync it after every change.
#
# Remove this shim and the root Dockerfile as soon as the workflow can be updated
# to: docker build . --file platform/containers/ci.Dockerfile
set -eu
SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
REPO_ROOT="$(CDPATH= cd -- "$SCRIPT_DIR/../.." && pwd)"
cp "$REPO_ROOT/platform/containers/ci.Dockerfile" "$REPO_ROOT/Dockerfile"
printf 'Synced root Dockerfile <- platform/containers/ci.Dockerfile\n'
