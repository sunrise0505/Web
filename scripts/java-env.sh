#!/usr/bin/env bash
# 供其他脚本 source，优先选择已安装的 JDK 21。
if [[ -z "${JAVA_HOME:-}" ]] || ! "$JAVA_HOME/bin/java" -version 2>&1 | head -n 1 | grep -Eq 'version "21\.|openjdk 21\.'; then
  if [[ -x /opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home/bin/java ]]; then
    export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
  elif [[ -x /usr/libexec/java_home ]]; then
    export JAVA_HOME="$(/usr/libexec/java_home -v 21)"
  fi
fi
if [[ -n "${JAVA_HOME:-}" ]]; then export PATH="$JAVA_HOME/bin:$PATH"; fi
if ! java -version 2>&1 | head -n 1 | grep -Eq 'version "21\.|openjdk 21\.'; then
  echo '本课程要求 JDK 21，请设置 JAVA_HOME 为 JDK 21 安装目录。' >&2
  return 1
fi
