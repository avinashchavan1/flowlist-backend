#!/usr/bin/env bash
# ──────────────────────────────────────────────────────────
# FlowList Backend — start script
# Loads variables from .env then runs the Spring Boot JAR.
# ──────────────────────────────────────────────────────────
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="$SCRIPT_DIR/.env"

# Load .env if it exists
if [ -f "$ENV_FILE" ]; then
  echo "[start.sh] Loading environment from $ENV_FILE"
  set -o allexport
  # shellcheck disable=SC1090
  source "$ENV_FILE"
  set +o allexport
else
  echo "[start.sh] Warning: .env not found — using application.properties defaults"
fi

# Build first if no JAR exists yet
JAR=$(find "$SCRIPT_DIR/build/libs" -maxdepth 1 -name "*.jar" ! -name "*plain*" 2>/dev/null | head -1)

if [ -z "$JAR" ]; then
  echo "[start.sh] No JAR found — running Gradle build first..."
  cd "$SCRIPT_DIR"
  ./gradlew bootJar
  JAR=$(find "$SCRIPT_DIR/build/libs" -maxdepth 1 -name "*.jar" ! -name "*plain*" | head -1)
fi

echo "[start.sh] Starting: $JAR"
java -jar "$JAR"
