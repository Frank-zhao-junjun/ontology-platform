# Phase 1 Spec Review Spec v1.2

> 基于 phase1-spec-v1.md v1.1 Final（提交 `366972d` 21:53） | 源自 US v1.1 | 2026-06-13 | Review → **2026-06-20 闭环 v1.2**
> 权威实施日志: [WORKLOG-2026-06-14.md](file:///d:/AI/ontology-platform/WORKLOG-2026-06-14.md)
> 本文件由 WORKLOG 反向重建（2026-06-13 代码审计就绪版；2026-06-20 更新 Task 4.4 闭环）

## Why
`docs/superpowers/specs/phase1-spec-v1.md` v1.0 Draft 覆盖了 US v1.1 的 12 条 P0 故事，但遗漏了 V1 基础表对应的仓储层完整性、以及若干预存在缺陷（`RelationRepositoryImpl` 内存存储、`queryObjects` 空实现、`GraphService` 占位、`InMemoryAgentTokenRepository` 内存、Token hash 用 Base64 而非 bcrypt、Testcontainers 集成测试缺失）。这些缺陷直接威胁 Phase 1 的发布质量。本次代码审计（WORKLOG §四 23:30+）确认 Tasks 1~5 已完成。Task 4 SubTask 4.4 降级已于 2026-06-14 实现（`GovernanceServiceImpl#matchesToken` — BCrypt + Base64 fallback + `legacyHashToken` + `constantTimeEquals`），2026-06-20 确认闭环。

## What Changes
- **已修复** V1 仓储层（Relation / ObjectInstance / AgentToken）迁至 MyBatis-Plus + PostgreSQL
- **已修复** `queryObjects` 接入真实 `object_instance` 数据
- **已修复** `GraphService` 委派 `AgeGraphService` + `GRAPH_UNAVAILABLE(8005)` 错误码 + `GraphProperties.degraded` 开关
- **已修复** Token 哈希改用 `BCryptPasswordEncoder(strength=10)`
- **已修复** Testcontainers 集成测试：Relation 4 + ObjectInstance 5 + Property 6 = 15 IT + CI pipeline
- **已修复** Task 4.4 Base64 历史 token 降级兼容（`GovernanceServiceImpl#matchesToken` — BCrypt + Base64 fallback + `legacyHashToken` + `constantTimeEquals`）
- **未跟踪** Phase 1 遗留 InMemory 11 个（行为/上传包下 5 个，WORKLOG 完整列出）

## Impact
- Affected specs: phase1-spec-v1.md（§3 数据模型, §5.5 Auth, §6 安全基线, §7 实施顺序, §8 测试）
- Affected code:
  - [RelationRepositoryImpl.java](file:///d:/AI/ontology-platform/ontology-infrastructure/src/main/java/com/ontology/platform/infrastructure/repository/RelationRepositoryImpl.java)（已修复 ✅）
  - [ObjectInstanceRepositoryImpl.java](file:///d:/AI/ontology-platform/ontology-infrastructure/src/main/java/com/ontology/platform/infrastructure/repository/ObjectInstanceRepositoryImpl.java)（已修复 ✅）
  - [GraphService.java](file:///d:/AI/ontology-platform/ontology-infrastructure/src/main/java/com/ontology/platform/infrastructure/graph/GraphService.java)（已修复 ✅）
  - [AgentTokenRepositoryImpl.java](file:///d:/AI/ontology-platform/ontology-infrastructure/src/main/java/com/ontology/platform/infrastructure/repository/governance/AgentTokenRepositoryImpl.java)（已修复 ✅）
  - `GovernanceServiceImpl#matchesToken`（✅ Task 4.4 BCrypt + Base64 fallback 已实现）

## ADDED Requirements

### Requirement: All V1 Repositories SHALL Use MyBatis-Plus + PostgreSQL ✅
系统 SHALL 移除所有 `ConcurrentHashMap` 内存存储的 Repository 实现，统一迁移到 MyBatis-Plus + `relation_definition` / `object_instance` / `property_definition` / `object_type` / `ontology` 等 V1 表。

#### Scenario: Relation persistence survives restart
- **WHEN** 通过 `POST /api/v1/ontologies/{id}/relations` 创建一个新关系
- **AND** 进程重启
- **THEN** `GET /api/v1/ontologies/{id}/relations` 仍能返回该关系

#### Scenario: Relation uniqueness constraint enforced by DB
- **WHEN** 试图在同一 ontology 下创建同名（`name`）关系两次
- **THEN** DB UNIQUE 约束 `uk_relation_ontology_name` 抛 `DataIntegrityViolationException`

> ✅ 代码审计：RelationRepositoryImpl 委派 RelationPOMapper，10 单元测试通过；ObjectInstanceRepositoryImpl 已实现（MyBatis-Plus）。

### Requirement: queryObjects SHALL Return Real Data ✅
`OntologyServiceImpl#queryObjects` SHALL 从 `object_instance` 表（V1）查询并返回结果，而非空 `ObjectListResponse`。

#### Scenario: Object listing by type
- **WHEN** 调用 `GET /api/v1/ontologies/{id}/objects?type=Equipment`
- **AND** `object_instance` 表中存在 5 条匹配记录
- **THEN** 返回包含 5 条数据的 `ObjectListResponse`，带分页元信息

> ✅ 代码审计：queryObjects 已实现分页 + coreData/extendedData 合并。

### Requirement: GraphService SHALL Delegate to AgeGraphService ✅
`infrastructure/graph/GraphService` SHALL 委派到 `AgeGraphService`。无 AGE 扩展时 MUST 抛 `BusinessException("GRAPH_UNAVAILABLE", 503)`，由 `GraphProperties.degraded` 控制是否降级到日志模式。

#### Scenario: Edge creation reaches AGE
- **WHEN** 调用 `GraphService.createEdge(relation)`
- **THEN** `AgeGraphService` 收到参数并执行 Cypher
- **AND** 失败时返回 `BusinessException` 而非 silent log

> ✅ 代码审计：GraphService 委派 AgeGraphService + GRAPH_UNAVAILABLE(8005) + GraphProperties.degraded 默认 false，9 测试通过。

### Requirement: Agent Token Hash SHALL Use bcrypt ✅
`GovernanceServiceImpl#hashToken` SHALL 使用 `BCryptPasswordEncoder(strength=10)`。`agent_token.token_hash` 列 SHALL 存储 bcrypt 格式（`$2a$...`）。

#### Scenario: Token issuance produces bcrypt hash
- **WHEN** 签发新 Agent Token
- **THEN** `token_hash` 字段以 `$2a$10$...` 开头
- **AND** 用相同明文调用 `matches()` 验证返回 true

> ✅ 代码审计：BCryptPasswordEncoder(strength=10) + AgentTokenRepositoryImpl（MyBatis-Plus）+ InMemoryAgentTokenRepository 已删除。

### Requirement: All Repository Tests SHALL Mock at the Mapper Boundary ✅
Repository 实现测试 SHALL 仅 mock `*POMapper` 与 `*Converter`，不引入 in-memory store 的语义幻觉。

> ✅ 代码审计：RelationRepositoryImplTest 10 + ObjectInstanceRepositoryImplTest 10 + PropertyRepositoryImplTest 6 全 mock-based。

### Requirement: Testcontainers Integration Tests SHALL Cover All Repositories ✅
所有 V1 仓储 SHALL 有 Testcontainers（PostgreSQL + AGE）集成测试。

> ✅ 代码审计：RelationRepositoryIT 4 + ObjectInstanceRepositoryIT 5 + PropertyRepositoryIT 6 = 15 IT + CI pipeline（java-backend + mcp-server + gate）。

### Requirement: Base64 Token Backward Compatibility (Task 4.4) ✅
`GovernanceServiceImpl#matchesToken` SHALL 支持历史 Base64 token 降级验证：以 `$2a$` 开头 → bcrypt，否则视为历史 Base64（SHA-256 + Base64）重算比对。

> ✅ 已实现：`matchesToken` 包含 BCrypt + Base64 fallback（`legacyHashToken` + `constantTimeEquals` + `log.warn`），提交 `c24ba0b`。

## MODIFIED Requirements

### Requirement: Phase 1 实施顺序（§7）
**原**：`1a Flyway V2-V6` → `1b Domain` → `1c MCP` → `1d E2E`
**新**：插入 `Phase 0 (前置门)`：`Relation` / `ObjectInstance` / `AgentToken` 仓储迁移 + bcrypt + GraphService 委派 + Testcontainers 集成测试，作为 Phase 1a 的 `BLOCKING` 前置。

> ✅ Phase 0 100% 已完成（提交 `33d6c26` / `67d0f75` / `57ee10e` / `c24ba0b` 等），含 Task 4.4 降级。

### Requirement: 测试策略（§8）
**原**：Repository → Testcontainers
**新**：Repository → Testcontainers（PG + AGE）+ Mock-based 单元测试。CI 必须在 PR 上跑 Testcontainers。

> ✅ CI pipeline 已就位：`.github/workflows/ci.yml`（java-backend + mcp-server + gate）。

### Requirement: 安全基线（§6）— token_hash
**原**：未明确 `token_hash` 算法
**新**：MUST 使用 `BCryptPasswordEncoder`（strength=10），明文 token 仅在签发响应中返回一次。

> ✅ 全部完成。含历史 Base64 token 降级兼容（Task 4.4）。

## REMOVED Requirements
无（保留向后兼容路径：Base64 历史 token 在升级窗口内可降级验证，过期后强制 bcrypt——**代码已实现降级** `matchesToken` BCrypt + Base64 fallback）。

## Version History

| 版本 | 日期 | 变更 |
|------|------|------|
| **v1.2** | 2026-06-20 | 闭环 — Task 4.4 降级已实现（`c24ba0b` `matchesToken` BCrypt + Base64 fallback），3 文件同步更新 |
| **v1.1** | 2026-06-13 23:30+ | Review Phase — Tasks 1~5 ✅（Task 4 SubTask 4.4 降级 ❌ 唯一缺口），从 WORKLOG 反向重建 |
| v1.0 | 2026-06-13 21:53 | Review Phase — 初始 Draft（提交 `366972d`） |

## §9 验证清单

### §9.1 Phase 0 冒烟（Phase 0 完成时验证）
- [x] Repository 层：Relation 10 + ObjectInstance 10 + Property 6 单元测试通过 ✅
- [x] GraphService 9 测试通过 ✅
- [x] Testcontainers 15 IT（Relation 4 + ObjectInstance 5 + Property 6）+ CI pipeline 通过 ✅
- [x] BCryptPasswordEncoder 输出 `$2a$10$...` 且 `matches()` 验证成功 ✅ + Base64 降级路径已实现（Task 4.4 ✅）

### §9.2 Phase 1 冒烟（Phase 1 完成时验证）
- [ ] 携带 Agent Token 调用 MCP tool → 鉴权通过
- [ ] 端到端链路：创建本体 → 对象类型 → 属性 → 关系 → 对象实例 → 图遍历 → 重启 → 数据仍在

### §9.3 CI 门禁
- [x] **Task 4.4 降级** → CI 通过（IT 已在跑）→ 合并升版 phase1-spec-v1.md → v1.2 Final
- ✅ CI pipeline（java-backend + mcp-server + gate）已在 `.github/workflows/ci.yml` 就位（提交 `57ee10e`）

## §10 兼容性说明

| 事项 | 说明 |
|------|------|
| DB schema | 不变（沿用 V1 `relation_definition` / `object_instance` / `property_definition` / `agent_token` 等表） |
| REST API 契约 | 不变 |
| 行为变化 | Token 哈希格式（一次性升级，老 token 自然过期或强制重发），其余黑盒兼容 |
| **Base64 降级兼容** | ✅ **已实施（Task 4.4）**——`GovernanceServiceImpl#matchesToken` 已实现 BCrypt + Base64 fallback：`$2a$` 开头 → bcrypt；否则 → SHA-256 + Base64（`legacyHashToken` + `constantTimeEquals` + `log.warn`）。提交 `c24ba0b`。 |
| **剩余 Phase 1 InMemory** | **11 个**入 Phase 1 工单（见 tasks.md 表） |

## 升级路线

```text
Phase 0 (v1.1 → v1.2 Final)         结果                         下一步
    Task 1 ✅ Relation                     │                           │
    Task 2 ✅ ObjectInstance               Phase 0 代码 100% 完成      全部完成
    Task 3 ✅ GraphService                 主体已就位（5 项全绿）       Phase 1 全面投产
    Task 4 ✅ bcrypt + Base64 降级         ✅ SubTask 4.4 已闭环       │
    Task 5 ✅ Testcontainers 5+6+4 + CI    5+6+4 IT 15 用例 + CI        │
                                          → v1.2 Final ✅             │
```
