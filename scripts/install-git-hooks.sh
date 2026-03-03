#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(git rev-parse --show-toplevel 2>/dev/null || pwd)"
SOURCE_HOOK="$ROOT_DIR/.githooks/pre-push"
TARGET_HOOK="$ROOT_DIR/.git/hooks/pre-push"

if [[ ! -f "$SOURCE_HOOK" ]]; then
  echo "Error: missing hook source at $SOURCE_HOOK"
  exit 1
fi

mkdir -p "$(dirname "$TARGET_HOOK")"
cp "$SOURCE_HOOK" "$TARGET_HOOK"
chmod +x "$TARGET_HOOK"

echo "Installed pre-push hook: $TARGET_HOOK"
