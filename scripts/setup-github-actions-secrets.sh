#!/usr/bin/env bash
# Uploads local files to GitHub Actions repository secrets via `gh`.
#
# Prerequisites: GitHub CLI (`gh`) installed and `gh auth login` completed for the repo.
#
# Why some secrets are base64 and some are not:
#   - RELEASE_KEYSTORE_BASE64: the .jks is binary; CI writes it from a single-line secret.
#   - GOOGLE_SERVICES_JSON: matches existing workflows (echo … | base64 --decode > app/google-services.json).
#   - PLAY_SERVICE_ACCOUNT_JSON: store as PLAIN JSON text. The Play upload action uses
#     serviceAccountJsonPlainText (raw JSON). GitHub encrypts secrets at rest; base64 would
#     only force the workflow to decode an extra step.
#
# Usage (no args uses defaults below — adjust PLAY_SA_JSON or pass paths as needed):
#   ./scripts/setup-github-actions-secrets.sh
#   ./scripts/setup-github-actions-secrets.sh PATH_TO_PLAY_SERVICE_ACCOUNT.json
#   ./scripts/setup-github-actions-secrets.sh PLAY_SA.json [google-services.json] [upload-keystore.jks]
#
# Environment (optional — skips prompts when set):
#   PLAY_SA_JSON                 Play / GCP service account JSON path (overrides default file)
#   GOOGLE_SERVICES_JSON_FILE    Override path for Firebase google-services.json
#   RELEASE_KEYSTORE_FILE        Override path for upload keystore .jks
#   RELEASE_KEYSTORE_PASSWORD    Keystore password
#   RELEASE_KEY_PASSWORD         Key password (defaults to same as keystore if empty)
#   RELEASE_KEY_ALIAS            Key alias (default: upload)
#
# This script does not print secret values. Do not commit keystores, JSON keys, or passwords.

set -euo pipefail

usage() {
  sed -n '1,35p' "$0" | tail -n +2
  exit 0
}

die() {
  echo "error: $*" >&2
  exit 1
}

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_root"

if ! command -v gh >/dev/null 2>&1; then
  die "gh (GitHub CLI) not found. Install from https://cli.github.com/"
fi

if ! gh auth status >/dev/null 2>&1; then
  die "run: gh auth login"
fi

if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
  usage
fi

# Default Play/Firebase admin key path (override with arg 1 or PLAY_SA_JSON).
DEFAULT_PLAY_SA="${HOME}/Downloads/pgntogif-firebase-adminsdk-967eh-241fcba6fa.json"

PLAY_JSON="${1:-${PLAY_SA_JSON:-$DEFAULT_PLAY_SA}}"
GOOGLE_JSON="${2:-${GOOGLE_SERVICES_JSON_FILE:-$repo_root/app/google-services.json}}"
KEYSTORE="${3:-${RELEASE_KEYSTORE_FILE:-$repo_root/upload-keystore.jks}}"

[[ -f "$PLAY_JSON" ]] || die "Play service account JSON not found: $PLAY_JSON
  Pass path as first arg, or set PLAY_SA_JSON, or place the file at:
  $DEFAULT_PLAY_SA"
[[ -f "$KEYSTORE" ]] || die "Keystore not found: $KEYSTORE (pass as 3rd arg or set RELEASE_KEYSTORE_FILE)"

# Single-line base64 (works on macOS and Linux)
b64_file() {
  local f="$1"
  if base64 --help 2>&1 | grep -q -- '-w'; then
    base64 -w0 <"$f"
  else
    base64 <"$f" | tr -d '\n'
  fi
}

ALIAS="${RELEASE_KEY_ALIAS:-upload}"

if [[ -n "${RELEASE_KEYSTORE_PASSWORD:-}" ]]; then
  STORE_PW="$RELEASE_KEYSTORE_PASSWORD"
else
  read -r -s -p "Keystore password: " STORE_PW
  echo "" >&2
  [[ -n "$STORE_PW" ]] || die "empty keystore password"
fi

if [[ -n "${RELEASE_KEY_PASSWORD:-}" ]]; then
  KEY_PW="$RELEASE_KEY_PASSWORD"
else
  read -r -s -p "Key password [Enter if same as keystore]: " KEY_PW
  echo "" >&2
  KEY_PW="${KEY_PW:-$STORE_PW}"
fi

echo "Setting RELEASE_KEYSTORE_BASE64 (from $(basename "$KEYSTORE"))..." >&2
b64_file "$KEYSTORE" | gh secret set RELEASE_KEYSTORE_BASE64

echo "Setting RELEASE_KEYSTORE_PASSWORD..." >&2
gh secret set RELEASE_KEYSTORE_PASSWORD --body "$STORE_PW"

echo "Setting RELEASE_KEY_ALIAS..." >&2
gh secret set RELEASE_KEY_ALIAS --body "$ALIAS"

echo "Setting RELEASE_KEY_PASSWORD..." >&2
gh secret set RELEASE_KEY_PASSWORD --body "$KEY_PW"

echo "Setting PLAY_SERVICE_ACCOUNT_JSON as plain JSON (from $(basename "$PLAY_JSON"))..." >&2
gh secret set PLAY_SERVICE_ACCOUNT_JSON <"$PLAY_JSON"

if [[ -f "$GOOGLE_JSON" ]]; then
  echo "Setting GOOGLE_SERVICES_JSON as base64 (from $(basename "$GOOGLE_JSON"))..." >&2
  b64_file "$GOOGLE_JSON" | gh secret set GOOGLE_SERVICES_JSON
else
  echo "skip: GOOGLE_SERVICES_JSON — file not found: $GOOGLE_JSON" >&2
  echo "      (CI treats this as optional; pass path as 2nd arg or set GOOGLE_SERVICES_JSON_FILE)" >&2
fi

echo "Done. Verify under GitHub → Settings → Secrets and variables → Actions." >&2
