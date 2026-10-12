# dsh-java-plugins

[![License](https://img.shields.io/badge/license-MIT-green.svg)](./LICENSE)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](#21-requirements)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.16-brightgreen.svg)](#21-requirements)

---

A plugin collection for [dsh-java](https://github.com/linlannet/dsh-java): MySQL / OceanBase / DaMeng database plugins, virtual mall plugin, defining the plugin development convention with reference implementations.

## Version History

[CHANGELOG](./CHANGELOG.md)


## Features

- **dsh-java-mysql**: Standalone lightweight MySQL admin console (port 8091) with a Harness read-only plugin (5 tools: connection list, read query, EXPLAIN, performance snapshot, SQL review)
- **dsh-java-oceanbase**: Standalone lightweight OceanBase admin console (port 8093) with a Harness read-only plugin (5 tools, same usage as the MySQL plugin)
- **dsh-java-dameng**: Standalone lightweight DaMeng admin console (port 8094) with a Harness read-only plugin (5 tools, same usage as the MySQL plugin)
- **dsh-java-mall**: PC virtual mall (port 8092) — product search → cart → checkout → payment → logistics, with a Harness smart customer service plugin (11 tools)
- **Unified convention**: All plugins depend on `net.linlan:dsh-java-types` (provided), registered via SPI service files, package path unified as `net.linlan.dsh.*`

## Architecture

```text
dsh-java-plugins/
├── dsh-java-mysql/
│   ├── docs/                          Docs and screenshots
│   ├── dsh-java-mysql-app/           Spring Boot admin console (Admin UI + Admin API, port 8091)
│   └── dsh-java-mysql-plugin/        Harness read-only MySQL plugin (5 tools)
├── dsh-java-oceanbase/
│   ├── dsh-java-oceanbase-app/       Spring Boot admin console (Admin UI + Admin API, port 8093)
│   └── dsh-java-oceanbase-plugin/    Harness read-only OceanBase plugin (5 tools)
├── dsh-java-dameng/
│   ├── dsh-java-dameng-app/          Spring Boot admin console (Admin UI + Admin API, port 8094)
│   └── dsh-java-dameng-plugin/       Harness read-only DaMeng plugin (5 tools)
└── dsh-java-mall/
    ├── docs/                          Docs
    ├── dsh-java-mall-app/            Spring Boot mall service (REST API + UI, port 8092)
    └── dsh-java-mall-plugin/         Harness smart customer service plugin (11 tools)
```

```text
Browser ──► Admin UI ──► Admin API ──► Data Source
               │
               └─ AI Chat ──► dsh-java (8090)
                                 └─► dsh-java-*-plugin ──► Admin API ──► Data Source
```

- **Admin UI**: Vanilla HTML/CSS/JS, no frontend build pipeline
- **Plugin**: Accesses data via Admin API, does not hold credentials directly
- **Naming convention**: `{project-name}-app` (Spring Boot app) + `{project-name}-plugin` (Harness plugin)

## Requirements

- JDK 17+
- Maven 3.9+
- dsh-java 0.2.1 (provides `dsh-java-types` contract package)
- Optional (target instances to manage): MySQL 5.7+ / 8.x, OceanBase (MySQL-compatible mode), DaMeng 8 (JDBC driver bundled)

## Quick Start

### 1. Build

```bash
git clone <dsh-java-plugins-repo-url>
cd dsh-java-plugins
mvn package -DskipTests
```

> **exec packaging**: Consistent with `dsh-java/dsh-java-app`, the app modules attach the fat jar with the `exec` classifier and `attach=false`:
> - `xxx-app-<version>-exec.jar`: fat jar, runnable with `java -jar`, generated only in `target/`, never installed/deployed to Maven Central (avoids the central.sonatype.com space quota)
> - `xxx-app-<version>.jar`: thin jar, uploaded as a normal library artifact
>
> You can also start the apps with `scripts/start-mysql.sh`, `scripts/start-oceanbase.sh`, `scripts/start-dameng.sh`, `scripts/start-mall.sh` (they pick `-exec.jar` automatically).

### 2. Start Database Admin Consoles

The three database admin consoles work the same way; start the ones you need (each uses its own port):

```bash
# MySQL (port 8091)
java -jar dsh-java-mysql/dsh-java-mysql-app/target/dsh-java-mysql-app-0.2.1-exec.jar

# OceanBase (port 8093)
java -jar dsh-java-oceanbase/dsh-java-oceanbase-app/target/dsh-java-oceanbase-app-0.2.1-exec.jar

# DaMeng (port 8094)
java -jar dsh-java-dameng/dsh-java-dameng-app/target/dsh-java-dameng-app-0.2.1-exec.jar
```

| Module | URL |
| --- | --- |
| dsh-java-mysql | <http://127.0.0.1:8091> |
| dsh-java-oceanbase | <http://127.0.0.1:8093> |
| dsh-java-dameng | <http://127.0.0.1:8094> |

Open the corresponding URL, add a database connection to use the SQL console and AI chat.

### 3. Start Virtual Mall

```bash
java -jar dsh-java-mall/dsh-java-mall-app/target/dsh-java-mall-app-0.2.1-exec.jar
```

Open <http://127.0.0.1:8092>, default demo user is `customer-1`.

### 4. Install Harness Plugins

Start `dsh-java` (port 8090) first, then install the plugins you need:

```bash
# MySQL plugin
JAR="$PWD/dsh-java-mysql/dsh-java-mysql-plugin/target/dsh-java-mysql-plugin-0.2.1.jar"
curl -X POST http://localhost:8090/api/harness/plugins/install \
  -H 'Content-Type: application/json' \
  -d "{\"pluginId\":\"dsh-java-mysql-plugin\",\"displayName\":\"DSH MySQL Plugin\",\"pluginVersion\":\"0.2.1\",\"runtimeType\":\"JAVA_NATIVE\",\"sourcePath\":\"$JAR\",\"entrypoint\":\"dsh-java-mysql-plugin-0.2.1.jar\"}"
curl -X POST http://localhost:8090/api/harness/plugins/activate \
  -H 'Content-Type: application/json' -d '{"pluginId":"dsh-java-mysql-plugin"}'

# OceanBase plugin
JAR="$PWD/dsh-java-oceanbase/dsh-java-oceanbase-plugin/target/dsh-java-oceanbase-plugin-0.2.1.jar"
curl -X POST http://localhost:8090/api/harness/plugins/install \
  -H 'Content-Type: application/json' \
  -d "{\"pluginId\":\"dsh-java-oceanbase-plugin\",\"displayName\":\"DSH OceanBase Plugin\",\"pluginVersion\":\"0.2.1\",\"runtimeType\":\"JAVA_NATIVE\",\"sourcePath\":\"$JAR\",\"entrypoint\":\"dsh-java-oceanbase-plugin-0.2.1.jar\"}"
curl -X POST http://localhost:8090/api/harness/plugins/activate \
  -H 'Content-Type: application/json' -d '{"pluginId":"dsh-java-oceanbase-plugin"}'

# DaMeng plugin
JAR="$PWD/dsh-java-dameng/dsh-java-dameng-plugin/target/dsh-java-dameng-plugin-0.2.1.jar"
curl -X POST http://localhost:8090/api/harness/plugins/install \
  -H 'Content-Type: application/json' \
  -d "{\"pluginId\":\"dsh-java-dameng-plugin\",\"displayName\":\"DSH DaMeng Plugin\",\"pluginVersion\":\"0.2.1\",\"runtimeType\":\"JAVA_NATIVE\",\"sourcePath\":\"$JAR\",\"entrypoint\":\"dsh-java-dameng-plugin-0.2.1.jar\"}"
curl -X POST http://localhost:8090/api/harness/plugins/activate \
  -H 'Content-Type: application/json' -d '{"pluginId":"dsh-java-dameng-plugin"}'

# Mall plugin
JAR="$PWD/dsh-java-mall/dsh-java-mall-plugin/target/dsh-java-mall-plugin-0.2.1.jar"
curl -X POST http://localhost:8090/api/harness/plugins/install \
  -H 'Content-Type: application/json' \
  -d "{\"pluginId\":\"dsh-java-mall-plugin\",\"displayName\":\"DSH Mall Plugin\",\"pluginVersion\":\"0.2.1\",\"runtimeType\":\"JAVA_NATIVE\",\"sourcePath\":\"$JAR\",\"entrypoint\":\"dsh-java-mall-plugin-0.2.1.jar\"}"
curl -X POST http://localhost:8090/api/harness/plugins/activate \
  -H 'Content-Type: application/json' -d '{"pluginId":"dsh-java-mall-plugin"}'
```

## Available Agent Tools

### dsh-java-mysql-plugin (5 read-only tools)

| Tool | Description |
| --- | --- |
| `plugin__dsh-java-mysql-plugin__mysql_list_connections` | List configured connections |
| `plugin__dsh-java-mysql-plugin__mysql_read_query` | Execute read-only query (SELECT/SHOW/DESC) |
| `plugin__dsh-java-mysql-plugin__mysql_explain_query` | Execute EXPLAIN plan |
| `plugin__dsh-java-mysql-plugin__mysql_performance_snapshot` | Get performance snapshot |
| `plugin__dsh-java-mysql-plugin__mysql_sql_review` | Local SQL risk audit (no DB connection) |

### dsh-java-oceanbase-plugin (5 read-only tools)

| Tool | Description |
| --- | --- |
| `plugin__dsh-java-oceanbase-plugin__oceanbase_list_connections` | List configured connections |
| `plugin__dsh-java-oceanbase-plugin__oceanbase_read_query` | Execute read-only query (SELECT/SHOW/DESC) |
| `plugin__dsh-java-oceanbase-plugin__oceanbase_explain_query` | Execute EXPLAIN plan |
| `plugin__dsh-java-oceanbase-plugin__oceanbase_performance_snapshot` | Get performance snapshot |
| `plugin__dsh-java-oceanbase-plugin__oceanbase_sql_review` | Local SQL risk audit (no DB connection) |

### dsh-java-dameng-plugin (5 read-only tools)

| Tool | Description |
| --- | --- |
| `plugin__dsh-java-dameng-plugin__dameng_list_connections` | List configured connections |
| `plugin__dsh-java-dameng-plugin__dameng_read_query` | Execute read-only query (SELECT/SHOW/DESC) |
| `plugin__dsh-java-dameng-plugin__dameng_explain_query` | Execute EXPLAIN plan |
| `plugin__dsh-java-dameng-plugin__dameng_performance_snapshot` | Get performance snapshot |
| `plugin__dsh-java-dameng-plugin__dameng_sql_review` | Local SQL risk audit (no DB connection) |

### dsh-java-mall-plugin (11 tools)

| Tool | Description |
| --- | --- |
| `plugin__dsh-java-mall-plugin__search_products` | Search products |
| `plugin__dsh-java-mall-plugin__get_product` | View product details |
| `plugin__dsh-java-mall-plugin__add_to_cart` | Add to cart |
| `plugin__dsh-java-mall-plugin__place_order` | Place order |
| `plugin__dsh-java-mall-plugin__pay_order` | Pay order |
| `plugin__dsh-java-mall-plugin__get_order` | View order details |
| `plugin__dsh-java-mall-plugin__query_orders` | Query order list |
| `plugin__dsh-java-mall-plugin__cancel_order` | Cancel order |
| `plugin__dsh-java-mall-plugin__query_logistics` | Query logistics |

## Plugin Development Convention

1. **Directory naming**: `{project-name}-app` (Spring Boot app) + `{project-name}-plugin` (Harness plugin)
2. **Package path**: `net.linlan.dsh.{domain}`, contract package `net.linlan.dsh.domain.*`
3. **groupId**: `net.linlan.dsh`
4. **types dependency**: `net.linlan:dsh-java-types:0.2.1`, scope=provided
5. **SPI registration**: `META-INF/services/net.linlan.dsh.domain.spi.JavaHarnessPlugin` containing the plugin entry class FQN
6. **plugin.yaml**: `META-INF/plugin.yaml` declaring plugin id, name, version, entrypoint
7. **Tool implementation**: Extend `AbstractTool`, implement `name()`/`description()`/`parameters()`/`run()`, use `objectSchema()`/`stringSchema()` for parameter schema

## Configuration

### dsh-java-mysql

| Property | Default | Description |
| --- | --- | --- |
| `server.port` | `8091` | Admin console port |
| `dsh.mysql.data-directory` | `~/.dsh-java-mysql` | Connection config storage directory |
| `dsh.mysql.harness.base-url` | `http://127.0.0.1:8090` | Harness URL |

### dsh-java-oceanbase

| Property | Default | Description |
| --- | --- | --- |
| `server.port` | `8093` | Admin console port |
| `dsh.oceanbase.data-directory` | `~/.dsh-java-oceanbase` | Connection config storage directory |
| `dsh.oceanbase.query-timeout-seconds` | `15` | SQL execution timeout |
| `dsh.oceanbase.max-rows` | `500` | Max rows returned by a read-only query |
| `dsh.oceanbase.harness.base-url` | `http://127.0.0.1:8090` | Harness URL |

### dsh-java-dameng

| Property | Default | Description |
| --- | --- | --- |
| `server.port` | `8094` | Admin console port |
| `dsh.dameng.data-directory` | `~/.dsh-java-dameng` | Connection config storage directory |
| `dsh.dameng.query-timeout-seconds` | `15` | SQL execution timeout |
| `dsh.dameng.max-rows` | `500` | Max rows returned by a read-only query |
| `dsh.dameng.harness.base-url` | `http://127.0.0.1:8090` | Harness URL |

### dsh-java-mall

| Property | Default | Description |
| --- | --- | --- |
| `server.port` | `8092` | Mall port |
| `mall.harness.base-url` | `http://127.0.0.1:8090` | Harness URL |

## Security Boundary

- **Database plugins are fully read-only**: MySQL / OceanBase / DaMeng plugins provide no write SQL execution; server-side validation rejects non-read-only statements without `allowWrite`
- **Write operations require manual confirmation**: Executing write SQL in the SQL console requires enabling the "write" toggle and confirming again; SQL defaults to a 15-second timeout and at most 500 rows
- **Mall plugin**: Operates via Admin API, does not hold database credentials directly
- These consoles are intended for local use by default; production deployment must add authentication, CORS/CSRF policies, and centralized auditing
