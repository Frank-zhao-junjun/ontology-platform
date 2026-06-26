# Ontology Platform — Claude 项目指令

> 本体模型服务平台（Ontology Service Platform）是一个企业级本体服务治理平台，基于 DDD 分层架构。
> 仓库：https://github.com/Frank-zhao-junjun/ontology-platform

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 3.2.5 |
| Java | JDK | 21 |
| 数据库 | PostgreSQL + Apache AGE | 15+ / 1.5.x |
| 缓存/队列 | Redis | 7.x |
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
├── frontend/                  # Vite + Tailwind
├── docs/
│   ├── shared/                # PRD、TDD、API 契约、Manifest 规范
│   ├── superpowers/specs/     # Phase 1/2/3 实施 Spec
│   └── import/                # 导入模块分析与规划
├── .github/workflows/         # CI（GitHub Actions）
├── docker/                    # Docker Compose
└── scripts/                   # 启动脚本
```

## 架构约束

1. **DDD 分层**：domain 层不依赖 infrastructure 层。Repository 接口在 domain，实现在 infrastructure。
2. **Flyway 管理 Schema**：禁止 Hibernate ddl-auto，迁移脚本含 PG 专有语法（uuid-ossp、JSONB），不能用 H2 替代。
3. **统一响应格式**：`{ code, message, data, meta: { trace_id } }`
4. **MyBatis-Plus** 作为 ORM，PO 在 infrastructure 层，Entity 在 domain 层。
5. **测试要求**：174 tests, 0 failures。全量跑 `mvn test` 应全部通过。

## 关联项目

- **项目1**（D:\AI\ontology）：上游本体设计工具（Next.js），产出 OntologyProject JSON。
- **项目2**（本项目）：下游治理平台，接收项目1 导出并持久化。
- 导入链路：`POST /api/v1/ontologies/import` 已打通，6 个 E2E 场景覆盖。
- 跨项目文档：`docs/project1-to-project2-mapping.md`、`docs/shared/项目1-项目2对接差距分析.md`

## 常用命令

```bash
# 编译
mvn clean compile -DskipTests

# 全量测试
mvn test

# 跨项目 E2E
mvn test -pl ontology-application -am -Dtest="Project1ToProject2E2ETest"

# 开发启动
mvn -pl ontology-api spring-boot:run -DskipTests -Dspring-boot.run.profiles=dev

# MCP Server
cd mcp-server && npm ci && npm run dev
```

## 版本历史

- **Phase 0**: 基础 CRUD + 图查询（2026-06-08~13）
- **Phase 1**: Manifest V01-V11 导入/校验/发布 + MCP Server（2026-06-13~14）
- **Phase 2**: JobQueue + RateLimiter + Webhook + Agent 编排 + CI（2026-06-14~20）
- **Phase 3**: V2 交换契约 + V12-V14 新域（19 表） + E2E 测试（2026-06-19~26）

## 用户偏好

- 遵循中文回复，简洁直接
- 先确认后行动（不可逆操作）
- 文档与代码同步更新
