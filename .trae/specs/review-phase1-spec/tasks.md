# Tasks — Phase 1 Spec Review v1.1

> Phase 0 Foundation Hardening（Phase 1a 前置门）
> WORKLOG 同步：Tasks 1~5 ✅ 全部完成（含 Task 4.4 Base64 降级，提交 `c24ba0b`）；**Task 5 ✅**（ObjectInstanceIT 5 + PropertyIT 6 + RelationIT 4 + CI pipeline，提交 `57ee10e`）
> Task 4.4 降级兼容已完成 — `GovernanceServiceImpl#matchesToken` 已实现 BCrypt + Base64 fallback + constantTimeEquals
> 权威实施日志: [WORKLOG-2026-06-14.md](file:///d:/AI/ontology-platform/WORKLOG-2026-06-14.md)
> 本文件由 WORKLOG 反向重建（2026-06-13 23:30+ 代码审计就绪版；2026-06-20 更新 Task 4.4 闭环）

## Task Dependencies

```text
Phase 0 状态:
  Task 1 (Relation)              ✅ 已完成 (10 单元测试)
  Task 2 (ObjectInstance)        ✅ 已实现 (10 单元测试 + 5 IT)
  Task 3 (GraphService)          ✅ 已实现 (9 测试 + GRAPH_UNAVAILABLE 503 + degraded 开关)
  Task 4 (Token + bcrypt)        ✅ bcrypt + Base64 降级已完成 (SubTask 4.4 ✅)
  Task 5 (Testcontainers)        ✅ 已完成 (IT 5+6+4 + CI pipeline)
```

**Phase 1a BLOCKING 依赖 Phase 0 全部完成** → ✅ Phase 0 100% 就绪

---

## Phase 0 - Foundation Hardening（代码审计就绪版）

- [x] **Task 1: Relation 仓储迁移到 MyBatis-Plus** ✅ 已完成
  - [x] SubTask 1.1: 新增 `RelationPO` 持久化对象（MyBatis-Plus 注解）
  - [x] SubTask 1.2: 新增 `RelationPOMapper` 接口 + `RelationPOMapper.xml`
  - [x] SubTask 1.3: 新增 `RelationConverter`（PO ↔ Entity）
  - [x] SubTask 1.4: 重写 `RelationRepositoryImpl` 委托 mapper，移除 `ConcurrentHashMap`
  - [x] SubTask 1.5: 新增 `RelationRepositoryImplTest`（10 个用例覆盖 CRUD + exists/count）
  - [x] SubTask 1.6: `mvn -pl ontology-infrastructure test` 全绿
  - [x] SubTask 1.7: `RelationRepositoryIT` Testcontainers 集成测试 4 用例（提交 `57ee10e`）

- [x] **Task 2: object_instance 仓储实现 + queryObjects 接通真实数据** ✅ 已实现
  - [x] SubTask 2.1: 新增 `ObjectInstancePO` / `ObjectInstancePOMapper` / `ObjectInstanceRepository`（interface + impl）
  - [x] SubTask 2.2: 接入 `ObjectInstanceRepository` 到 `OntologyServiceImpl#queryObjects`，实现分页 + 过滤 + coreData/extendedData 合并
  - [x] SubTask 2.3: 新增 `ObjectInstanceRepositoryImplTest`（10 mock-based 用例）
  - [x] SubTask 2.4: `ObjectInstanceRepositoryIT` Testcontainers 集成测试 5 用例（提交 `57ee10e`）

- [x] **Task 3: GraphService 占位替换为 AgeGraphService 委派** ✅ 已实现
  - [x] SubTask 3.1: 引入 `GraphProperties` 配置项 `graph.degraded`（默认 false）
  - [x] SubTask 3.2: `GraphService.createEdge/deleteEdge` 委派 `AgeGraphService`
  - [x] SubTask 3.3: `BusinessException("GRAPH_UNAVAILABLE", 503)` 错误码（ErrorCode 8005，提交 `33d6c26`）
  - [x] SubTask 3.4: `RelationServiceImpl` 调用点回归测试（黑盒兼容）
  - [x] SubTask 3.5: `GraphServiceTest` 9 个用例全通过（提交 `67d0f75` 适配降级模式测试）

- [x] **Task 4: Agent Token 哈希 bcrypt + 仓储迁移** ✅ 全部完成（含 Base64 降级）
  - [x] SubTask 4.1: 引入 `spring-security-crypto`（`BCryptPasswordEncoder`）
  - [x] SubTask 4.2: `GovernanceServiceImpl#hashToken` 改用 `BCryptPasswordEncoder`（strength=10）
  - [x] SubTask 4.3: `InMemoryAgentTokenRepository` → `AgentTokenRepositoryImpl`（MyBatis-Plus + V6 `agent_token` 表）
  - [x] SubTask 4.4: ✅ 升级窗口兼容（旧 Base64 token 降级验证）— `matchesToken` 已实现 BCrypt + Base64 fallback + `legacyHashToken` + `constantTimeEquals`

- [x] **Task 5: Testcontainers 集成测试基线（Integration Test）** ✅ 已完成（提交 `57ee10e`）
  - [x] SubTask 5.1: 引入 `testcontainers-postgresql` 依赖
  - [x] SubTask 5.1b: 使用 `postgres:15-age` 镜像覆盖 PG + AGE 需求
  - [x] SubTask 5.2: 集成测试全补 — Relation 4 用例 + ObjectInstance 5 用例 + Property 6 用例（合计 15 用例）
  - [x] SubTask 5.3: GitHub Actions / CI 配置（`.github/workflows/ci.yml`：java-backend + mcp-server + gate）

## Phase 1 主线（已被 phase1-spec-v1.md v1.1 Final 覆盖，WORKLOG §一）

> Phase 1a~1d 任务清单见 [phase1-spec-v1.md](file:///D:/AI/ontology-platform/docs/superpowers/specs/phase1-spec-v1.md) §7 + [WORKLOG-2026-06-13.md](file:///d:/AI/ontology-platform/WORKLOG-2026-06-13.md) §二。
> **Phase 0 全部完成，Phase 1 全面投产已解锁。**

| Phase | 状态 | 提交 | 新增文件 | 备注 |
|-------|:----:|------|----------|------|
| Phase 0 (Foundation Hardening) | ✅ 100% | 3+2 | 6 | 全部完成（含 Task 4.4 降级） |
| Phase 1a (Flyway + Governance + Manifest) | ✅ 100% | 2 | 7 | 提交 `8e85e9d` (V2-V6 DDL) + `76f67da` (Manifest) + `5b3e83a` (ValidatorTest) |
| Phase 1b (Domain Extensions) | ✅ 100% | 2 | 29 | 提交 `00b358d` (6 实体+Repo) + `cb164bf` (Service+Controller+DTO) |
| Phase 1c (MCP Server) | ✅ 100% | 1 | 17 | 提交 `5951881`（Express + 5 工具 + JWT + RBAC，1,519 行 TS） |
| Phase 1d (E2E) | ⚠️ 待完善 | 1 | 1 | 端到端全链路冒烟（启动 Docker Compose → manufacturing-manifest.yaml → MCP tools/call） |

### 遗留 InMemory（入 Phase 1 工单，**11 个**——含 behavior + upload 包）

| Repository | 归属 Phase | 对应表 | 包路径 |
|-----------|-----------|--------|--------|
| `InMemoryCausalityRepository` | 1b | causality (V4) | `infrastructure.repository` |
| `InMemoryDomainEventRepository` | 1b | domain_event (V4) | `infrastructure.repository` |
| `InMemoryEpcStepRepository` | 1c | epc_step (V5) | `infrastructure.repository` |
| `InMemoryActionDefinitionRepository` | 1b | action_definition (V3) | `infrastructure.repository.behavior` |
| `InMemoryStateMachineRepository` | 1b | state_machine (V3) | `infrastructure.repository.behavior` |
| `InMemoryStateTransitionRepository` | 1b | state_transition (V3) | `infrastructure.repository.behavior` |
| `InMemoryAgentRoleRepository` | 1a | agent_role (V6) | `infrastructure.repository.governance` |
| `InMemoryApprovalRepository` | 1a | approval_request (V6) | `infrastructure.repository.governance` |
| `InMemoryRolePermissionRepository` | 1a | role_permission (V6) | `infrastructure.repository.governance` |
| `InMemoryUploadTaskRepository` | 1d | （无表，业务实体） | `infrastructure.repository.upload` |
| `InMemoryImportTaskRepository` | 1d | （无表，业务实体） | `infrastructure.repository.upload` |
