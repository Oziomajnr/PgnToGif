#!/usr/bin/env bash
# Wait for the latest "Android Tests" run to finish using `gh run watch` (CLI-backed updates,
# default refresh every few seconds). No hand-written REST polling loop.
#
# Usage:
#   ./scripts/watch_android_tests_run.sh [run_id]
#
# If run_id is omitted, resolves the latest run for workflow android-tests.yml on BRANCH
# (default: current git branch), retrying briefly in case the run has not been created yet.
#
# Environment:
#   BRANCH   Branch to filter (default: git rev-parse --abbrev-ref HEAD)
#   REPO     Optional gh -R owner/repo
#
# Requires: gh auth login, git (for default branch).
set -euo pipefail

GH_REPO=()
if [[ -n "${REPO:-}" ]]; then
  GH_REPO=(-R "$REPO")
fi

RUN_ID="${1:-}"
BRANCH="${BRANCH:-$(git rev-parse --abbrev-ref HEAD)}"

resolve_run_id() {
  gh run list "${GH_REPO[@]}" \
    --workflow=android-tests.yml \
    --branch="$BRANCH" \
    --limit=1 \
    --json databaseId \
    -q '.[0].databaseId // empty'
}

if [[ -z "$RUN_ID" ]]; then
  echo "Resolving latest Android Tests run on branch: $BRANCH"
  for _ in $(seq 1 60); do
    RUN_ID="$(resolve_run_id)"
    if [[ -n "$RUN_ID" ]]; then
      break
    fi
    sleep 2
  done
fi

if [[ -z "$RUN_ID" ]]; then
  echo "error: no workflow run found for android-tests.yml on branch $BRANCH" >&2
  exit 3
fi

echo "Watching run $RUN_ID (exit non-zero if failed)..."
exec gh run watch "${GH_REPO[@]}" "$RUN_ID" --exit-status
