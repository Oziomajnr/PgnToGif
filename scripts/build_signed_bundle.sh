#!/usr/bin/env bash
# Build a signed release AAB using upload-keystore.jks at the repo root.
#
# Provide credentials one of:
#   - Repo-root .signing.env (gitignored); copy from .signing.env.example
#   - Or export RELEASE_KEY_PASSWORD and/or RELEASE_KEYSTORE_PASSWORD before running
#
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

export RELEASE_KEYSTORE_FILE="${RELEASE_KEYSTORE_FILE:-$ROOT/upload-keystore.jks}"
export RELEASE_KEY_ALIAS="${RELEASE_KEY_ALIAS:-upload}"

if [[ -f "$ROOT/.signing.env" ]]; then
  # shellcheck disable=SC1091
  set -a
  # shellcheck source=/dev/null
  source "$ROOT/.signing.env"
  set +a
fi

if [[ -z "${RELEASE_KEYSTORE_PASSWORD:-}" && -z "${RELEASE_KEY_PASSWORD:-}" ]]; then
  echo "error: no keystore password. Export RELEASE_KEY_PASSWORD or create .signing.env (see .signing.env.example)." >&2
  exit 1
fi

if [[ ! -f "$RELEASE_KEYSTORE_FILE" ]]; then
  echo "error: keystore not found: $RELEASE_KEYSTORE_FILE" >&2
  exit 1
fi

./gradlew :app:bundleRelease --no-daemon

AAB="$ROOT/app/build/outputs/bundle/release/app-release.aab"
VERIFY_OUT=$(jarsigner -verify -certs "$AAB" 2>&1) || true
if echo "$VERIFY_OUT" | grep -qi "unsigned"; then
  echo "error: AAB is unsigned (check password / alias)." >&2
  echo "$VERIFY_OUT" >&2
  exit 1
fi
echo "Signed AAB: $AAB"
