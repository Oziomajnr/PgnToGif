#!/usr/bin/env bash
# Re-download Lichess (lila) MP3s from https://github.com/lichess-org/lila (AGPL-3.0)
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
DEST="$ROOT/app/src/main/assets/lichess_sounds"
mkdir -p "$DEST"
BASE="https://raw.githubusercontent.com/lichess-org/lila/master/public/sound/lisp"
for f in Move Capture Check Castles Victory; do
  curl -sL "$BASE/${f}.mp3" -o "$DEST/${f}.mp3"
  echo "Downloaded $f.mp3 ($(wc -c < "$DEST/${f}.mp3") bytes)"
done
