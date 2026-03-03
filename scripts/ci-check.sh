#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(git rev-parse --show-toplevel 2>/dev/null || pwd)"
cd "$ROOT_DIR"

if [[ "${SKIP_CI_CHECK:-0}" == "1" ]]; then
  echo "SKIP_CI_CHECK=1 set. Skipping ciCheck."
  exit 0
fi

echo "Running ciCheck (assembleDebug + testDebugUnitTest)..."

if command -v cmd.exe >/dev/null 2>&1 && [[ -f "./gradlew.bat" ]] && [[ "${USE_WINDOWS_GRADLE:-1}" == "1" ]]; then
  cmd.exe /C "gradlew.bat ciCheck --no-daemon --stacktrace"
  exit 0
fi

if [[ ! -f "./gradlew" ]]; then
  echo "Error: ./gradlew not found."
  exit 1
fi

if LC_ALL=C grep -q $'\r' "./gradlew"; then
  echo "Detected CRLF line endings in gradlew. Using normalized temporary wrapper."
  TMP_WRAPPER="./.gradlew.unix"
  trap 'rm -f "$TMP_WRAPPER"' EXIT
  tr -d '\r' < "./gradlew" > "$TMP_WRAPPER"
  chmod +x "$TMP_WRAPPER"
  "$TMP_WRAPPER" ciCheck --no-daemon --stacktrace
else
  ./gradlew ciCheck --no-daemon --stacktrace
fi
