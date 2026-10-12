# dsh-java-mall

一个 PC 端虚拟商城：商品搜索、加入购物车、下单、支付、订单查询和物流跟踪都在浏览器内闭环。项目同时包含一个可安装到 `deepseek-harness-java` 的智能客服 Java Native 插件。

## 核心能力

- 商城服务：Spring Boot 提供商品、购物车、订单、支付、物流 REST API。
- 虚拟业务闭环：数据保存在内存中，预置用户 `customer-1` 与 12 件商品。
- 智能客服插件：注册商品搜索、商品详情、订单查询、订单详情和物流查询工具。
- Harness 集成：插件通过系统提示词说明工具边界，并通过 `POST_TOOL_USE` 发出客服审计事件。
- PC 界面：原生 HTML/CSS/JS，无前端构建依赖。

## 启动商城

商城服务本身无需数据库插件，可直接启动：

```bash
java -jar dsh-java-mall-app/target/dsh-java-mall-app-0.2.1-exec.jar
```

或开发模式：

```bash
mvn spring-boot:run -pl dsh-java-mall-app
```

打开 <http://127.0.0.1:8092>。默认演示用户是 `customer-1`。

> App 模块采用 exec 打包：可运行的是 `-exec.jar`（fat jar，仅在 `target/` 生成，不上传 Maven Central），
> `dsh-java-mall-app-0.2.1.jar` 为瘦 jar，仅供依赖使用。

> 重要：页面右上角的 **AI 客服** 不是商城独立能力。必须先在 `deepseek-harness-java` 中导入并启动 `dsh-java-mall-plugin` 插件，否则客服无法调用商品、订单和物流工具。完整接入流程见 Harness 仓库的 [`README.md`](../deepseek-harness-java/README.md)。

## 安装客服插件

先编译本项目，插件 JAR 在 `dsh-java-mall-plugin/target/dsh-java-mall-plugin-0.2.1.jar`：

```bash
mvn package -DskipTests
```

先启动 `deepseek-harness-java`（端口 8090），然后调用安装接口：

```bash
JAR="$PWD/dsh-java-mall-plugin/target/dsh-java-mall-plugin-0.2.1.jar"

curl -X POST http://localhost:8090/api/harness/plugins/install \
  -H 'Content-Type: application/json' \
  -d "{\"pluginId\":\"dsh-java-mall-plugin\",\"displayName\":\"DSH Mall Plugin\",\"pluginVersion\":\"0.2.1\",\"runtimeType\":\"JAVA_NATIVE\",\"sourcePath\":\"$JAR\",\"entrypoint\":\"dsh-java-mall-plugin-0.2.1.jar\"}"

curl -X POST http://localhost:8090/api/harness/plugins/activate \
  -H 'Content-Type: application/json' \
  -d '{"pluginId":"dsh-java-mall-plugin"}'
```

宿主加载后，Agent 会看到 `plugin__dsh-java-mall-plugin__search_products` 等工具。

在 Harness 插件配置中设置 `mall.service-token`，值需要与商城的
`mall.security.service-token` 一致。客服插件通过这个服务凭证查询当前登录用户的订单和物流。
同时建议设置 `mall.base-url` 为 `http://127.0.0.1:8092`。保存插件配置后，停用并重新启用插件，确保插件重新执行 `configure(context)`。

## 客服助手

商城页面右上角提供 AI 客服面板。`/api/assistant/stream` 会把用户消息代理到 Harness 的 `/api/agent/stream`，默认 Agent 是 `customer-service-demo`。可在 `dsh-java-mall-app/src/main/resources/application.yml` 调整：

```yaml
mall:
  assistant:
    harness-base-url: http://localhost:8090
    agent-id: customer-service-demo
```

插件工具默认调用 `http://localhost:8092`，可通过 Harness 插件配置 `mall.base-url` 覆盖。

## 项目结构

- `dsh-java-mall-app`：商城后端和 PC 前端。
- `dsh-java-mall-plugin`：DeepSeek Harness 插件。
- `docs/DESIGN.md`：业务、架构和插件能力设计。
