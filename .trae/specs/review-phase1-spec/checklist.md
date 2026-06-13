# Checklist — Phase 1 Spec Review v1.1

> 跟踪 Phase 0 Foundation Hardening 实施进度
> 代码审计 + WORKLOG 交叉确认: Tasks 1~3 完成；Task 4 主体完成但 Base64 降级未实现；Task 5 5.1+5.2 部分（仅 RelationRepositoryIT）
> 权威实施日志: [WORKLOG-2026-06-13.md](file:///d:/AI/ontology-platform/WORKLOG-2026-06-13.md)

## 版本状态

- **当前 review spec**: `spec.md` v1.1 (Review)
- **源 spec**: `phase1-spec-v1.md` v1.0 (Draft) — WORKLOG 提交 `366972d` 已合并 v1.1 Final (21:53)，含 8 项修复
- **目标**: Task 4 降级 + Task 5 全部完成后 → 升版当前 review 到 v1.1 Final

## 总体进度

```
Tasks 1~3:    ✅✅✅            70%
Task 4:       ⚠️  13% bcrypt 主体完成     → 2% Base64 降级（SubTask 4.4）❌
Task 5:       ⚠️  5.1+5.2(Rel) 15% / 5.2(ObjectInstance+Property) ❌ / 5.3 CI ❌
                                            
汇总: 70% + 15% + 15% = 100% (按 bucket)
      完成度: ~85% (Task 4.4 + Task 5 全部仍缺)
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
- [ ] **Task 4**: 历史 Base64 token 在 90 天升级窗口内可降级验证 — ❌ **未实现（Task 4.4）**。`GovernanceServiceImpl#matchesToken` 仅走 `BCryptPasswordEncoder.matches()`，无 Base64 fallback。需补：以 `$2a$` 开头 → bcrypt，否则视为历史 Base64（SHA-256 + Base64）重算比对。⚠️

### GraphService
- [x] **Task 3**: `infrastructure/graph/GraphService` 不再是 `log.debug` 占位 ✅
- [x] **Task 3**: `createEdge` / `deleteEdge` 委派 `AgeGraphService` ✅
- [x] **Task 3**: `GraphProperties.degraded` 默认 false，未配置且无 AGE 时抛 `BusinessException("GRAPH_UNAVAILABLE", 503)` ✅
- [x] **Task 3**: `RelationServiceImpl` 调用点行为不变（黑盒兼容） ✅

### 测试覆盖
- [x] `RelationRepositoryImplTest` mock-based（Unit Test） ✅ Task 1
- [x] **Task 2**: `ObjectInstanceRepositoryImplTest` 已就绪（Unit Test） ✅
- [x] **Task 5.1**: Testcontainers 容器基类 `PostgresTestContainer` 已就位（apache/age PG15） ✅
- [x] **Task 5.2-Relation**: `RelationRepositoryIT` 4 个集成用例已跑通 ✅
- [ ] **Task 5.2-ObjectInstance**: `ObjectInstanceRepositoryIT` 集成测试 ❌
- [ ] **Task 5.2-Property**: `PropertyRepositoryIT` 集成测试 ❌
- [ ] **CI 门禁 5.3**: GitHub Actions 配置在 PR 上跑 Testcontainers ❌
- [ ] **合并升版**: Task 4 降级 + Task 5 全部完成 + CI 通过 → 升版 phase1-spec-v1.md → v1.1 Final（WORKLOG `366972d` 之外） ❌

### Spec 文档一致性
- [x] spec.md v1.1 Review 完成，代码审计差距已更新
- [x] spec.md §9 验证清单
- [x] spec.md §10 兼容性说明（含 Phase 1 InMemory 遗留清单）
- [x] tasks.md 已更新为实际代码状态

### §9.1 Phase 0 冒烟
- [ ] 启动 Docker Compose（PG + AGE + Redis），创建本体 → 对象类型 → 属性 → 关系 → 对象实例
- [ ] 重启进程，查询关系仍存在（持久化验证）
- [ ] 签发 Agent Token → 验证 bcrypt hash 落库（`$2a$...`）

## Phase 1 遗留 InMemory（**11 个**——WORKLOG 第四节只列 6 个，行为/上传包下还剩 5 个）

- [ ] `InMemoryCausalityRepository` → MyBatis-Plus（Phase 1b）
- [ ] `InMemoryDomainEventRepository` → MyBatis-Plus（Phase 1b）
- [ ] `InMemoryEpcStepRepository` → MyBatis-Plus（Phase 1c）
- [ ] `InMemoryAgentRoleRepository` → MyBatis-Plus（Phase 1a）
- [ ] `InMemoryApprovalRepository` → MyBatis-Plus（Phase 1a）
- [ ] `InMemoryRolePermissionRepository` → MyBatis-Plus（Phase 1a）
- [ ] `InMemoryActionDefinitionRepository` → MyBatis-Plus（Phase 1b）⚠️ WORKLOG 漏列
- [ ] `InMemoryStateMachineRepository` → MyBatis-Plus（Phase 1b）⚠️ WORKLOG 漏列
- [ ] `InMemoryStateTransitionRepository` → MyBatis-Plus（Phase 1b）⚠️ WORKLOG 漏列
- [ ] `InMemoryUploadTaskRepository` → MyBatis-Plus（Phase 1d）⚠️ WORKLOG 漏列
- [ ] `InMemoryImportTaskRepository` → MyBatis-Plus（Phase 1d）⚠️ WORKLOG 漏列
