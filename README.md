# Ontology Platform — 本体建模平台

> 此项目也被称为**项目2**。

企业级本体服务治理平台，基于 DDD 分层架构，为上游本体建模工具（**项目1**：[`D:\AI\Ontology`](../Ontology)）产出的本体模型提供持久化、查询、校验、发布、Agent 编排（ACP 协议接入 Kimi/Claude/Codex）、V12-V14 领域模型（19+2 张新表）、106 条校验规则（7 插件）、CI 自动构建测试（1m26s）、跨项目 E2E 导入/导出测试（6 个场景）、MCP 语义意图解析（4-phase）、组织上下文权限链（Position→Role→Permission），以及 Phase 2 异步任务、Webhook、幂等与限流能力。MCP Server 将 REST API 暴露为 AI Agent 可调用的 MCP 工具。

**仓库**: [Frank-zhao-junjun/ontology-platform](https://github.com/Frank-zhao-junjun/ontology-platform)

## 双项目分工

| 项目 | 路径 | 技术栈 | 职责 |
|------|------|--------|------|
| **项目1** Ontology | [`../Ontology`](../Ontology) | Next.js 16 · React 19 · Vitest | A→B→C→EPC 建模、E1–E8 要素库、Manifest 编译导出 |
| **项目2** Ontology Platform | 本仓库 | Spring Boot 3.2 · PostgreSQL · MCP | 持久化、发布、治理、Agent 编排、V12–V14 域 |

- 项目1 `pnpm run ci:check` 全绿（2026-06-26）：~**1049** tests · lint 0 error
- 项目2 `mvn test`：**220+** tests · 0 failures · CI ~59s
- MCP Server `npm test`：**177** tests · 10 files · 0 failures
- 对接文档：[`docs/shared/项目1-项目2对接差距分析.md`](docs/shared/项目1-项目2对接差距分析.md) · [`docs/project1-to-project2-mapping.md`](docs/project1-to-project2-mapping.md)
- 项目1 共享文档跳转：[`../Ontology/docs/shared/README.md`](../Ontology/docs/shared/README.md)
- Bug 排查与测试闭环：[`TC-TODO.md`](TC-TODO.md) — 26/27 完成（2026-07-02）

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 3.2.5 |
| Java | JDK | 21（编译目标；运行建议 21+） |
| 数据库 | PostgreSQL + Apache AGE | 15+ / 1.5.x |
| 缓存 / 队列 | Redis | 7.x |
| ORM | MyBatis-Plus | 3.5.6 |
| 数据库迁移 | Flyway | 10.11.0 |
| API 文档 | SpringDoc OpenAPI | 2.5.0 |
| MCP 适配层 | Node.js + TypeScript | 22 |
| 构建 | Maven | 3.8+ |

## 项目结构

```
ontology-platform/
├── ontology-api/              # Controller、DTO、Filter、Flyway 迁移
├── ontology-application/      # Service 实现、Manifest 校验
├── ontology-domain/           # 实体、值对象、Repository 接口
├── ontology-infrastructure/   # Repository 实现、JobQueue、Webhook、AGE
├── ontology-common/           # 工具类、异常、常量
├── mcp-server/                # MCP 协议适配（:3001）
├── frontend/                  # Vite + Tailwind（PPT 等辅助前端）
├── docs/
│   ├── shared/                # PRD、TDD、API 契约、Manifest 规范
│   ├── superpowers/specs/     # Phase 1 / Phase 2 / Phase 3 实施 Spec
│   └── import/                # 导入模块分析与规划
├── .github/workflows/         # CI 自动编译+测试（GitHub Actions）
├── docker/                    # Docker Compose（PG + Redis + App + MCP）
├── scripts/                   # 启动脚本、证书生成等
├── .coze                      # Coze CLI 配置文件
└── README.md
```

## 快速开始

### 前置条件

- JDK 21+
- Maven 3.8+
- **Docker Desktop**（本地 dev 需 PostgreSQL；Redis 可选，dev profile 使用 no-op 模式）
- Node.js 22+（仅 MCP Server 开发）

### 方式一：Coze CLI

项目根目录已配置 `.coze` 文件。启动前需先拉起 PostgreSQL（Phase 2 功能还需 Redis）：

```bash
cd docker
cp .env.example .env    # 首次
docker compose up -d postgres redis
docker compose ps
cd ..
coze dev
```

```bash
# 打包部署
coze build
coze start
```

服务就绪后：

- Spring Boot: `http://localhost:8081/api`（dev profile 默认 8081）
- Swagger UI: `http://localhost:8081/api/swagger-ui.html`
- 健康检查: `http://localhost:8081/api/actuator/health`

> **注意**: `dev` profile 使用 **PostgreSQL + Flyway + MyBatis-Plus**（与生产栈一致）。Flyway 脚本含 PG 专有语法，**不能用 H2 替代**。dev profile 使用无 Redis 模式（`DevRedisConfig`），Redis 非必需。

### 方式二：Docker Compose（完整环境）

```bash
cd docker
cp .env.example .env
docker compose up -d
docker compose ps
```

服务就绪后：

- Spring Boot: `http://localhost:8080/api`
- Swagger UI: `http://localhost:8080/api/swagger-ui.html`
- MCP Server: `http://localhost:3001/health`
- 健康检查: `http://localhost:8080/api/actuator/health`

### 方式三：本地 Maven 启动

先启动基础设施（同上 `docker compose up -d postgres redis`），再：

```bash
# 编译
mvn clean compile

# 单元测试（不依赖 Docker）
mvn test

# 开发模式（bash / git-bash）
SPRING_PROFILES_ACTIVE=dev mvn -pl ontology-api spring-boot:run -DskipTests

# 验证
curl http://localhost:8081/api/actuator/health
```

### 开发环境配置

| 配置文件 | 说明 |
|----------|------|
| `application.yml` | 默认配置（PostgreSQL、Flyway、MyBatis-Plus） |
| `application-dev.yml` | dev 覆盖：调试日志、`jpa.ddl-auto=none`、Flyway 开启 |
| `application-prod.yml` | 生产覆盖 |

`dev` / `prod` 均依赖 PostgreSQL；schema 由 **Flyway** 迁移，不由 Hibernate 建表。Flyway 脚本使用 PG 专有语法（`uuid-ossp`、JSONB 等），**不支持 H2**。

| 环境变量 | 默认值 | 说明 |
|----------|--------|------|
| `DB_HOST` / `DB_PORT` | `localhost` / `5432` | PostgreSQL |
| `DB_USERNAME` / `DB_PASSWORD` | `ontology` / `ontology123` | 与 `docker/.env.example` 一致 |
| `REDIS_HOST` / `REDIS_PORT` | `localhost` / `6379` | Phase 2 Job / 限流 / Webhook |
| `REDIS_PASSWORD` | 空（本地需设 `redis123`） | Docker Redis 默认带密码 |

### MCP Server

```bash
cd mcp-server
npm ci
npm run dev    # 开发
npm test       # Vitest
```

环境变量见 [`mcp-server/README.md`](mcp-server/README.md)。

## API 约定

- **基址**: `http://localhost:{port}/api`（`server.servlet.context-path=/api`；dev profile 默认 8081，Docker Compose 默认 8080）
- **公开路径**: 以 `/api/v1/...` 开头（与 [API 契约 v2.0](docs/shared/API契约-本体建模平台-v2.0.yaml) 一致）
- **统一响应**: `{ code, message, data, meta: { trace_id, ... } }`
- **幂等（可选）**: 写请求可带 `Idempotency-Key` Header（Phase 2）
- **租户**: `X-Tenant-Id` Header，默认 `default`

完整 OpenAPI 定义：[docs/shared/API契约-本体建模平台-v2.0.yaml](docs/shared/API契约-本体建模平台-v2.0.yaml)

## 主要 API 模块

| 模块 | 端点前缀 | 说明 |
|------|----------|------|
| 本体 | `/api/v1/ontologies` | CRUD、发布、归档、校验 |
| 对象类型 | `/api/v1/ontologies/{id}/object-types` | 类型与属性 |
| 关系 | `/api/v1/ontologies/{id}/relations` | 关系 CRUD |
| 图遍历 | `/api/v1/ontologies/{id}/graph/*` | traverse / paths / subgraph |
| Agent 编排 | `/api/v1/agents/*` | Agent 任务提交/查询（Kimi/Claude/Codex） |
| Manifest | `/api/v1/manifests/*` | 导入、预览、发布、导出 |
| **项目1导入** | `/api/v1/ontologies/import` | 项目1 JSON → v2 pipeline + autoPublish |
| **Exchange v2** | `/api/v2/exchanges/*` | import(JSON/Excel/Markdown) + validate(no-persist) + publish |
| 行为/事件/EPC | `/api/v1/ontologies/{id}/actions\|events\|epc` | 领域定义查询 |
| **EPC 覆盖** | `/api/v1/ontologies/{id}/epc/coverage` | 覆盖率报告（含未覆盖 actions/events） |
| **语义层** | `/api/v1/ontologies/{id}/semantic-layer` + `/api/v2/semantic/resolve-intent` | 语义层查询 + 意图解析（4-phase） |
| **生命周期** | `/api/v1/ontologies/{id}/lifecycle/{entityId}` | 实体生命周期快照查询 |
| V12 组织/指标 | `/api/v1/ontologies/{id}/departments\|positions\|business-metrics\|orchestrations\|process-steps` | 部门、岗位、指标、编排、流程 |
| V12 元数据/术语/意图 | `/api/v1/ontologies/{id}/metadata-templates\|business-terms\|agent-intents` | 元数据模板、业务术语、Agent 意图 |
| V13 语义 | `/api/v1/ontologies/{id}/semantic-relations\|intent-slots\|agent-policies-semantic\|error-recoveries\|semantic-field-mappings\|entity-lifecycle-snapshots` | 语义关系、槽位、策略、容错、字段映射、生命周期 |
| V14 EPC | `/api/v1/ontologies/{id}/epc-chains\|epc-nodes\|epc-edges\|epc-model-refs\|epc-profiles` | EPC 链/节点/边/模型引用/配置 |
| **治理角色** | `/api/v1/ontologies/{id}/governance-roles` | 角色 + 权限 CRUD（Position→Role→Permission 链） |
| 治理 | `/api/v1/governance/*` | Token、角色、权限、审批 |
| 上传/导入 | `/api/v1/uploads/*`, `/api/v1/imports/*` | 分片上传与导入任务 |
| 异步任务 | `/api/v1/jobs` | 提交/查询/取消 Job（Phase 2） |
| Webhook | `/api/v1/webhooks` | 订阅 job 完成/失败回调（Phase 2） |
| 可观测 | `/api/actuator/prometheus` | Prometheus 指标 |
| 健康检查 | `/api/v1/health/details` | 构建信息、JVM 版本、测试统计、运行时长 |

### Agent 编排

平台内置 Agent 编排网关，通过 ACP 协议接入多款 AI Agent CLI：

| Agent | 协议 | 用途 |
|:-----:|:----:|:----:|
| Kimi | ACP Bridge (Python) | 深度推理、架构分析 |
| Claude Code | ACP Bridge (Python) | 代码审阅、架构决策 |
| Codex CLI | ACP Bridge (Python) | 批量编码、测试执行 |

**关键特性：**
- RESTful API：`POST /api/v1/agents/tasks`（提交）→ `GET /api/v1/agents/tasks/{id}`（轮询）
- 跨 Agent 任务编排：`AgentOrchestrationService` 支持按策略分发任务
- 安全保护：CWD 白名单、输出 1MB 硬限、stdout/stderr 合并防死锁
- 11 个单元测试覆盖 Controller、OrchestrationService、BridgeService

Manifest v2 交换契约见 [ontology-manifest-spec-v2.md](docs/shared/ontology-manifest-spec-v2.md)；v1 Legacy（V01–V11）见 [ontology-manifest-spec.md](docs/shared/ontology-manifest-spec.md)。

## CI 流水线

项目配置了 GitHub Actions 自动构建与测试（[`.github/workflows/ci.yml`](.github/workflows/ci.yml)）：

- **触发**: `main` / `release/**` 分支 push 或 PR
- **环境**: `ubuntu-latest`, JDK 21 (Temurin), Maven 依赖缓存
- **步骤**: Checkout → `mvn compile -B -q` → `mvn verify -Pcoverage-check` → 上传 JaCoCo 报告 + 测试报告（7天保留）→ 飞书通知
- **运行耗时**: 约 59 秒
- **覆盖率**: JaCoCo 指令覆盖率 ≥ 70%
- **测试统计**: Java 220+ tests + MCP 177 tests, 0 failures（2026-07-02）

## 跨项目 E2E 测试

项目内置了从**项目1（OntologyManifest JSON 格式）→ 项目2（本平台）**的完整导入/导出端到端测试：

| 测试组 | 用例 | 场景 | 验证点 |
|:------:|:----:|------|:------:|
| CROSS-1~6 | 6 | 基础管线 | ManifestConverter → JSON 转换 → 结构化数据 → 发布 → 导出 |
| CROSS-7~9 | 3 | 最小清单 | 最小 JSON 通过验证 → 导入 → 全生命周期 |
| CROSS-10~12 | 3 | 全领域 (V12-V14) | Governance + Properties + EPC chains 完整映射 |
| CROSS-13~15 | 3 | Tags 已知差距 | tags/domainTags 丢失验证 + 导入可用 + 全链路 |
| CROSS-16~18 | 3 | scenario/subDomain | 字段保留 → 导入 → 全生命周期不丢失 |
| CROSS-19~21 | 3 | entity 角色映射 | aggregate_root / child_entity 角色保留回归 |
| CROSS-22~24 | 3 | 最新模块 | EntityLifecycle + AgentSemanticLayer 导入验证 |
| CROSS-25 | 1 | Round-trip | 导入→导出→再导入→再导出 全等比对 |

**测试文件**: [`Project1ToProject2E2ETest.java`](ontology-application/src/test/java/com/ontology/platform/application/service/manifest/Project1ToProject2E2ETest.java)  
**Fixtures**: [`ontology-application/src/test/resources/fixtures/`](ontology-application/src/test/resources/fixtures/)（7 个 fixture 文件）

覆盖场景：Kimi/Claude/Codex Agent 类型、多本体、6 维度状态机、语义层、生命周期、V12-V14 新域。
执行：`mvn test -pl ontology-application -am -Dtest="Project1ToProject2E2ETest"`

## 文档索引

| 文档 | 路径 |
|------|------|
| 故事地图 v1.2 | [docs/shared/PRD-本体建模平台-UserStoryMap-v1.2.md](docs/shared/PRD-本体建模平台-UserStoryMap-v1.2.md) |
| PRD v2.0 | [docs/shared/PRD-本体建模平台-v2.0.md](docs/shared/PRD-本体建模平台-v2.0.md) |
| TDD v2.0 | [docs/shared/TDD-本体建模平台-v2.0.md](docs/shared/TDD-本体建模平台-v2.0.md) |
| 项目结构速查 | [AGENTS.md](AGENTS.md) |
| API 契约 | [docs/shared/API契约-本体建模平台-v2.0.yaml](docs/shared/API契约-本体建模平台-v2.0.yaml) |
| Manifest 规范 | [docs/shared/ontology-manifest-spec-v2.md](docs/shared/ontology-manifest-spec-v2.md) |
| Phase 3 Spec | [docs/superpowers/specs/phase3-spec-v1.md](docs/superpowers/specs/phase3-spec-v1.md) |
| Phase 1 Spec | [docs/superpowers/specs/phase1-spec-v1.md](docs/superpowers/specs/phase1-spec-v1.md) |
| Phase 2 Spec | [docs/superpowers/specs/phase2-spec-v1.md](docs/superpowers/specs/phase2-spec-v1.md) |
| 工作日志 | [WORKLOG-2026-06-14.md](WORKLOG-2026-06-14.md) · [WORKLOG-2026-06-16.md](WORKLOG-2026-06-16.md) · [WORKLOG-2026-06-19.md](WORKLOG-2026-06-19.md) · [WORKLOG-2026-06-20.md](WORKLOG-2026-06-20.md) · [WORKLOG-2026-06-26.md](WORKLOG-2026-06-26.md) |
| 项目1 README / TODO | [`../Ontology/README.md`](../Ontology/README.md) · [`../Ontology/docs/TODO.md`](../Ontology/docs/TODO.md) |

## 测试

```bash
# 后端单元测试（220+ tests, 0 failures）
mvn test

# 覆盖率校验（JaCoCo ≥ 70%）
mvn verify -Pcoverage-check

# 集成测试（需 Docker — Testcontainers PG+AGE）
mvn verify -Pintegration-test

# 指定模块测试
mvn test -pl ontology-application

# 跨项目 E2E 导入/导出测试（25 个场景）
mvn test -pl ontology-application -am -Dtest="Project1ToProject2E2ETest"

# Agent 编排测试（11 tests）
mvn test -pl ontology-application -am -Dtest="*Agent*"

# MCP Server（177 tests）
cd mcp-server && npm test
```

> Integration Test（Testcontainers PG/Redis）需在 Docker 可用环境下单独执行；详见 TDD v2.0。

## 监控

| 端点 | 说明 |
|------|------|
| `GET /api/actuator/health` | 健康检查 |
| `GET /api/actuator/metrics` | Micrometer 指标 |
| `GET /api/actuator/prometheus` | Prometheus 抓取 |

## Coze 配置

项目根目录 `.coze` 文件定义了启动命令：

```toml
[project]
requires = ["java-21", "maven-3.8"]
entrypoint = "ontology-api/src/main/java/com/ontology/platform/api/OntologyPlatformApplication.java"

[dev]
build = ["sh", "-c", "mvn clean compile -DskipTests -q"]
run = ["sh", "-c", "mvn -pl ontology-api spring-boot:run -DskipTests -Dspring-boot.run.profiles=dev"]

[deploy]
build = ["sh", "-c", "mvn clean package -DskipTests -q"]
run = ["sh", "-c", "java -Dspring.profiles.active=dev -jar ontology-api/target/ontology-api-1.0.0-SNAPSHOT.jar"]
```

## 许可证

Apache License 2.0
