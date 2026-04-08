#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Load .env if present and RAILWAY_API_TOKEN is not already set
if [[ -z "${RAILWAY_API_TOKEN:-}" && -f "$SCRIPT_DIR/.env" ]]; then
  export $(grep -v '^#' "$SCRIPT_DIR/.env" | grep 'RAILWAY_API_TOKEN' | xargs) 2>/dev/null || true
fi

if [[ -z "${RAILWAY_API_TOKEN:-}" ]]; then
  echo "❌  RAILWAY_API_TOKEN is not set."
  echo "    Add it to drift-backend/.env or export it before running:"
  echo "    export RAILWAY_API_TOKEN=<your-token>"
  exit 1
fi

echo "▶  Building backend JAR..."
./gradlew bootJar --no-daemon -q

echo "▶  Deploying backend to Railway..."
railway up --service flowlist-api --detach

echo "⏳  Waiting for Railway to start the new deployment..."
sleep 20

echo "▶  Checking deployment health..."
for i in 1 2 3 4 5; do
  STATUS=$(curl -s -o /dev/null -w "%{http_code}" https://flowlist-api-production.up.railway.app/api/health)
  if [[ "$STATUS" == "200" ]]; then
    echo "✅  Backend deployed and healthy → https://flowlist-api-production.up.railway.app"
    exit 0
  fi
  echo "   Attempt $i/5 — got HTTP $STATUS, retrying in 15s..."
  sleep 15
done

echo "⚠️  Backend deployed but health check did not pass in time."
echo "   Check logs: railway logs --service flowlist-api"
exit 1
