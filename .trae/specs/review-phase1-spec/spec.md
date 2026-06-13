# Phase 1 Spec Review Spec v1.1

> 基于 phase1-spec-v1.md | 源自 US v1.1 | 2026-06-13 | Review | ✅ Code audit 完成
> 权威实施日志: [WORKLOG-2026-06-13.md](file:///d:/AI/ontology-platform/WORKLOG-2026-06-13.md)

## Why

当前 `docs/superpowers/specs/phase1-spec-v1.md` 覆盖了 US v1.1 的 12 条 P0 故事。Phase 0 Foundation Hardening（Task 1~5）识别出以下预存缺陷，代码审计后发现 Task 2~4 已先行修复：

| 缺陷 | 代码状态 | 任务 |
|------|---------|------|
| `RelationRepositoryImpl` 内存存储 | ✅ `RelationPOMapper` + 10 用例 | Task 1 ✅ |
| `queryObjects` 空返回 | ✅ `ObjectInstanceRepositoryImpl` 已实现，分页 + coreData/extendedData 合并 | Task 2 ✅ |
| `GraphService` 占位实现 | ✅ `AgeGraphService` 委派 + `@Deprecated` 旧签名 + `GraphProperties.degraded` | Task 3 ✅ |
| `InMemoryAgentTokenRepository` 内存存储 | ✅ `AgentTokenRepositoryImpl` (MyBatis-Plus) | Task 4 ✅ |
| Token hash 用 Base64 而非 bcrypt | ✅ `BCryptPasswordEncoder(strength=10)` | Task 4 ✅ |
| Testcontainers 集成测试缺失 | ⚠️ 5.1+5.2 已部分（PostgresTestContainer + RelationRepositoryIT 4 用例） | Task 5 ⚠️ |
| Task 4 Base64 历史 token 降级 | ❌ 未实现（spec §10 声明但代码无 fallback） | Task 4 ❌ |

## Version History

| 版本 | 日期 | 变更 |
|------|------|------|
| **v1.1** | 2026-06-13 | Review Phase — Tasks 1~3 完成；Task 4 主体完成但 Base64 降级 (4.4) 未实现；Task 5 部分完成 |
| v1.0 | 2026-06-13 | 初始 Draft，覆盖 US v1.1 全部 12 条 P0 故事 |

## What Changes

- **新增** `Phase 0 - Foundation Hardening` 章节，覆盖 V1 仓储层与安全基线的一致性修复
- **更新** 第 3 节"数据模型"：补充仓储实现状态列
- **更新** 第 5.5 节"Auth 流程"：Token 哈希算法（bcrypt）+ AgentTokenRepository 已替换
- **更新** 第 6 节"安全基线"：token_hash 约束、密码学库版本
- **更新** 第 7 节"实施顺序"：Phase 0 作为 Phase 1a 前置门
- **更新** 第 8 节"测试策略"：Unit Test + Integration Test 分层

## Impact

- Affected specs: phase1-spec-v1.md（§3 数据模型, §5.5 Auth, §6 安全基线, §7 实施顺序, §8 测试）
- Affected code:
  - [RelationRepositoryImpl.java](file:///d:/AI/ontology-platform/ontology-infrastructure/src/main/java/com/ontology/platform/infrastructure/repository/RelationRepositoryImpl.java) ✅ Task 1
  - [ObjectInstanceRepositoryImpl.java](file:///d:/AI/ontology-platform/ontology-infrastructure/src/main/java/com/ontology/platform/infrastructure/repository/ObjectInstanceRepositoryImpl.java) ✅ Task 2
  - [GraphService.java](file:///d:/AI/ontology-platform/ontology-infrastructure/src/main/java/com/ontology/platform/infrastructure/graph/GraphService.java) ✅ Task 3
  - [AgentTokenRepositoryImpl.java](file:///d:/AI/ontology-platform/ontology-infrastructure/src/main/java/com/ontology/platform/infrastructure/repository/governance/AgentTokenRepositoryImpl.java) ✅ Task 4
  - [GovernanceServiceImpl.java](file:///d:/AI/ontology-platform/ontology-application/src/main/java/com/ontology/platform/application/service/governance/impl/GovernanceServiceImpl.java) ✅ Task 4 (bcrypt)

## ADDED Requirements

### Requirement: All V1 Repositories SHALL Use MyBatis-Plus + PostgreSQL
系统 SHALL 移除所有 `ConcurrentHashMap` 内存存储的 Repository 实现，统一迁移到 MyBatis-Plus + `relation_definition` / `object_instance` 等 V1 表。

> 注：`ObjectType` / `Ontology` / `Property` 的 Repository 已是 MyBatis-Plus，无需迁移。仅关系、对象实例、Agent Token 三处需要处理。
> ✅ 代码审计：三处均已实现。

#### Scenario: Relation persistence survives restart
- **WHEN** 通过 `POST /api/v1/ontologies/{id}/relations` 创建一个新关系
- **AND** 进程重启
- **THEN** `GET /api/v1/ontologies/{id}/relations` 仍能返回该关系（不再丢失）

#### Scenario: Relation uniqueness constraint enforced by DB
- **WHEN** 试图在同一 ontology 下创建同名（`name`）关系两次
- **THEN** DB UNIQUE 约束 `uk_relation_ontology_name` 抛 `DataIntegrityViolationException`，Service 层翻译为 `BusinessException`

### Requirement: queryObjects SHALL Return Real Data
`OntologyServiceImpl#queryObjects` SHALL 从 `object_instance` 表（V1）查询并返回结果，而非空 `ObjectListResponse`。

> ✅ 代码审计：已实现 `ObjectInstanceRepositoryImpl`（MyBatis-Plus），`queryObjects` 已完成分页 + coreData/extendedData 合并。

#### Scenario: Object listing by type
- **WHEN** 调用 `GET /api/v1/ontologies/{id}/objects?type=Equipment`
- **AND** `object_instance` 表中存在 5 条匹配记录
- **THEN** 返回包含 5 条数据的 `ObjectListResponse`，带分页元信息

### Requirement: GraphService SHALL Delegate to AgeGraphService
`infrastructure/graph/GraphService` SHALL 不再是空操作的占位实现。其 `createEdge` / `deleteEdge` MUST 委派到 `AgeGraphService`。无 AGE 扩展时 MUST 抛 `BusinessException("GRAPH_UNAVAILABLE", 503)`（而非静默跳过），由上游决定是否降级到日志模式（须显式配置 `graph.degraded=true`）。

> ✅ 代码审计：`GraphService` 已委派 `AgeGraphService`，`GraphProperties.degraded=false` 默认关闭降级。

#### Scenario: Edge creation reaches AGE
- **WHEN** 关系被创建，调用 `GraphService.createEdge(src, dst, "owns")`
- **THEN** `AgeGraphService` 收到同样的参数并执行 Cypher `MATCH ... CREATE ...`
- **AND** 失败时返回错误而非 silent log

### Requirement: Agent Token Hash SHALL Use bcrypt
`GovernanceServiceImpl#hashToken` SHALL 使用 `BCryptPasswordEncoder`（Spring Security）替代 Base64 编码。`agent_token.token_hash` 列 SHALL 存储 bcrypt 格式（`$2a$...`）。

> ✅ 代码审计：`BCryptPasswordEncoder(strength=10)` 已使用，`hashToken` / `matchesToken` 已实现，`InMemoryAgentTokenRepository` 已删除。

#### Scenario: Token issuance produces bcrypt hash
- **WHEN** 签发新 Agent Token
- **THEN** `token_hash` 字段以 `$2a$10$...` 开头
- **AND** 用相同明文调用 `matches()` 验证返回 true

### Requirement: All Repository Tests SHALL Mock at the Mapper Boundary （Unit Test）
Repository 实现测试 SHALL 仅 mock `*POMapper` 与 `*Converter`，不引入 in-memory store 的语义幻觉。

#### Scenario: Unit test contract
- **WHEN** 编写 `RelationRepositoryImplTest`
- **THEN** `relationPOMapper` 是 mock，所有断言基于 mapper 返回值 + converter 转换结果

## MODIFIED Requirements

### Requirement: Phase 1 实施顺序（§7）
**原**：`1a Flyway V2-V6` → `1b Domain` → `1c MCP` → `1d E2E`
**新**：插入 `Phase 0 (前置门)`：`Relation` / `ObjectInstance` / `AgentToken` 仓储迁移 + bcrypt + GraphService 委派，作为 Phase 1a 的 `BLOCKING` 前置。**Phase 0 代码已完成，仅缺 Testcontainers CI 基线。**

### Requirement: 测试策略（§8）
**原**：Repository → Testcontainers
**新**：分层策略：
- **Unit Test**：mock `*POMapper` + `*Converter`，验证转换逻辑和边界条件 ✅ Task 1.5
- **Integration Test**：Testcontainers（PG + AGE），验证完整 SQL 执行和持久化行为 ❌ Task 5
- CI 必须在 PR 上跑 Testcontainers 集成测试 ❌ Task 5

### Requirement: 安全基线（§6）— token_hash
**原**：未明确 `token_hash` 算法
**新**：MUST 使用 `BCryptPasswordEncoder`（strength=10），明文 token 仅在签发响应中返回一次。 ✅ 已实现

## REMOVED Requirements
无（保留向后兼容路径：Base64 历史 token 在升级窗口内可降级验证，过期后强制 bcrypt）。

## 升级路线

```text
Phase 0 (v1.0 → v1.1 Review)         结果                         下一步
    Task 1 ✅ Relation                     │                           │
    Task 2 ✅ ObjectInstance               Phase 0 代码 70% 已完成     剩 Task 4.4 降级
    Task 3 ✅ GraphService                 主体已就位（3 项全绿）       + Task 5 全部
    Task 4 ⚠️ bcrypt 13% / 降级 2%        ⚠️ SubTask 4.4 缺口          （5.2 + 5.3）
    Task 5 ⚠️ 5.1+5.2(Rel) 15% / 5.3 ❌    仍需 5.2 补全 + 5.3 CI        │
                                          → v1.1 Final                 │
```

- v1.1 Final = v1.0 Draft + Phase 0 修复（Tasks 1~3 全部代码就绪；Task 4 主体就绪但 Base64 降级未实现；Task 5 5.1+5.2 部分就绪）。**真正未完成的入口：Task 4.4 降级 + Task 5 全部（5.2 补全 + 5.3 CI）**。

## §9 验证清单

### §9.1 Phase 0 冒烟（Phase 0 完成时验证）
- [ ] 启动 Docker Compose（PG + AGE + Redis），创建本体 → 对象类型 → 属性 → 关系 → 对象实例
- [ ] 重启进程，查询关系仍存在（持久化验证）
- [ ] 签发 Agent Token → 验证 bcrypt hash 落库（`$2a$...`）

### §9.2 Phase 1 冒烟（Phase 1 完成时验证）
- [ ] 携带 Agent Token 调用 MCP tool → 鉴权通过
- [ ] 端到端链路：创建本体 → 对象类型 → 属性 → 关系 → 对象实例 → 图遍历 → 重启 → 数据仍在

### §9.3 CI 门禁
- [ ] **Task 4.4 降级** + **Task 5 全部**（5.2 补全 ObjectInstanceIT+PropertyIT + 5.3 GitHub Actions CI）→ CI 通过 → 合并升版 phase1-spec-v1.md → v1.1 Final
- ⚠️ 当前 CI 门禁绑定的"Task 5"不完整，需同时绑定 Task 4.4 降级（参见 Review Y2）

## §10 兼容性说明

| 事项 | 说明 |
|------|------|
| DB schema | 沿用 V1 表，`agent_token.token_hash` 当前为 VARCHAR(500)，bcrypt 60 字符足够，无需 DDL 变更 |
| REST API | 契约不变 |
| 现有 Repository | `ObjectTypeRepository` / `OntologyRepository` / `PropertyRepository` 已是 MyBatis-Plus |
| Testcontainers | `postgres:15-age` 镜像可同时覆盖 PG + AGE 需求 |
| GraphService 错误码 | `BusinessException("GRAPH_UNAVAILABLE", 503)`；`GraphProperties.degraded` 默认 false |
| 行为变化 | Token 哈希格式（一次性升级，老 token 自然过期或强制重发），其余黑盒兼容 |
| **Base64 降级兼容** | ❌ **待实施（Task 4.4）**——spec 声明"90 天升级窗口内可降级验证"，但 `GovernanceServiceImpl#matchesToken` 当前只走 `passwordEncoder.matches()`，**无 Base64 fallback**。需补：以 `$2a$` 开头 → bcrypt；否则视为历史 Base64（SHA-256 + Base64 编码），重算后比对。 |
| **剩余 Phase 1 InMemory** | **11 个**入 Phase 1 工单（见 tasks.md 表） |
