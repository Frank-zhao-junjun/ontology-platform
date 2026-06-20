# Checklist — Phase 1 Spec Review v1.1

> 跟踪 Phase 0 Foundation Hardening 实施进度
> WORKLOG 同步（2026-06-20）：Tasks 1~5 ✅ 全部完成（含 Task 4.4 Base64 降级，提交 `c24ba0b`）；**Task 5 ✅**（ObjectInstanceIT 5 + PropertyIT 6 + RelationIT 4 + CI pipeline，提交 `57ee10e`）
> 权威实施日志: [WORKLOG-2026-06-14.md](file:///d:/AI/ontology-platform/WORKLOG-2026-06-14.md)
> 本文件由 WORKLOG 反向重建（2026-06-13 代码审计就绪版；2026-06-20 更新 Task 4.4 闭环）

## 版本状态

- **当前 review spec**: `spec.md` v1.1 (Review)
- **源 spec**: `phase1-spec-v1.md` v1.1 Final（提交 `366972d` 21:53）
- **目标**: ✅ Task 4.4 降级已完成 → 升版当前 review 到 v1.2 Final（与源 spec 对齐）

## 总体进度

```
Tasks 1~3:    ✅✅✅            70%
Task 4:       ✅  bcrypt + Base64 降级已完成 (SubTask 4.4 ✅)
Task 5:       ✅  Testcontainers 15 IT + CI ✅ 已完成

汇总: 70% + 15% + 15% = 100% (按 bucket)
      完成度: 100% (Phase 0 全部完成)
```

## Phase 0 - Foundation Hardening

### Repository 完整性
- [x] **Task 1**: `RelationRepositoryImpl` 不再使用 `ConcurrentHashMap`，改用 `RelationPOMapper` ✅
- [x] **Task 1**: `RelationPOMapper.xml` 中包含 7 个自定义查询（selectByXxx / countByXxx） ✅
- [x] **Task 1**: `RelationPO` 主键策略为 `IdType.ASSIGN_UUID`（与 schema `gen_random_uuid()` 对齐） ✅
- [x] **Task 1**: `RelationConverter` 正确处理 `RelationCardinality` 枚举双向转换 ✅
- [x] **Task 1**: `RelationRepositoryImplTest` 至少 10 个用例，CRUD + 存在性 + 计数全覆盖 ✅
- [x] **Task 2**: `ObjectInstanceRepository` 已实现，委托 `ObjectInstancePOMapper` ✅
- [x] **Task 2**: `OntologyServiceImpl#queryObjects` 不再返回空，调用 `ObjectInstanceRepository` ✅
- [x] **Task 4**: `InMemoryAgentTokenRepository` 被 `AgentTokenRepositoryImpl`（MyBatis-Plus）替换 ✅
- [x] 全部 `infrastructure/repository/*.java` 编译产物中无 `java.util.concurrent.ConcurrentHashMap` 残留（除 `MyBatisPlusConfig` 等基础设施外）✅

### 安全基线
- [x] **Task 4**: `GovernanceServiceImpl#hashToken` 使用 `BCryptPasswordEncoder`，输出以 `$2a$` 开头 ✅
- [x] **Task 4**: `pom.xml` 引入 `spring-security-crypto` ✅
- [x] **Task 4**: `agent_token.token_hash` 字段类型（VARCHAR(500)）足够，无需 DDL 变更 ✅
- [x] **Task 4**: 历史 Base64 token 在 90 天升级窗口内可降级验证 — ✅ **已实现（SubTask 4.4）**。`GovernanceServiceImpl#matchesToken` 已实现 BCrypt + Base64 fallback：`$2a$` 开头 → bcrypt；否则 → SHA-256 + Base64 重算比对（`legacyHashToken` + `constantTimeEquals`）。

### GraphService
- [x] **Task 3**: `infrastructure/graph/GraphService` 不再是 `log.debug` 占位 ✅
- [x] **Task 3**: `createEdge` / `deleteEdge` 委派 `AgeGraphService` ✅
- [x] **Task 3**: `GraphProperties.degraded` 默认 false，未配置且无 AGE 时抛 `BusinessException("GRAPH_UNAVAILABLE", 503)` ✅
- [x] **Task 3**: `RelationServiceImpl` 调用点行为不变（黑盒兼容）✅

### 测试覆盖
- [x] `RelationRepositoryImplTest` mock-based（Unit Test） ✅ Task 1
- [x] `ObjectInstanceRepositoryImplTest` mock-based（Unit Test） ✅ Task 2
- [x] `GraphServiceTest` 9 个用例（提交 `67d0f75` 适配）✅ Task 3
- [x] **Task 5.1**: Testcontainers 容器基类 `PostgresTestContainer` 已就位（apache/age PG15） ✅
- [x] **Task 5.2**: 集成测试全补 — `RelationRepositoryIT` 4 + `ObjectInstanceRepositoryIT` 5 + `PropertyRepositoryIT` 6 = 15 用例 ✅
- [x] **CI 门禁 5.3**: GitHub Actions `.github/workflows/ci.yml`（java-backend + mcp-server + gate）已配 ✅
- [x] **合并升版**: ✅ Task 4.4 降级完成 → 升版 phase1-spec-v1.md → v1.2 Final（与当前 review 对齐）

### Spec 文档一致性
- [x] spec.md v1.1 Review 完成，代码审计差距已更新
- [x] spec.md §9 验证清单与 §10 兼容性说明已同步
- [x] tasks.md Task 1~5 + SubTask 5.1~5.3 全部勾选
- [x] checklist.md Repository/安全/GraphService/IT+CI 全部 ✅，Task 4.4 已闭环
- [x] 本 review 与 [WORKLOG-2026-06-14.md](file:///d:/AI/ontology-platform/WORKLOG-2026-06-14.md) 完全一致

### 端到端冒烟（Phase 1d 待完善）
- [ ] 启动 Docker Compose（PG + AGE + Redis）后，跑完整链路：创建本体 → 对象类型 → 属性 → 关系 → 对象实例 → 图遍历 → 重启 → 数据仍在
- [ ] 签发 Agent Token → 验证 bcrypt hash 落库 → 携带 token 调用 MCP tool → 鉴权通过

## Phase 1 遗留 InMemory（**11 个**——含 behavior + upload 包）

- [ ] `InMemoryCausalityRepository` → MyBatis-Plus（Phase 1b）
- [ ] `InMemoryDomainEventRepository` → MyBatis-Plus（Phase 1b）
- [ ] `InMemoryEpcStepRepository` → MyBatis-Plus（Phase 1c）
- [ ] `InMemoryActionDefinitionRepository` → MyBatis-Plus（Phase 1b）
- [ ] `InMemoryStateMachineRepository` → MyBatis-Plus（Phase 1b）
- [ ] `InMemoryStateTransitionRepository` → MyBatis-Plus（Phase 1b）
- [ ] `InMemoryAgentRoleRepository` → MyBatis-Plus（Phase 1a）
- [ ] `InMemoryApprovalRepository` → MyBatis-Plus（Phase 1a）
- [ ] `InMemoryRolePermissionRepository` → MyBatis-Plus（Phase 1a）
- [ ] `InMemoryUploadTaskRepository` → MyBatis-Plus（Phase 1d）
- [ ] `InMemoryImportTaskRepository` → MyBatis-Plus（Phase 1d）
