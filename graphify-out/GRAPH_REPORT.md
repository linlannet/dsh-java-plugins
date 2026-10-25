# dsh-java-plugins 工程知识图谱分析报告

> 由 graphify 自动生成 · 2026-10-15
> 扫描根：`/Users/mac/liubai/linlannet/dsh-java-plugins`

---

## 1. 总览

| 指标 | 数值 |
|------|------|
| 代码文件 | 113 |
| 节点总数 | 1,594 |
| 边总数 | 3,700 |
| 社区数 | 82 |
| 置信度 EXTRACTED | 3,587（96.9%） |
| 置信度 INFERRED | 113（3.1%） |

---

## 2. 模块分布

```
dsh-java-dameng     382 节点  ██████████████  (达梦数据库插件)
dsh-java-mysql      382 节点  ██████████████  (MySQL 插件)
dsh-java-oceanbase  382 节点  ██████████████  (OceanBase 插件)
dsh-java-mall       276 节点  ██████████      (商城示例插件)
```

**特征**：三个数据库插件结构完全对称（各 382 节点），mall 为独立示例。

---

## 3. 关系类型分布

| 关系 | 数量 | 说明 |
|------|:----:|------|
| calls | 1,136 | 方法调用 |
| method | 765 | 方法定义归属 |
| references | 599 | 引用 |
| imports | 558 | 导入 |
| contains | 549 | 包含 |
| indirect_call | 56 | 间接调用（推断） |
| inherits | 32 | 类继承 |
| defines | 4 | 定义 |
| implements | 1 | 接口实现 |

---

## 4. 核心概念节点（连接度 Top 15）

| 节点 | 连接度 | 角色 |
|------|:------:|------|
| app.js (dameng) | 72 | 达梦前端入口 |
| app.js (mysql) | 72 | MySQL 前端入口 |
| app.js (oceanbase) | 72 | OceanBase 前端入口 |
| app.js (mall) | 46 | 商城前端入口 |
| **Order** | **36** | **订单实体** |
| map | 36 | Map 类型 |
| **QueryService** | **32** ×3 | **查询服务（三个插件各一个）** |
| **MallService** | **31** | **商城服务** |
| **MallController** | **29** | **商城控制器** |
| Service 注解 | 28 | Spring Service |

---

## 5. 社区分析（Top 15）

| 社区 | 节点数 | 模块 | 核心概念 | 语义 |
|:----:|:------:|------|----------|------|
| 0 | 73 | dameng | **QueryService, ConnectionStore, DamengController** | **达梦查询+连接管理** |
| 1 | 73 | dameng | app.js, escapeHtml, renderConversations | 达梦前端 |
| 2 | 73 | mysql | app.js, escapeHtml, renderConversations | MySQL 前端 |
| 3 | 73 | oceanbase | app.js, escapeHtml, renderConversations | OceanBase 前端 |
| 4-6 | 70×3 | dameng/mysql/oceanbase | highlight.min.js | 代码高亮库（三份副本） |
| 7 | 47 | mall | app.js, bindEvents, askAssistant | **商城前端** |
| 8 | 46 | dameng+mysql | ApiExceptionHandler, HttpStatus | **异常处理** |
| 9 | 45 | dameng | **DshDamengPlugin, ExplainQueryTool, ListConnectionsTool** | **达梦插件工具** |
| 10 | 45 | oceanbase | **DshOceanBasePlugin, OceanBaseController** | **OceanBase 控制器** |
| 11 | 45 | mysql | **DshMysqlPlugin, ExplainQueryTool, ListConnectionsTool** | **MySQL 插件工具** |
| 12 | 45 | oceanbase | **DshOceanBasePlugin, ExplainQueryTool** | **OceanBase 插件工具** |
| 13 | 45 | mysql | **QueryService, MySqlController, Connection** | **MySQL 查询服务** |
| 14 | 32 | mysql | **ConnectionStore, ConnectionController** | **连接管理** |

---

## 6. 架构洞察

### 6.1 三数据库插件对称结构

```
dsh-java-dameng     dsh-java-mysql     dsh-java-oceanbase
├── Plugin          ├── Plugin          ├── Plugin
│   DshDamengPlugin │   DshMysqlPlugin  │   DshOceanBasePlugin
├── Controller      ├── Controller      ├── Controller
│   DamengController│   MySqlController │   OceanBaseController
├── QueryService    ├── QueryService    ├── QueryService
├── ConnectionStore ├── ConnectionStore ├── ConnectionStore
├── ExplainQueryTool├── ExplainQueryTool├── ExplainQueryTool
├── ListConnectionsTool              ├── ListConnectionsTool
└── app.js (前端)   └── app.js (前端)   └── app.js (前端)
```

每个数据库插件提供 2 个 Tool：
- **ExplainQueryTool** — 执行 SQL 并解释结果
- **ListConnectionsTool** — 列出数据库连接

### 6.2 Mall 商城示例

```
dsh-java-mall
├── MallController    — REST API
├── MallService       — 业务逻辑
├── Order             — 订单实体
└── app.js            — 前端（对话式助手）
```

### 6.3 关键发现

1. **三插件完全对称**：dameng/mysql/oceanbase 各 382 节点，结构一致，仅数据库驱动不同
2. **Tool 模式统一**：每个插件注册 ExplainQueryTool + ListConnectionsTool，通过 DshXxxPlugin 暴露给 Harness
3. **前端复用**：三个插件的前端 app.js 完全相同（escapeHtml/renderConversations/saveConversations）
4. **连接管理独立**：ConnectionStore + ConnectionController 管理数据库连接池
5. **异常处理共享**：ApiExceptionHandler 跨插件共享

---

## 7. 输出文件

| 文件 | 说明 |
|------|------|
| `graphify-out/graph.json` | 完整知识图谱 |
| `graphify-out/graph.html` | **交互式 HTML 可视化（节点级，1594 节点 < 5000）** |

**查看 HTML**：
```bash
open /Users/mac/liubai/linlannet/dsh-java-plugins/graphify-out/graph.html
```
