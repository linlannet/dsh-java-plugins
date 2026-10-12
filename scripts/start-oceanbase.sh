#!/usr/bin/env bash
#
# 启动 dsh-java-oceanbase-app（端口 8093）。
#
# 说明：App 模块采用 exec 打包（见 dsh-java-oceanbase-app/pom.xml）：
#   - dsh-java-oceanbase-app-<version>-exec.jar：fat jar，可直接 java -jar 运行，只在 target/ 生成，不上传 Maven Central
#   - dsh-java-oceanbase-app-<version>.jar：瘦 jar，仅作库构件，不可直接运行
# 因此这里优先选择 -exec.jar。
#
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
APP_DIR="${ROOT_DIR}/dsh-java-oceanbase/dsh-java-oceanbase-app"
APP_NAME="dsh-java-oceanbase-app"

resolve_jar() {
  if [[ -n "${JAR_PATH:-}" ]]; then
    echo "${JAR_PATH}"
    return
  fi
  [[ -d "${APP_DIR}/target" ]] || return 0
  find "${APP_DIR}/target" -maxdepth 1 -type f -name "${APP_NAME}-*-exec.jar" | sort | tail -1
}

APP_JAR="$(resolve_jar)"
if [[ -z "${APP_JAR}" ]]; then
  APP_JAR="$(find "${APP_DIR}/target" -maxdepth 1 -type f -name "${APP_NAME}-*.jar" \
    ! -name '*-exec.jar' ! -name '*sources.jar' ! -name '*javadoc.jar' 2>/dev/null | sort | tail -1)"
fi

if [[ -z "${APP_JAR}" || ! -f "${APP_JAR}" ]]; then
  echo "未找到 ${APP_NAME} 的可运行 JAR，请先构建：" >&2
  echo "  mvn -pl dsh-java-oceanbase/${APP_NAME} -am package -DskipTests" >&2
  exit 1
fi

JAVA_BIN="${JAVA_HOME:+${JAVA_HOME}/bin/java}"
JAVA_BIN="${JAVA_BIN:-java}"

echo "启动 ${APP_NAME}：${APP_JAR}"
echo "控制台：http://127.0.0.1:8093"

exec "${JAVA_BIN}" ${DSH_JAVA_OPTS:-} -jar "${APP_JAR}" "$@"
