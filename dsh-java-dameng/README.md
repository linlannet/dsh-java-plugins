# dsh-java-dameng

一个可独立运行的 DaMeng 轻量管理后台（DaMeng Studio），并附带 `deepseek-harness-java` 的 Java Native Plugin，让 AI Agent 可以安全地以**只读**方式查询、分析你的 DaMeng 数据库。

## 功能特性

- **连接管理**：多连接配置，密码使用本机 AES-256-GCM 密钥加密存储（`~/.dsh-java-dameng/connections.json`）
- **AI 对话助手**：自然语言查询数据库，流式输出思考过程 → 工具调用 → 最终结论
- **SQL 控制台**：手动执行 SQL / EXPLAIN，写操作需显式勾选并二次确认
- **库表浏览**：表列表、表结构（列/索引）快速查看
- **性能监控**：连接数、慢查询、进程列表等实例快照
- **Harness 插件**：5 个只读工具注册到 deepseek-harness-java，供 Agent 调用

## 架构

```text
dsh-java-dameng/
├── dsh-java-dameng-app/      Spring Boot 管理后台（Admin UI + Admin API）
└── dsh-java-dameng-plugin/   Harness 只读 DaMeng 插件（JAVA_NATIVE）
```

```text
浏览器 ──► Admin UI (8094) ──► Admin API ──► DaMeng
              │
              └─ AI 对话 ──► deepseek-harness-java (8090)
                                └─► dsh-java-dameng-plugin ──► Admin API ──► DaMeng
```

- **Admin UI**：原生 HTML/CSS/JS，无前端构建链
- **Plugin**：通过 `http://127.0.0.1:8094` 访问 Admin API，不直接持有 DaMeng 密码

## 环境要求

- JDK 17+
- Maven 3.9+
- DaMeng 5.7+ / 8.x（被管理的目标实例）
- 可选：deepseek-harness-java（AI 对话能力依赖它提供 Agent 运行时）

## 快速开始

### 1. 构建

```bash
git clone git@github.com:linlannet/dsh-java-dameng.git
cd dsh-java-dameng
mvn package -DskipTests
```

### 2. 启动管理后台

```bash
java -jar dsh-java-dameng-app/target/dsh-java-dameng-app-0.2.1-exec.jar
```

或开发模式：

```bash
mvn spring-boot:run -pl dsh-java-dameng-app
```

打开 <http://127.0.0.1:8094>。

### 3. 添加数据库连接

点击右上角「＋ 连接」，填写主机、端口、数据库、用户名、密码，保存并测试。

### 4. 使用 AI 助手（可选）

AI 对话依赖 `deepseek-harness-java`（默认 `http://127.0.0.1:8090`）提供 Agent 运行时，并需要安装本仓库的插件（见下文）。选中连接后，在输入框用自然语言提问，例如：

- `列出所有表和行数`
- `当前有哪些慢查询隐患？`
- `帮我审计这条 SQL：SELECT * FROM user`

## 安装 Harness 插件

先启动 `deepseek-harness-java`（端口 8090），然后：

```bash
JAR="$PWD/dsh-java-dameng-plugin/target/dsh-java-dameng-plugin-0.2.1.jar"

curl -X POST http://localhost:8090/api/harness/plugins/install \
  -H 'Content-Type: application/json' \
  -d "{\"pluginId\":\"dsh-java-dameng-plugin\",\"displayName\":\"DSH DaMeng Plugin\",\"pluginVersion\":\"0.2.1\",\"runtimeType\":\"JAVA_NATIVE\",\"sourcePath\":\"$JAR\",\"entrypoint\":\"dsh-java-dameng-plugin-0.2.1.jar\"}"

curl -X POST http://localhost:8090/api/harness/plugins/activate \
  -H 'Content-Type: application/json' \
  -d '{"pluginId":"dsh-java-dameng-plugin"}'
```

Agent 可用工具：

| 工具 | 说明 |
| --- | --- |
| `plugin__dsh-java-dameng-plugin__dameng_list_connections` | 列出本地已配置连接 |
| `plugin__dsh-java-dameng-plugin__dameng_read_query` | 执行只读查询（SELECT/SHOW/DESC） |
| `plugin__dsh-java-dameng-plugin__dameng_explain_query` | 执行 EXPLAIN 执行计划 |
| `plugin__dsh-java-dameng-plugin__dameng_performance_snapshot` | 获取性能快照 |
| `plugin__dsh-java-dameng-plugin__dameng_sql_review` | 本地 SQL 风险审计（不连库） |

## 配置项

| 配置 | 环境变量 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `server.port` | — | `8094` | 管理后台端口 |
| `dsh.dameng.data-directory` | — | `~/.dsh-java-dameng` | 连接配置存储目录 |
| `dsh.dameng.query-timeout-seconds` | — | `15` | SQL 执行超时 |
| `dsh.dameng.max-rows` | — | `500` | 只读查询最大返回行数 |
| `dsh.dameng.harness.base-url` | `DSH_HARNESS_BASE_URL` | `http://127.0.0.1:8090` | Harness 地址 |

## 安全边界

- **AI 链路全程只读**：插件只注册只读、EXPLAIN、性能与规则审计工具，不提供写 SQL 执行能力；`QueryService` 服务端二次校验，非只读语句且无 `allowWrite` 时直接拒绝。
- **写操作需人工确认**：SQL 控制台执行写 SQL 必须勾选「写操作」开关，并再次人工确认。
- SQL 默认 15 秒超时、最多返回 500 行。
- 该后台默认面向本机使用；生产部署前必须补充登录鉴权、CORS/CSRF 策略和集中审计。
