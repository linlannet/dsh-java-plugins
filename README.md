# dsh-java-plugins


[![License](https://img.shields.io/badge/license-MIT-green.svg)](./LICENSE)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](#技术栈)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.16-brightgreen.svg)](#技术栈)

基于 [dsh-java](https://github.com/linlannet/dsh-java) 的插件集合：MySQL 数据库插件、虚拟商城插件，定义插件的开发规范并给出参考实现。
---

## 版本历史

[CHANGELOG](./CHANGELOG.md)



## 功能特性

- **dsh-java-mysql**：可独立运行的 MySQL 轻量管理后台（端口 8091），附带 Harness 只读插件（5 个工具：连接列表、只读查询、EXPLAIN、性能快照、SQL 审计）
- **dsh-java-oceanbase**：可独立运行的 OceanBase 轻量管理后台（端口 8093），附带 Harness 只读插件（5 个工具，用法与 MySQL 插件一致）
- **dsh-java-dameng**：可独立运行的 DaMeng 轻量管理后台（端口 8094），附带 Harness 只读插件（5 个工具，用法与 MySQL 插件一致）
- **dsh-java-mall**：PC 端虚拟商城（端口 8092），商品搜索 → 购物车 → 下单 → 支付 → 物流闭环，附带 Harness 智能客服插件（11 个工具）
- **统一规范**：所有插件依赖 `net.linlan:dsh-java-types`（provided），通过 SPI 服务文件注册，包路径统一采用 `net.linlan.dsh.*`

## 架构

```text
dsh-java-plugins/
├── dsh-java-mysql/
│   ├── docs/                          文档与截图
│   ├── dsh-java-mysql-app/            Spring Boot 管理后台（Admin UI + Admin API，端口 8091）
│   └── dsh-java-mysql-plugin/         Harness 只读 MySQL 插件（5 个工具）
├── dsh-java-oceanbase/
│   ├── dsh-java-oceanbase-app/        Spring Boot 管理后台（Admin UI + Admin API，端口 8093）
│   └── dsh-java-oceanbase-plugin/     Harness 只读 OceanBase 插件（5 个工具）
├── dsh-java-dameng/
│   ├── dsh-java-dameng-app/           Spring Boot 管理后台（Admin UI + Admin API，端口 8094）
│   └── dsh-java-dameng-plugin/        Harness 只读 DaMeng 插件（5 个工具）
└── dsh-java-mall/
    ├── docs/                          文档
    ├── dsh-java-mall-app/             Spring Boot 商城服务（REST API + UI，端口 8092）
    └── dsh-java-mall-plugin/          Harness 智能客服插件（11 个工具）
```

```text
浏览器 ──► Admin UI ──► Admin API ──► 数据源
               │
               └─ AI 对话 ──► dsh-java (8090)
                                 └─► dsh-java-*-plugin ──► Admin API ──► 数据源
```

- **Admin UI**：原生 HTML/CSS/JS，无前端构建链
- **Plugin**：通过 Admin API 访问数据，不直接持有凭证
- **命名规范**：`{项目名}-app`（Spring Boot 应用）+ `{项目名}-plugin`（Harness 插件）

## 环境要求

- JDK 17+
- Maven 3.9+
- dsh-java 0.2.1（提供 `dsh-java-types` 契约包）
- 可选（被管理的目标实例）：MySQL 5.7+ / 8.x、OceanBase（MySQL 兼容模式）、DaMeng 8（JDBC 驱动已内置）

## 快速开始

### 1. 构建

```bash
git clone <dsh-java-plugins-repo-url>
cd dsh-java-plugins
mvn package -DskipTests
```

> **exec 打包说明**：与 `dsh-java/dsh-java-app` 保持一致，App 模块的 fat jar 附加 `exec` classifier 且 `attach=false`：
> - `xxx-app-<version>-exec.jar`：fat jar，可直接 `java -jar` 运行，只生成在 `target/`，不 install/deploy 到 Maven Central（避免占用 central.sonatype.com 空间限额）
> - `xxx-app-<version>.jar`：瘦 jar，作为库构件正常上传
>
> 也可以直接用 `scripts/start-mysql.sh`、`scripts/start-oceanbase.sh`、`scripts/start-dameng.sh`、`scripts/start-mall.sh` 启动（自动选择 `-exec.jar`）。

### 2. 启动数据库管理后台

三个数据库管理后台用法一致，按需要启动（端口各不相同）：

```bash
# MySQL（端口 8091）
java -jar dsh-java-mysql/dsh-java-mysql-app/target/dsh-java-mysql-app-0.2.1-exec.jar

# OceanBase（端口 8093）
java -jar dsh-java-oceanbase/dsh-java-oceanbase-app/target/dsh-java-oceanbase-app-0.2.1-exec.jar

# DaMeng（端口 8094）
java -jar dsh-java-dameng/dsh-java-dameng-app/target/dsh-java-dameng-app-0.2.1-exec.jar
```

| 工程 | 访问地址 |
| --- | --- |
| dsh-java-mysql | <http://127.0.0.1:8091> |
| dsh-java-oceanbase | <http://127.0.0.1:8093> |
| dsh-java-dameng | <http://127.0.0.1:8094> |

打开对应地址，添加数据库连接后即可使用 SQL 控制台和 AI 对话。

### 3. 启动虚拟商城

```bash
java -jar dsh-java-mall/dsh-java-mall-app/target/dsh-java-mall-app-0.2.1-exec.jar
```

打开 <http://127.0.0.1:8092>，默认演示用户 `customer-1`。

### 4. 安装 Harness 插件

先启动 `dsh-java`（端口 8090），然后按需安装插件：

```bash
# MySQL 插件
JAR="$PWD/dsh-java-mysql/dsh-java-mysql-plugin/target/dsh-java-mysql-plugin-0.2.1.jar"
curl -X POST http://localhost:8090/api/harness/plugins/install \
  -H 'Content-Type: application/json' \
  -d "{\"pluginId\":\"dsh-java-mysql-plugin\",\"displayName\":\"DSH MySQL Plugin\",\"pluginVersion\":\"0.2.1\",\"runtimeType\":\"JAVA_NATIVE\",\"sourcePath\":\"$JAR\",\"entrypoint\":\"dsh-java-mysql-plugin-0.2.1.jar\"}"
curl -X POST http://localhost:8090/api/harness/plugins/activate \
  -H 'Content-Type: application/json' -d '{"pluginId":"dsh-java-mysql-plugin"}'

# OceanBase 插件
JAR="$PWD/dsh-java-oceanbase/dsh-java-oceanbase-plugin/target/dsh-java-oceanbase-plugin-0.2.1.jar"
curl -X POST http://localhost:8090/api/harness/plugins/install \
  -H 'Content-Type: application/json' \
  -d "{\"pluginId\":\"dsh-java-oceanbase-plugin\",\"displayName\":\"DSH OceanBase Plugin\",\"pluginVersion\":\"0.2.1\",\"runtimeType\":\"JAVA_NATIVE\",\"sourcePath\":\"$JAR\",\"entrypoint\":\"dsh-java-oceanbase-plugin-0.2.1.jar\"}"
curl -X POST http://localhost:8090/api/harness/plugins/activate \
  -H 'Content-Type: application/json' -d '{"pluginId":"dsh-java-oceanbase-plugin"}'

# DaMeng 插件
JAR="$PWD/dsh-java-dameng/dsh-java-dameng-plugin/target/dsh-java-dameng-plugin-0.2.1.jar"
curl -X POST http://localhost:8090/api/harness/plugins/install \
  -H 'Content-Type: application/json' \
  -d "{\"pluginId\":\"dsh-java-dameng-plugin\",\"displayName\":\"DSH DaMeng Plugin\",\"pluginVersion\":\"0.2.1\",\"runtimeType\":\"JAVA_NATIVE\",\"sourcePath\":\"$JAR\",\"entrypoint\":\"dsh-java-dameng-plugin-0.2.1.jar\"}"
curl -X POST http://localhost:8090/api/harness/plugins/activate \
  -H 'Content-Type: application/json' -d '{"pluginId":"dsh-java-dameng-plugin"}'

# Mall 插件
JAR="$PWD/dsh-java-mall/dsh-java-mall-plugin/target/dsh-java-mall-plugin-0.2.1.jar"
curl -X POST http://localhost:8090/api/harness/plugins/install \
  -H 'Content-Type: application/json' \
  -d "{\"pluginId\":\"dsh-java-mall-plugin\",\"displayName\":\"DSH Mall Plugin\",\"pluginVersion\":\"0.2.1\",\"runtimeType\":\"JAVA_NATIVE\",\"sourcePath\":\"$JAR\",\"entrypoint\":\"dsh-java-mall-plugin-0.2.1.jar\"}"
curl -X POST http://localhost:8090/api/harness/plugins/activate \
  -H 'Content-Type: application/json' -d '{"pluginId":"dsh-java-mall-plugin"}'
```

## Agent 可用工具

### dsh-java-mysql-plugin（5 个只读工具）

| 工具 | 说明 |
| --- | --- |
| `plugin__dsh-java-mysql-plugin__mysql_list_connections` | 列出本地已配置连接 |
| `plugin__dsh-java-mysql-plugin__mysql_read_query` | 执行只读查询（SELECT/SHOW/DESC） |
| `plugin__dsh-java-mysql-plugin__mysql_explain_query` | 执行 EXPLAIN 执行计划 |
| `plugin__dsh-java-mysql-plugin__mysql_performance_snapshot` | 获取性能快照 |
| `plugin__dsh-java-mysql-plugin__mysql_sql_review` | 本地 SQL 风险审计（不连库） |

### dsh-java-oceanbase-plugin（5 个只读工具）

| 工具 | 说明 |
| --- | --- |
| `plugin__dsh-java-oceanbase-plugin__oceanbase_list_connections` | 列出本地已配置连接 |
| `plugin__dsh-java-oceanbase-plugin__oceanbase_read_query` | 执行只读查询（SELECT/SHOW/DESC） |
| `plugin__dsh-java-oceanbase-plugin__oceanbase_explain_query` | 执行 EXPLAIN 执行计划 |
| `plugin__dsh-java-oceanbase-plugin__oceanbase_performance_snapshot` | 获取性能快照 |
| `plugin__dsh-java-oceanbase-plugin__oceanbase_sql_review` | 本地 SQL 风险审计（不连库） |

### dsh-java-dameng-plugin（5 个只读工具）

| 工具 | 说明 |
| --- | --- |
| `plugin__dsh-java-dameng-plugin__dameng_list_connections` | 列出本地已配置连接 |
| `plugin__dsh-java-dameng-plugin__dameng_read_query` | 执行只读查询（SELECT/SHOW/DESC） |
| `plugin__dsh-java-dameng-plugin__dameng_explain_query` | 执行 EXPLAIN 执行计划 |
| `plugin__dsh-java-dameng-plugin__dameng_performance_snapshot` | 获取性能快照 |
| `plugin__dsh-java-dameng-plugin__dameng_sql_review` | 本地 SQL 风险审计（不连库） |

### dsh-java-mall-plugin（11 个工具）

| 工具 | 说明 |
| --- | --- |
| `plugin__dsh-java-mall-plugin__search_products` | 搜索商品 |
| `plugin__dsh-java-mall-plugin__get_product` | 查看商品详情 |
| `plugin__dsh-java-mall-plugin__add_to_cart` | 加入购物车 |
| `plugin__dsh-java-mall-plugin__place_order` | 下单 |
| `plugin__dsh-java-mall-plugin__pay_order` | 支付订单 |
| `plugin__dsh-java-mall-plugin__get_order` | 查看订单详情 |
| `plugin__dsh-java-mall-plugin__query_orders` | 查询订单列表 |
| `plugin__dsh-java-mall-plugin__cancel_order` | 取消订单 |
| `plugin__dsh-java-mall-plugin__query_logistics` | 查询物流 |

## 插件开发规范

1. **目录命名**：`{项目名}-app`（Spring Boot 应用）+ `{项目名}-plugin`（Harness 插件）
2. **包路径**：`net.linlan.dsh.{业务域}`，底层契约包 `net.linlan.dsh.domain.*`
3. **groupId**：`net.linlan.dsh`
4. **types 依赖**：`net.linlan:dsh-java-types:0.2.1`，scope=provided
5. **SPI 注册**：`META-INF/services/net.linlan.dsh.domain.spi.JavaHarnessPlugin` 文件内写入插件入口类全限定名
6. **plugin.yaml**：`META-INF/plugin.yaml` 声明插件 id、name、version、entrypoint
7. **工具实现**：继承 `AbstractTool`，实现 `name()`/`description()`/`parameters()`/`run()`，通过 `objectSchema()`/`stringSchema()` 构建参数 schema

## 配置项

### dsh-java-mysql

| 配置 | 默认值 | 说明 |
| --- | --- | --- |
| `server.port` | `8091` | 管理后台端口 |
| `dsh.mysql.data-directory` | `~/.dsh-java-mysql` | 连接配置存储目录 |
| `dsh.mysql.harness.base-url` | `http://127.0.0.1:8090` | Harness 地址 |

### dsh-java-oceanbase

| 配置 | 默认值 | 说明 |
| --- | --- | --- |
| `server.port` | `8093` | 管理后台端口 |
| `dsh.oceanbase.data-directory` | `~/.dsh-java-oceanbase` | 连接配置存储目录 |
| `dsh.oceanbase.query-timeout-seconds` | `15` | SQL 执行超时 |
| `dsh.oceanbase.max-rows` | `500` | 只读查询最大返回行数 |
| `dsh.oceanbase.harness.base-url` | `http://127.0.0.1:8090` | Harness 地址 |

### dsh-java-dameng

| 配置 | 默认值 | 说明 |
| --- | --- | --- |
| `server.port` | `8094` | 管理后台端口 |
| `dsh.dameng.data-directory` | `~/.dsh-java-dameng` | 连接配置存储目录 |
| `dsh.dameng.query-timeout-seconds` | `15` | SQL 执行超时 |
| `dsh.dameng.max-rows` | `500` | 只读查询最大返回行数 |
| `dsh.dameng.harness.base-url` | `http://127.0.0.1:8090` | Harness 地址 |

### dsh-java-mall

| 配置 | 默认值 | 说明 |
| --- | --- | --- |
| `server.port` | `8092` | 商城端口 |
| `mall.harness.base-url` | `http://127.0.0.1:8090` | Harness 地址 |

## 安全边界

- **数据库插件全程只读**：MySQL / OceanBase / DaMeng 插件均不提供写 SQL 执行能力，服务端二次校验，非只读语句且无 `allowWrite` 时直接拒绝
- **写操作需人工确认**：SQL 控制台执行写 SQL 必须勾选「写操作」开关，并再次人工确认；SQL 默认 15 秒超时、最多返回 500 行
- **Mall 插件**：通过 Admin API 操作，不直接持有数据库凭证
- 上述后台默认面向本机使用；生产部署前必须补充登录鉴权、CORS/CSRF 策略和集中审计
