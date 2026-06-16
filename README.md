# Ontology Platform — 本体建模平台

企业级本体管理系统，基于 DDD 分层架构，提供本体 CRUD、Manifest 导入/发布、行为/事件/EPC 查询、Agent 治理，以及 Phase 2 异步任务、Webhook、幂等与限流能力。MCP Server 将 REST API 暴露为 AI Agent 可调用的 MCP 工具。

**仓库**: [Frank-zhao-junjun/ontology-platform](https://github.com/Frank-zhao-junjun/ontology-platform)

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
│   └── superpowers/specs/     # Phase 1 / Phase 2 实施 Spec
├── docker/                    # Docker Compose（PG + Redis + App + MCP）
├── scripts/                   # 启动脚本、证书生成等
├── .coze                      # Coze CLI 配置文件
└── README.md
```

## 快速开始

### 前置条件

- JDK 21+
- Maven 3.8+
- Node.js 22+（仅 MCP Server 开发）
- Docker & Docker Compose（推荐完整环境）

### 方式一：Coze CLI（推荐开发环境）

项目已配置 `.coze` 文件，支持一键启动：

```bash
# 开发模式（H2内存数据库，无需Docker）
coze dev

# 生产模式
coze build
coze start
```

服务就绪后：

- Spring Boot: `http://localhost:8080/api`
- Swagger UI: `http://localhost:8080/api/swagger-ui.html`
- H2 Console: `http://localhost:8080/api/h2-console`
- 健康检查: `http://localhost:8080/api/actuator/health`

> **注意**: `coze dev` 使用 H2 内存数据库，数据不会持久化。如需 PostgreSQL，请使用 Docker Compose 方式。

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

```bash
# 编译
mvn clean compile

# 运行测试
mvn test

# 开发模式启动（H2内存数据库）
mvn spring-boot:run -Dspring.profiles.active=dev

# 生产模式启动（需配置PostgreSQL）
mvn spring-boot:run -Pprod
```

### 开发环境配置

项目提供两种数据库配置：

| 配置文件 | 数据库 | 用途 |
|----------|--------|------|
| `application-dev.yml` | H2 内存数据库 | 快速开发、单元测试 |
| `application.yml` | PostgreSQL + AGE | 生产环境、完整功能 |

切换配置：
```bash
# 使用开发配置（默认）
-Dspring.profiles.active=dev

# 使用生产配置
-Dspring.profiles.active=prod
```

**H2 Console 访问**: `http://localhost:8080/api/h2-console`
- JDBC URL: `jdbc:h2:mem:ontology`
- Username: `sa`
- Password: (空)

### MCP Server

```bash
cd mcp-server
npm ci
npm run dev    # 开发
npm test       # Vitest
```

环境变量见 [`mcp-server/README.md`](mcp-server/README.md)。

## API 约定

- **基址**: `http://localhost:8080`（`server.servlet.context-path=/api`）
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
| Manifest | `/api/v1/manifests/*` | 导入、预览、发布、导出 |
| 行为/事件/EPC | `/api/v1/ontologies/{id}/actions|events|epc` | 领域定义查询 |
| 治理 | `/api/v1/governance/*` | Token、角色、权限、审批 |
| 上传/导入 | `/api/v1/uploads/*`, `/api/v1/imports/*` | 分片上传与导入任务 |
| 异步任务 | `/api/v1/jobs` | 提交/查询/取消 Job（Phase 2） |
| Webhook | `/api/v1/webhooks` | 订阅 job 完成/失败回调（Phase 2） |
| 可观测 | `/api/actuator/prometheus` | Prometheus 指标 |

Manifest 格式与 V01–V11 校验规则见 [docs/shared/ontology-manifest-spec.md](docs/shared/ontology-manifest-spec.md)。

## 文档索引

| 文档 | 路径 |
|------|------|
| 故事地图 v1.2 | [docs/shared/PRD-本体建模平台-UserStoryMap-v1.2.md](docs/shared/PRD-本体建模平台-UserStoryMap-v1.2.md) |
| PRD v2.0 | [docs/shared/PRD-本体建模平台-v2.0.md](docs/shared/PRD-本体建模平台-v2.0.md) |
| TDD v2.0 | [docs/shared/TDD-本体建模平台-v2.0.md](docs/shared/TDD-本体建模平台-v2.0.md) |
| API 契约 | [docs/shared/API契约-本体建模平台-v2.0.yaml](docs/shared/API契约-本体建模平台-v2.0.yaml) |
| Manifest 规范 | [docs/shared/ontology-manifest-spec-v2.md](docs/shared/ontology-manifest-spec-v2.md) |
| Phase 3 Spec | [docs/superpowers/specs/phase3-spec-v1.md](docs/superpowers/specs/phase3-spec-v1.md) |
| Phase 1 Spec | [docs/superpowers/specs/phase1-spec-v1.md](docs/superpowers/specs/phase1-spec-v1.md) |
| Phase 2 Spec | [docs/superpowers/specs/phase2-spec-v1.md](docs/superpowers/specs/phase2-spec-v1.md) |
| 工作日志 | [WORKLOG-2026-06-14.md](WORKLOG-2026-06-14.md) |

## 测试

```bash
# 后端单元测试
mvn test

# MCP Server
cd mcp-server && npm test

# 测试覆盖率（可选）
mvn test jacoco:report
```

> Integration Test（Testcontainers PG/Redis）需在 Docker 可用环境下单独执行；详见 TDD v2.0 与 Worklog §十七。

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
entrypoint = "Ontology-platform/ontology-api/src/main/java/com/ontology/platform/OntologyApiApplication.java"

[dev]
build = ["sh", "-c", "cd Ontology-platform && mvn clean compile -DskipTests -q"]
run = ["sh", "-c", "cd Ontology-platform && mvn spring-boot:run -pl ontology-api -am -DskipTests -Dspring.profiles.active=dev"]

[deploy]
build = ["sh", "-c", "cd Ontology-platform && mvn clean package -DskipTests -q"]
run = ["sh", "-c", "cd Ontology-platform && java -Dspring.profiles.active=dev -jar ontology-api/target/ontology-api-1.0.0-SNAPSHOT.jar"]
```

## 许可证

Apache License 2.0
