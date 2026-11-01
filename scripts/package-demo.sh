#!/usr/bin/env bash
set -euo pipefail
PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$PROJECT_DIR"
if [[ "${1:-}" != "--skip-build" ]]; then bash scripts/package.sh; fi
python3 - <<'PY'
from pathlib import Path
import shutil,zipfile
root=Path.cwd()
jar=root/'backend/target/java-server-1.0-SNAPSHOT.jar'
if not jar.exists(): raise SystemExit('请先运行 scripts/package.sh')
out=root/'artifacts/sdu-venue-demo'
out.mkdir(parents=True,exist_ok=True)
shutil.copyfile(jar,out/jar.name)
shutil.copyfile(root/'docs/老师运行说明.md',out/'运行说明.md')
shell='''#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
if [[ "$(uname)" == "Darwin" && -x /usr/libexec/java_home ]]; then
  DEMO_JAVA_HOME="$(/usr/libexec/java_home -v 21 2>/dev/null || true)"
  if [[ -n "$DEMO_JAVA_HOME" ]]; then export JAVA_HOME="$DEMO_JAVA_HOME"; export PATH="$JAVA_HOME/bin:$PATH"; fi
fi
if ! command -v java >/dev/null 2>&1; then echo 'Please install Java 21 first.'; exit 1; fi
echo 'Open http://127.0.0.1:22222 after startup. Keep this window open.'
exec java -jar java-server-1.0-SNAPSHOT.jar --spring.profiles.active=demo --server.address=127.0.0.1 "$@"
'''
for n in ['start-mac.command','start-linux.sh']:
 p=out/n;p.write_text(shell);p.chmod(0o755)
(out/'start-windows.bat').write_bytes(b'@echo off\r\ncd /d "%~dp0"\r\nwhere java >nul 2>nul\r\nif errorlevel 1 (\r\n  echo Please install Java 21 first.\r\n  pause\r\n  exit /b 1\r\n)\r\necho Open http://127.0.0.1:22222 after startup. Keep this window open.\r\njava -jar java-server-1.0-SNAPSHOT.jar --spring.profiles.active=demo --server.address=127.0.0.1 %*\r\npause\r\n')
# 明确列出交付文件，排除曾运行生成的数据。
names=[jar.name,'运行说明.md','start-mac.command','start-linux.sh','start-windows.bat']
with zipfile.ZipFile(root/'artifacts/sdu-venue-demo.zip','w',zipfile.ZIP_DEFLATED) as z:
 for n in names:z.write(out/n,'sdu-venue-demo/'+n)
print('已生成 artifacts/sdu-venue-demo.zip（不含个人数据与学校密码）')
PY
