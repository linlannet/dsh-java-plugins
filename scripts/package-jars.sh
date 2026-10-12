#!/usr/bin/env bash
#
# 构建四个 App 模块，并把可直接运行的 exec fat jar 收集到 dist/ 目录。
#
# 背景：App 模块的 fat jar 只在 target/ 生成（classifier=exec、attach=false），
# 不 install/deploy 到 Maven Central，避免占用 central.sonatype.com 的空间限额。
# 因此发布产物需要从 target/ 显式收集。
#
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DIST_DIR="${ROOT_DIR}/dist"
VERSION="$(sed -n '/<artifactId>dsh-java-plugins<\/artifactId>/{n;s/.*<version>\(.*\)<\/version>.*/\1/p;}' "${ROOT_DIR}/pom.xml")"
if [[ -z "${VERSION}" ]]; then
  echo "无法从根 pom.xml 解析版本号" >&2
  exit 1
fi

# 格式：模块目录|应用 artifactId
APPS=(
  "dsh-java-mysql|dsh-java-mysql-app"
  "dsh-java-oceanbase|dsh-java-oceanbase-app"
  "dsh-java-dameng|dsh-java-dameng-app"
  "dsh-java-mall|dsh-java-mall-app"
)

(cd "${ROOT_DIR}" && mvn clean package -DskipTests)

rm -rf "${DIST_DIR}"
mkdir -p "${DIST_DIR}"

for item in "${APPS[@]}"; do
  MODULE="${item%%|*}"
  APP="${item##*|}"
  JAR="$(find "${ROOT_DIR}/${MODULE}/${APP}/target" -maxdepth 1 -type f -name "${APP}-*-exec.jar" | sort | tail -1)"
  if [[ -z "${JAR}" ]]; then
    echo "未找到 exec fat jar：${MODULE}/${APP}/target/${APP}-<version>-exec.jar" >&2
    exit 1
  fi
  cp "${JAR}" "${DIST_DIR}/${APP}-${VERSION}.jar"
  echo "已收集：${DIST_DIR}/${APP}-${VERSION}.jar"
done

echo "输出目录：${DIST_DIR}"
