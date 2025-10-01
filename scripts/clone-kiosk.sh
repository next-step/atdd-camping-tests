#!/usr/bin/env bash
set -euo pipefail
ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
REPO_DIR="$ROOT_DIR/repos/atdd-camping-kiosk"

if [ ! -d "$REPO_DIR/.git" ]; then
  git clone --branch main --single-branch --depth 1 \
    https://github.com/next-step/atdd-camping-kiosk "$REPO_DIR"
else
  cd "$REPO_DIR"
  git fetch origin main
  git switch main
  git pull --rebase
fi
echo "[OK] kiosk repo synced."
