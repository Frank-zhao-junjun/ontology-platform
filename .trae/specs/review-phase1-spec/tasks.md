# Tasks — Phase 1 Spec Review v1.2

> Phase 0 Foundation Hardening — **全部完成 ✅**
> Phase 1 主线 InMemory 迁移 — **全部完成 ✅**（11 repos → MyBatis-Plus + V7 DDL）
> WORKLOG 同步：Phase 0 (Task 1~5) + Phase 1 迁移 (Group A~D) 全部 ✅
> 权威实施日志: [WORKLOG-2026-06-14.md](file:///d:/AI/ontology-platform/WORKLOG-2026-06-14.md)

## Task Dependencies

```text
Phase 0 状态:
  Task 1 (Relation)              ✅ 已完成
  Task 2 (ObjectInstance)        ✅ 已实现
  Task 3 (GraphService)          ✅ 已实现（GRAPH_UNAVAILABLE 503 + degraded 开关）
  Task 4 (Token + bcrypt + Base64)  ✅ 全部完成（bcrypt + 仓储迁移 + SubTask 4.4 Base64 降级）
  Task 5 (Testcontainers)        ✅ 已完成（IT 5+6+4 + CI）
```

**Phase 1a BLOCKING 依赖 Phase 0 全部完成** → ✅ **Phase 0 100% 完成**

---

- [x] **Task 1: Relation 仓储迁移到 MyBatis-Plus** ✅ 已完成
  - [x] SubTask 1.1: 新增 `RelationPO` 持久化对象（MyBatis-Plus 注解）
  - [x] SubTask 1.2: 新增 `RelationPOMapper` 接口 + `RelationPOMapper.xml`
  - [x] SubTask 1.3: 新增 `RelationConverter`（PO ↔ Entity）
  - [x] SubTask 1.4: 重写 `RelationRepositoryImpl` 委托 mapper，移除 `ConcurrentHashMap`
  - [x] SubTask 1.5: 新增 `RelationRepositoryImplTest`（10 个用例覆盖 CRUD + exists/count）
  - [x] SubTask 1.6: `mvn -pl ontology-infrastructure test` 全绿

- [x] **Task 2: object_instance 仓储实现 + queryObjects 接通真实数据** ✅ 代码已实现
  - [x] SubTask 2.1: 新增 `ObjectInstancePO` / `ObjectInstancePOMapper` / `ObjectInstanceRepository`（interface + impl）
  - [x] SubTask 2.2: 接入 `ObjectInstanceRepository` 到 `OntologyServiceImpl#queryObjects`，实现分页 + coreData/extendedData 合并
  - [x] SubTask 2.3: 新增 `ObjectInstanceRepositoryImplTest`（mock-based）
  - [x] SubTask 2.4: 端到端逻辑就绪

- [x] **Task 3: GraphService 占位替换为 AgeGraphService 委派** ✅ 代码已实现
  - [x] SubTask 3.1: 引入 `GraphProperties` 配置项 `graph.degraded`（默认 false）
  - [x] SubTask 3.2: `GraphService.createEdge/deleteEdge` 委派 `AgeGraphService`，新旧签名兼容
  - [x] SubTask 3.3: `BusinessException("GRAPH_UNAVAILABLE", 503)` 错误码
  - [x] SubTask 3.4: `RelationServiceImpl` 调用点回归测试

- [x] **Task 4: Agent Token 哈希 bcrypt + 仓储迁移 + Base64 降级** ✅ 全部完成
  - [x] SubTask 4.1: 引入 `spring-security-crypto`（`BCryptPasswordEncoder`）
  - [x] SubTask 4.2: `GovernanceServiceImpl#hashToken` 改用 `BCryptPasswordEncoder`（strength=10）
  - [x] SubTask 4.3: `InMemoryAgentTokenRepository` → `AgentTokenRepositoryImpl`（MyBatis-Plus + V6 `agent_token` 表）
  - [x] SubTask 4.4: Base64 降级兼容 ✅ 已完成 — `matchesToken` 以 `$2a$` 开头 → bcrypt；否则 → SHA-256 + Base64 重算比对（constant-time）+ GovernanceServiceImplTest 13 用例

- [x] **Task 5: Testcontainers 集成测试基线（Integration Test）** ✅ 全部完成
  - [x] SubTask 5.1: 引入 `testcontainers-postgresql` 依赖（Frank 提交 `33d6c26` 系列完成）
  - [x] SubTask 5.1b: 使用 `postgres:15-age` 镜像覆盖 PG + AGE 需求（`PostgresTestContainer` 已就位）
  - [x] SubTask 5.2: 集成测试全补 — RelationRepositoryIT ✅ (4) + ObjectInstanceRepositoryIT ✅ (5) + PropertyRepositoryIT ✅ (6) = 15 用例
  - [x] SubTask 5.3: GitHub Actions CI 配置 `.github/workflows/ci.yml`（java-backend + mcp-server + gate）已配 ✅

## Phase 1 主线 — InMemory → MyBatis-Plus 迁移 ✅ 全部完成

> Phase 1a~1d 任务清单见 [phase1-spec-v1.md §7](file:///d:/AI/ontology-platform/docs/superpowers/specs/phase1-spec-v1.md)。
> **Phase 0 + Phase 1 迁移 100% 完成** ✅

### 11 InMemory → MyBatis-Plus（全部迁移完成 ✅）

| # | 原 InMemory Repository | 新 RepositoryImpl | 表 | 组 |
|---|------------------------|-------------------|-----|:--:|
| 1 | `InMemoryUploadTaskRepository` | `UploadTaskRepositoryImpl` | upload_task (V7) | A |
| 2 | `InMemoryImportTaskRepository` | `ImportTaskRepositoryImpl` | import_task (V7) | A |
| 3 | `InMemoryAgentRoleRepository` | `AgentRoleRepositoryImpl` | agent_role (V6) | B |
| 4 | `InMemoryApprovalRepository` | `ApprovalRepositoryImpl` | approval_request (V6) | B |
| 5 | `InMemoryRolePermissionRepository` | `RolePermissionRepositoryImpl` | role_permission (V6) | B |
| 6 | `InMemoryActionDefinitionRepository` | `ActionDefinitionRepositoryImpl` | action_definition (V3) | C |
| 7 | `InMemoryStateMachineRepository` | `StateMachineRepositoryImpl` | state_machine (V3) | C |
| 8 | `InMemoryStateTransitionRepository` | `StateTransitionRepositoryImpl` | state_transition (V3) | C |
| 9 | `InMemoryDomainEventRepository` | `DomainEventRepositoryImpl` | domain_event (V4) | D |
| 10 | `InMemoryCausalityRepository` | `CausalityRepositoryImpl` | causality (V4) | D |
| 11 | `InMemoryEpcStepRepository` | `EpcStepRepositoryImpl` | epc_step (V5) | D |

**新增文件**: 1 Flyway (V7) + 11 PO + 11 Mapper + 9 XML + 11 Converter + 11 Impl = **54 文件**
**删除文件**: 11 InMemory implementations
**验证**: `mvn compile` 全绿 ✅ / 零 InMemory 残留 / 零 ConcurrentHashMap

### 剩余 Phase 1 工作（待实施）

| 优先级 | 任务 | 内容 |
|:------:|------|------|
| P1 | Repository Unit Tests | 11 个新 Impl 各 ≥5 用例（mock Mapper 边界） |
| P2 | Repository Integration Tests | Testcontainers 覆盖 V3~V7 新表（每表 ≥3 用例） |
| P3 | Phase 1d E2E Smoke | 全链路：Docker Compose → Import → MCP tools/call |
| P4 | §9.1 冒烟测试 | 持久化验证 + bcrypt 验证 + API 回归 |

### Phase 2 前瞻

| 文档 | 版本 | 路径 |
|------|:----:|------|
| Phase 2 Spec | v1.0 | [phase2-spec-v1.md](file:///d:/AI/ontology-platform/docs/superpowers/specs/phase2-spec-v1.md) |
| 覆盖范围 | 6 US | F01 异步, F02 幂等, F03 可观测, F04 版本, F05 限流, mTLS |
| 实施顺序 | 2a→2b→2c | 幂等+TraceId → 任务队列+Webhook+限流 → Metrics+版本化+mTLS |

### 全部 17 个 MyBatis-Plus Repository

| 已有 (Phase 0 前) | Phase 0 迁移 | Phase 1 迁移 |
|-------------------|-------------|-------------|
| ObjectTypeRepositoryImpl | RelationRepositoryImpl | UploadTaskRepositoryImpl |
| OntologyRepositoryImpl | ObjectInstanceRepositoryImpl | ImportTaskRepositoryImpl |
| PropertyRepositoryImpl | AgentTokenRepositoryImpl | AgentRoleRepositoryImpl |
| | | ApprovalRepositoryImpl |
| | | RolePermissionRepositoryImpl |
| | | ActionDefinitionRepositoryImpl |
| | | StateMachineRepositoryImpl |
| | | StateTransitionRepositoryImpl |
| | | DomainEventRepositoryImpl |
| | | CausalityRepositoryImpl |
| | | EpcStepRepositoryImpl |
| **3 个** | **3 个** | **11 个** |
