#!/usr/bin/env bash
# Poll GitHub Actions for the latest "Android Tests" workflow run (low rate to avoid API limits).
# Usage: REPO=owner/repo POLL_SEC=90 ./scripts/poll_android_tests_run.sh [max_polls]
set -euo pipefail
REPO="${REPO:-Oziomajnr/PgnToGif}"
POLL_SEC="${POLL_SEC:-90}"
MAX="${1:-40}"

wf_url="https://api.github.com/repos/${REPO}/actions/workflows/android-tests.yml/runs?per_page=1"
echo "Polling ${REPO} Android Tests every ${POLL_SEC}s (max ${MAX} polls)..."

for i in $(seq 1 "$MAX"); do
  json="$(curl -fsS "$wf_url")" || { echo "curl failed (rate limit?)"; sleep "$POLL_SEC"; continue; }
  id="$(echo "$json" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d['workflow_runs'][0]['id'])")"
  status="$(echo "$json" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d['workflow_runs'][0]['status'])")"
  conclusion="$(echo "$json" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d['workflow_runs'][0].get('conclusion') or '')")"
  sha="$(echo "$json" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d['workflow_runs'][0]['head_sha'][:7])")"
  echo "poll $i: run=$id sha=$sha status=$status conclusion=$conclusion"
  if [[ "$status" == "completed" ]]; then
    jobs_url="https://api.github.com/repos/${REPO}/actions/runs/${id}/jobs"
    curl -fsS "$jobs_url" | python3 -c "
import sys, json
d = json.load(sys.stdin)
for j in d.get('jobs', []):
    print(' ', j['name'], j['conclusion'])
"
    [[ "$conclusion" == "success" ]] && exit 0 || exit 1
  fi
  sleep "$POLL_SEC"
done
echo "Timeout after $MAX polls"
exit 2
