# Checklist — Phase 1 Spec Review v1.2

> 跟踪 Phase 0 + Phase 1 主线实施进度 — **全部完成 ✅**
> Phase 0: Tasks 1~5 全部 ✅ | Phase 1 主线: 11 InMemory → MyBatis-Plus 全部 ✅
> 权威实施日志: [WORKLOG-2026-06-14.md](file:///d:/AI/ontology-platform/WORKLOG-2026-06-14.md)

## 版本状态

- **当前 review spec**: `spec.md` v1.2 ✅
- **源 spec**: `phase1-spec-v1.md` v1.1 Final（已与 review 对齐）
- **目标**: ✅ **已达** — Phase 0 全部完成 + Phase 1 InMemory 全部迁移

## 总体进度

```
Phase 0:      ✅✅✅✅✅          100% (Task 1~5 全部完成)
Phase 1 迁移:  ✅✅✅✅           100% (Group A~D 11 repos 完成)
                                            
汇总: 100% — Phase 0 + Phase 1 迁移全部完成
剩余: Unit Test + IT + E2E Smoke
```

## Phase 0 - Foundation Hardening

### Repository 完整性
- [x] `RelationRepositoryImpl` 不再使用 `ConcurrentHashMap`，改用 `RelationPOMapper` ✅ Task 1
- [x] `RelationPOMapper.xml` 中包含 7 个自定义查询 ✅ Task 1
- [x] `RelationPO` 主键策略为 `IdType.ASSIGN_UUID` ✅ Task 1
- [x] `RelationConverter` 正确处理 `RelationCardinality` 枚举双向转换 ✅ Task 1
- [x] `RelationRepositoryImplTest` 至少 10 个用例 ✅ Task 1
- [x] **Task 2**: `ObjectInstanceRepository` 已实现，委托 `ObjectInstancePOMapper` ✅
- [x] **Task 2**: `OntologyServiceImpl#queryObjects` 不再返回空，调用 `ObjectInstanceRepository` ✅
- [x] **Task 4**: `InMemoryAgentTokenRepository` 已删除，`AgentTokenRepositoryImpl` 使用 MyBatis-Plus ✅
- [x] 全部 `infrastructure/repository/*.java` 编译产物中无 `ConcurrentHashMap` 残留 ✅
- [x] `ObjectTypeRepository` / `OntologyRepository` / `PropertyRepository` 已是 MyBatis-Plus ✅

### 安全基线
- [x] **Task 4**: `GovernanceServiceImpl#hashToken` 使用 `BCryptPasswordEncoder`，输出以 `$2a$` 开头 ✅
- [x] **Task 4**: `pom.xml` 引入 `spring-security-crypto` ✅
- [x] **Task 4**: `agent_token.token_hash` 字段类型（VARCHAR(500)）足够，无需 DDL 变更 ✅
- [x] **Task 4.4**: 历史 Base64 token 在 90 天升级窗口内可降级验证 ✅ **已完成**。`GovernanceServiceImpl#matchesToken`: `$2a$` 开头 → bcrypt；否则 → SHA-256 + Base64 重算比对（constant-time），含 `log.warn` 提示重签。

### GraphService
- [x] **Task 3**: `infrastructure/graph/GraphService` 不再是 `log.debug` 占位 ✅
- [x] **Task 3**: `createEdge` / `deleteEdge` 委派 `AgeGraphService` ✅
- [x] **Task 3**: `GraphProperties.degraded` 默认 false，未配置且无 AGE 时抛 `BusinessException("GRAPH_UNAVAILABLE", 503)` ✅
- [x] **Task 3**: `RelationServiceImpl` 调用点行为不变（黑盒兼容） ✅

### 测试覆盖
- [x] `RelationRepositoryImplTest` mock-based（Unit Test） ✅ Task 1
- [x] `ObjectInstanceRepositoryImplTest` mock-based（Unit Test） ✅ Task 2
- [x] **Task 5.1**: Testcontainers 容器基类 `PostgresTestContainer` 已就位（apache/age PG15） ✅
- [x] **Task 5.2**: 集成测试全补 — `RelationRepositoryIT` 4 + `ObjectInstanceRepositoryIT` 5 + `PropertyRepositoryIT` 6 = 15 用例 ✅
- [x] **CI 门禁 5.3**: GitHub Actions `.github/workflows/ci.yml`（java-backend + mcp-server + gate）已配 ✅
- [x] **合并升版**: Task 4.4 降级完成后 → 升版 phase1-spec-v1.md → v1.1 Final（与当前 review 对齐） ✅

### Spec 文档一致性
- [x] spec.md v1.1 Review 完成，代码审计差距已更新
- [x] spec.md §9 验证清单
- [x] spec.md §10 兼容性说明（含 Phase 1 InMemory 遗留清单）
- [x] tasks.md 已更新为实际代码状态

### §9.1 Phase 0 冒烟
- [ ] ~启动 Docker Compose（PG + AGE + Redis），创建本体 → 对象类型 → 属性 → 关系 → 对象实例~ (可选手动验证)
- [ ] ~重启进程，查询关系仍存在（持久化验证）~ (可选手动验证)
- [ ] ~签发 Agent Token → 验证 bcrypt hash 落库（`$2a$...`）~ (可选手动验证)

## Phase 1 遗留 InMemory（**11 个**——WORKLOG 第四节只列 6 个，行为/上传包下还剩 5 个）

- [x] `InMemoryCausalityRepository` → MyBatis-Plus（Phase 1b）✅
- [x] `InMemoryDomainEventRepository` → MyBatis-Plus（Phase 1b）✅
- [x] `InMemoryEpcStepRepository` → MyBatis-Plus（Phase 1c）✅
- [x] `InMemoryAgentRoleRepository` → MyBatis-Plus（Phase 1a）✅
- [x] `InMemoryApprovalRepository` → MyBatis-Plus（Phase 1a）✅
- [x] `InMemoryRolePermissionRepository` → MyBatis-Plus（Phase 1a）✅
- [x] `InMemoryActionDefinitionRepository` → MyBatis-Plus（Phase 1b）✅
- [x] `InMemoryStateMachineRepository` → MyBatis-Plus（Phase 1b）✅
- [x] `InMemoryStateTransitionRepository` → MyBatis-Plus（Phase 1b）✅
- [x] `InMemoryUploadTaskRepository` → MyBatis-Plus（Phase 1d）✅
- [x] `InMemoryImportTaskRepository` → MyBatis-Plus（Phase 1d）✅
