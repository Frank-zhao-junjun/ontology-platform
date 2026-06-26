## 项目概述

本体模型服务平台（Ontology Service Platform）是一个企业级本体服务治理平台，基于领域驱动设计（DDD）和本体论架构，为本体模型提供持久化、查询、校验、发布、Agent 编排和 MCP 协议适配能力。

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 3.2.5 |
| Java 版本 | Java | 21 |
| 数据库 | PostgreSQL + Apache AGE | 15+ / 1.5.x |
| 缓存 | Redis | 7.x |
| API 文档 | SpringDoc OpenAPI | 2.5.0 |
| ORM | MyBatis-Plus | 3.5.6 |
| 数据库迁移 | Flyway | 10.11.0 |
| 构建工具 | Maven | 3.8+ |

## 目录结构

```
ontology-platform/
├── ontology-api/              # API层（Controller、DTO、配置）
├── ontology-application/      # 应用层（Service接口与实现、DTO）
├── ontology-domain/           # 领域层（Entity、Value Object、Repository接口）
├── ontology-infrastructure/   # 基础设施层（Repository实现、数据库配置、图服务）
├── ontology-common/           # 公共模块（工具类、异常、常量、枚举）
├── frontend/                  # 前端（Vite + Tailwind，当前用于PPT生成）
├── docker/                    # Docker配置
├── docs/                      # 文档
└── scripts/                   # 启动脚本
```

## 关键入口 / 核心模块

- **应用启动入口**: `ontology-api/src/main/java/com/ontology/platform/api/OntologyPlatformApplication.java`
- **API 控制器**:
  - `OntologyController` - 本体 CRUD、发布/归档/验证
  - `ObjectTypeController` - 对象类型管理
  - `RelationController` - 关系管理
  - `GraphTraversalController` - 图遍历查询
  - `UploadController` - 文件上传/导入/导出
- **核心服务**:
  - `OntologyServiceImpl` - 本体、对象类型、属性、关系的统一服务
  - `GraphQueryServiceImpl` - 图遍历查询服务（委托 AgeQueryExecutor）
  - `AgeGraphService` - Apache AGE 图存储操作
- **领域模型**:
  - `Ontology` - 本体聚合根
  - `ObjectType` - 对象类型实体
  - `Relation` - 关系实体
  - `Property` - 属性值对象
  - `RelationProperty` - 关系属性值对象

## 运行与预览

```bash
# 编译
mvn clean compile

# 运行测试
mvn test

# 启动应用（开发环境）
mvn spring-boot:run -Pdev

# 或使用 Docker Compose
cd docker && cp .env.example .env && docker-compose up -d
```

API 文档地址：启动后访问 `/swagger-ui.html`

## 用户偏好与长期约束

- 遵循 DDD 分层架构：domain 层不依赖 infrastructure 层
- **Codex 工具约束：** 同一文件/命令只读一次；`Get-Content` 用 `-Raw -Encoding UTF8`；拿到 tool 输出后立即总结或继续，禁止重复执行相同命令
- Repository 接口定义在 domain 层，实现在 infrastructure 层
- 关系仓储（RelationRepository）当前使用内存存储（ConcurrentHashMap），待 PostgreSQL + AGE 就绪后替换
- 属性仓储（PropertyRepository）已接入 MyBatis-Plus
- 图操作依赖 Apache AGE 扩展，需要 PostgreSQL 15+ 环境
- Java 版本要求 21+

## 常见问题和预防

- `RelationRepositoryImpl` 使用内存存储，重启后数据丢失。生产环境需替换为数据库实现。
- `AgeGraphService` 和 `AgeQueryExecutorImpl` 依赖 Apache AGE 扩展，在无 AGE 环境下会降级（日志警告）。
- `queryObjects` 方法当前返回空结果集，待对象实例存储层就绪后接入实际数据。
- `GraphService`（infrastructure/graph）是占位实现，实际图操作由 `AgeGraphService` 和 `AgeQueryExecutorImpl` 承担。

## graphify

This project has a knowledge graph at graphify-out/ with god nodes, community structure, and cross-file relationships.

When the user types `/graphify`, invoke the `skill` tool with `skill: "graphify"` before doing anything else.

Rules:
- For codebase questions, first run `graphify query "<question>"` when graphify-out/graph.json exists. Use `graphify path "<A>" "<B>"` for relationships and `graphify explain "<concept>"` for focused concepts. These return a scoped subgraph, usually much smaller than GRAPH_REPORT.md or raw grep output.
- Dirty graphify-out/ files are expected after hooks or incremental updates; dirty graph files are not a reason to skip graphify. Only skip graphify if the task is about stale or incorrect graph output, or the user explicitly says not to use it.
- If graphify-out/wiki/index.md exists, use it for broad navigation instead of raw source browsing.
- Read graphify-out/GRAPH_REPORT.md only for broad architecture review or when query/path/explain do not surface enough context.
- After modifying code, run `graphify update .` to keep the graph current (AST-only, no API cost).
