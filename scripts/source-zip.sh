#!/usr/bin/env bash
set -euo pipefail
PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$PROJECT_DIR"
mkdir -p artifacts
rm -f artifacts/campus-venue-source.zip
zip -qr artifacts/campus-venue-source.zip frontend backend database docs scripts README.md .gitignore .env.example \
  -x '*/node_modules/*' '*/dist/*' '*/target/*' '*/data/*' '*/resources/public/*' \
  '*/.env' '*/.env.local' '*/.env.*.local' '*.log' '*.DS_Store' '*.jar' '*.class'
echo "已生成：$PROJECT_DIR/artifacts/campus-venue-source.zip"
