# 版本

## 版本号：0.2.1
- 更新日期：2026-10-11

1. 内部版本号与 dsh-java 0.2.1 对齐（由 0.2.0 升级）
2. **App 模块改为 exec 打包**（与 `dsh-java/dsh-java-app` 对齐）：
   - fat jar 附加 `exec` classifier（`xxx-app-<version>-exec.jar`），`attach=false`，只生成在 `target/`，不 install/deploy 到 Maven Central
   - 主 artifact 保留为瘦 jar（约 70~125KB），可被下游依赖
   - 解决 fat jar（约 22~24MB）上传 central.sonatype.com 超出空间限额的问题
3. 新增 `scripts/` 启动与打包脚本：`start-mysql.sh`、`start-oceanbase.sh`、`start-dameng.sh`、`start-mall.sh`、`package-jars.sh`（自动选择 `-exec.jar`）
4. 四个 `ActionDockerfile` 改为拷贝 `*-exec.jar`

## 版本号：0.2.0
- 更新日期：2026-10-11

1. 内部版本号与 dsh-java 0.2.0 对齐（由 0.1.9 升级）
2. spring-boot 版本升级 3.3.2 → 3.5.16
3. mybatis-spring-boot 版本升级 3.0.4 → 3.0.5
4. scm 与项目 url 统一为 linlannet 仓库子路径
5. 对 dsh-java-types 契约包依赖升级至 0.2.0

- **dsh-java-mysql**：
  - dsh-java-mysql-app：版本对齐 0.2.0，spring-boot 3.5.16
  - dsh-java-mysql-plugin：版本对齐 0.2.0，依赖 dsh-java-types 0.2.0
- **dsh-java-mall**：
  - dsh-java-mall-app：版本对齐 0.2.0，spring-boot 3.5.16
  - dsh-java-mall-plugin：版本对齐 0.2.0，依赖 dsh-java-types 0.2.0
- **dsh-java-oceanbase**：
  - dsh-java-oceanbase-app：版本对齐 0.2.0，spring-boot 3.5.16
  - dsh-java-oceanbase-plugin：版本对齐 0.2.0，依赖 dsh-java-types 0.2.0
- **dsh-java-dameng**：
  - dsh-java-dameng-app：版本对齐 0.2.0，spring-boot 3.5.16
  - dsh-java-dameng-plugin：版本对齐 0.2.0，依赖 dsh-java-types 0.2.0

## 版本号：0.1.0

- 更新日期：2026-10-08
- 初始化版本
