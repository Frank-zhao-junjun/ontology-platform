---
name: ontology-platform-best-practices
description: 本体模型服务平台开发最佳实践 — 文档闭环、Spec原子化、层级分工、测试策略、Worklog规范
version: 1.0.0
tags: [ddd, spec-driven, spring-boot, mcp, java, nodejs]
---

# ontology-platform Best Practices

## 文档六层闭环

```
US → PRD → Spec → Task → Test → Worklog
```

- **US**: 标注前置依赖，验收标准可勾选
- **PRD**: 版本化 (v1.0 → v1.1 → v2.0)，每版记录变更
- **Spec**: 主Spec是唯一权威来源，审核直接合并进主Spec，不留隔离文件
- **Task**: 原子化，≤5文件/任务，可独立验证
- **Test**: 分层策略 (Unit → Integration → MCP → E2E)
- **Worklog**: 每阶段 +N/-M 文件数统计

## DDD 分层架构

```
ontology-api/               Controller, DTO, 配置
ontology-application/       Service 接口+实现
ontology-domain/            Entity, Repository接口, Value Object
ontology-infrastructure/    Repository实现, MyBatis Mapper, Flyway
```

- domain 层绝不依赖 infrastructure 层
- 所有 Repository 用 MyBatis-Plus，禁止 ConcurrentHashMap 内存存储
- Flyway 按模块独立迁移文件 (V1~V7+)

## 双模块架构

| 模块 | 端口 | 技术 |
|------|------|------|
| Spring Boot | :8080 | Java 25, DDD |
| MCP Server | :3001 | Node.js 22, Express, JSON-RPC 2.0 |

- MCP Server 独立于 Spring Boot，不干扰编译
- MCP → Platform 通过 REST + API Key 通信
- 同一 docker-compose 编排

## 测试分层

| 层 | 框架 | 测试什么 |
|----|------|---------|
| Domain Unit | JUnit 5 + Mockito | 业务规则 |
| Controller | MockMvc | 契约测试 |
| Repository IT | Testcontainers | 真实 PG+AGE |
| MCP Unit | Vitest | Tool 逻辑+RBAC |
| MCP HTTP E2E | Vitest + Express | JSON-RPC 2.0 全链路 |

## 文档路径

```
.hermes/                   计划 + User Stories
docs/shared/               PRD, TDD, API契约, 故事地图
docs/superpowers/specs/    技术 Spec (版本化)
mcp-server/                Node.js 独立项目
WORKLOG-YYYY-MM-DD.md     变更日志 (根目录)
```

## 常见 Pitfalls

| 坑 | 症状 | 解法 |
|----|------|------|
| 审核产物隔离 | .trae/ 下有 review spec，主Spec还是旧的 | 审核结论直接合并进主Spec |
| 工作量不量化 | 只说"完成了" | 每阶段记 +N/-M 文件数 |
| 文档分散无索引 | 找不到对应文档 | 用 docs/README.md 做索引 |

## 启动流程

1. 读 WORKLOG-*.md 了解当前进度
2. 读主Spec (docs/superpowers/specs/phase*-spec-v*.md)
3. 确认阶段标签和版本号
4. 按 P1~P4 优先级执行
