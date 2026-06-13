# Tasks — Phase 1 Spec Review v1.1

> Phase 0 Foundation Hardening（Phase 1a 前置门）
> 代码审计确认 Tasks 1~4 已完成，Task 5 部分完成（5.1 ✅、5.2 ⚠️ RelationRepositoryIT 已就位 / ObjectInstanceIT+PropertyIT 缺、5.3 ❌）
> Task 4 降级兼容未实现（spec 声明但代码缺 fallback）
> 权威实施日志: [WORKLOG-2026-06-13.md](file:///d:/AI/ontology-platform/WORKLOG-2026-06-13.md)

## Task Dependencies

```text
Phase 0 状态:
  Task 1 (Relation)              ✅ 已完成
  Task 2 (ObjectInstance)        ✅ 已实现
  Task 3 (GraphService)          ✅ 已实现（GRAPH_UNAVAILABLE 503 + degraded 开关）
  Task 4 (Token + bcrypt)        ⚠️ bcrypt 已实现 / Base64 降级未实现
  Task 5 (Testcontainers)        ⚠️ 5.1+5.2 部分 / 5.3 CI 未配
```

**Phase 1a BLOCKING 依赖 Phase 0 全部完成** → 剩 Task 4.4 降级（2%）+ Task 5 全部（5.2 补全 + 5.3 CI = 15%）

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

- [x] **Task 4: Agent Token 哈希 bcrypt + 仓储迁移** ✅ 主体完成，⚠️ Base64 降级未实现
  - [x] SubTask 4.1: 引入 `spring-security-crypto`（`BCryptPasswordEncoder`）
  - [x] SubTask 4.2: `GovernanceServiceImpl#hashToken` 改用 `BCryptPasswordEncoder`（strength=10）
  - [x] SubTask 4.3: `InMemoryAgentTokenRepository` → `AgentTokenRepositoryImpl`（MyBatis-Plus + V6 `agent_token` 表）
  - [ ] SubTask 4.4: ⚠️ 升级窗口兼容（旧 Base64 token 降级验证）— **未实现**，需补 spec 中声明的回退路径

- [ ] **Task 5: Testcontainers 集成测试基线（Integration Test）** ⚠️ 5.1+5.2 部分完成
  - [x] SubTask 5.1: 引入 `testcontainers-postgresql` 依赖（Frank 提交 `33d6c26` 系列完成）
  - [x] SubTask 5.1b: 使用 `postgres:15-age` 镜像覆盖 PG + AGE 需求（`PostgresTestContainer` 已就位）
  - [ ] SubTask 5.2: 集成测试补全 — Relation ✅ (4 用例) / **ObjectInstance ❌** / **Property ❌**
  - [ ] SubTask 5.3: GitHub Actions / CI 配置：在 PR 上跑 Testcontainers — 未配

## Phase 1 主线（已被 phase1-spec-v1.md v1.0 覆盖，不在本次 review 范围）

> Phase 1a~1d 任务清单见 [phase1-spec-v1.md §7](file:///D:/AI/ontology-platform/docs/superpowers/specs/phase1-spec-v1.md)。
> **Phase 0 剩 Task 4 降级 + Task 5 全部（5.2 补全、5.3 CI），完成后即可解锁 Phase 1。**

### 遗留 InMemory（入 Phase 1 工单，**11 个**——WORKLOG 第四节只列 6 个，行为/上传包下还剩 5 个）

| Repository | 归属 Phase | 对应表 | 包路径 |
|-----------|-----------|--------|--------|
| `InMemoryCausalityRepository` | 1b | causality (V4) | `infrastructure.repository` |
| `InMemoryDomainEventRepository` | 1b | domain_event (V4) | `infrastructure.repository` |
| `InMemoryEpcStepRepository` | 1c | epc_step (V5) | `infrastructure.repository` |
| `InMemoryAgentRoleRepository` | 1a | agent_role (V6) | `infrastructure.repository.governance` |
| `InMemoryApprovalRepository` | 1a | approval_request (V6) | `infrastructure.repository.governance` |
| `InMemoryRolePermissionRepository` | 1a | role_permission (V6) | `infrastructure.repository.governance` |
| `InMemoryActionDefinitionRepository` | 1b | action_definition (V3) | `infrastructure.repository.behavior` ⚠️ **WORKLOG 漏列** |
| `InMemoryStateMachineRepository` | 1b | state_machine (V3) | `infrastructure.repository.behavior` ⚠️ **WORKLOG 漏列** |
| `InMemoryStateTransitionRepository` | 1b | state_transition (V3) | `infrastructure.repository.behavior` ⚠️ **WORKLOG 漏列** |
| `InMemoryUploadTaskRepository` | 1d | （无表，in-memory 业务实体） | `infrastructure.repository.upload` ⚠️ **WORKLOG 漏列** |
| `InMemoryImportTaskRepository` | 1d | （无表，in-memory 业务实体） | `infrastructure.repository.upload` ⚠️ **WORKLOG 漏列** |
