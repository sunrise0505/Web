#!/usr/bin/env bash
set -euo pipefail
PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
source "$PROJECT_DIR/scripts/java-env.sh"
cd "$PROJECT_DIR"
npm --prefix frontend run build
node scripts/copy-frontend.mjs
cd backend
mvn -Pcourse clean verify
echo "已生成：$PROJECT_DIR/backend/target/java-server-1.0-SNAPSHOT.jar"
