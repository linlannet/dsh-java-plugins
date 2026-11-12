# AGENTS.md — dsh-java-plugins

## Project
Java native plugins for dsh-java: MySQL, OceanBase, Dameng and Mall. Version 0.2.2.

## Layout
- `dsh-java-<name>/dsh-java-<name>-plugin` thin plugin JAR (`META-INF/plugin.yaml` + entry class)
- `dsh-java-<name>/dsh-java-<name>-app` runnable Spring Boot app (ports 8091–8094)
- `scripts/` build helpers

## Build & Test
- Build: `mvn clean package -DskipTests`
- Test: `mvn test`
- Offline: append `-o`

## Conventions
- Plugin entry extends the dsh-java plugin contract; keep `plugin.yaml` in sync with code
- Plugin JAR must stay thin (no Spring Boot repackage); app modules are the runnable ones
- Artifact names follow `dsh-java-<name>-<module>-<version>.jar`

## Do Not
- Do not embed the Harness runtime in plugin JARs
- Do not change plugin IDs or entry class names without a migration note
- Do not edit `target/`

## Key Docs
- `README.md`, `README.en.md`
- `../dsh-java/docs/md/java-plugin-development.md`
- `llms.txt`