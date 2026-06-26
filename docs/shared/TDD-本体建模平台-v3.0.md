# 本体建模平台 TDD v3.0

> 版本：v3.0
> 状态：Draft
> 日期：2026-06-26
> 基于：TDD v2.0 + Phase 2/3 实施反馈 + V2 Import + 校验体系扩展

---

## 1. 测试分层策略

```
Layer 1: 单元测试 ──→ 领域层 + 应用层逻辑（JUnit 5 + Mockito）
Layer 2: 仓储测试 ──→ MyBatis+ 映射（@MybatisPlusTest + H2/Testcontainers）
Layer 3: API 测试 ──→ Controller 端到端（MockMvc + Testcontainers）
Layer 4: MCP 测试 ──→ Tools 逻辑（Vitest，无 Spring 依赖）
Layer 5: E2E 测试 ──→ 完整链路（Spring Boot + MCP Server）
Layer 6: 跨项目 E2E ──→ 项目1 导出 → 项目2 导入 → 验证（6 场景）
```

## 2. 测试覆盖目标

| 层 | 模块 | 用例数 | 覆盖率目标 |
|----|------|:------:|:----------:|
| 单元 | domain 实体 | 20+ | 90%+ |
| 单元 | 应用层 service | 35+ | 85%+ |
| 单元 | Manifest Validator (V01~V11) | 22+ | 95%+ |
| 单元 | V2 Import 管道 | 15+ | 85%+ |
| 单元 | 校验器插件 (VE/VM/VX/V-LC/V-AS) | 30+ | 85%+ |
| 单元 | Semantic Layer | 10+ | 85%+ |
| 仓储 | MyBatis Mapper | 30+ | 80%+ |
| API | Controller | 60+ | 80%+ |
| MCP | Tools | 10+ | 80%+ |
| E2E | 完整链路 | 6+ | — |
| E2E | 跨项目 | 6 | ✅ |

## 3. 详细测试清单

### 3.1 Manifest 导入与校验（对应 US-P01, P02）

| 测试 | 类型 | 说明 |
|------|------|------|
| TC-P01-01 | 单元 | Validator V01: 支持 apiVersion 校验 |
| TC-P01-02 | 单元 | Validator V02: semver 格式校验 |
| TC-P01-03 | 单元 | Validator V03: ≥1 aggregate_root |
| TC-P01-04 | 单元 | Validator V04: entity ref 一致性 |
| TC-P01-05 | 单元 | Validator V05: action ref 一致性 |
| TC-P01-06 | 单元 | Validator V06: event ref 一致性 |
| TC-P01-07 | 单元 | Validator V07: 无明文凭证 |
| TC-P01-08 | 单元 | Validator V08: id 唯一性 |
| TC-P01-09 | 单元 | Validator V09: single initial state |
| TC-P01-10 | 单元 | Validator V10: EPC refs |
| TC-P01-11 | 单元 | Validator V11: no causality cycle |
| TC-P01-12 | 集成 | 完整 Manifest 导入 → 校验通过 |
| TC-P01-13 | 集成 | 损坏 Manifest → 校验拒绝 + 错误列表 |

### 3.2 V2 Import 管道（新增 v3）

| 测试 | 类型 | 说明 |
|------|------|------|
| TC-V2-01 | 单元 | Project1JsonToExchangeConverter: 项目 JSON → ExchangeDocument 映射 |
| TC-V2-02 | 单元 | ExchangePhase3bPublisher: ObjectType+属性持久化 |
| TC-V2-03 | 单元 | ExchangePhase3bPublisher: entityRole 映射（aggregate_root / child_entity） |
| TC-V2-04 | 单元 | ExchangePhase3bPublisher: businessScenarioId 持久化 |
| TC-V2-05 | 单元 | ExchangePhase3bPublisher: EPC 链/节点/边持久化 |
| TC-V2-06 | 单元 | ExchangePhase3bPublisher: Lifecycle snapshot 持久化 |
| TC-V2-07 | 单元 | ExchangePhase3bPublisher: Semantic Layer 持久化 |
| TC-V2-08 | 单元 | ExchangePhase3bPublisher: Organization 持久化（部门/岗位） |
| TC-V2-09 | 单元 | ExchangePhase3bPublisher: Metrics/BusinessTerm 持久化 |
| TC-V2-10 | 单元 | YamlManifestConverter: YAML → ExchangeDocument 映射 |
| TC-V2-11 | 集成 | 完整 v2 JSON 导入 → 校验 → 发布 → 查询闭环 |
| TC-V2-12 | 集成 | BusinessScenario CRUD（新增 V20 表） |
| TC-V2-13 | 集成 | 非法 JSON → 400 + 详细错误 |

### 3.3 校验器插件测试（新增 v3）

| 测试 | 类型 | 说明 |
|------|------|------|
| TC-VE-01~17 | 单元 | VE 插件: EPC 要素一致性校验（17 条） |
| TC-VM-01~39 | 单元 | VM 插件: EPC 覆盖率校验（39 条） |
| TC-VX-01~15 | 单元 | VX 插件: 交叉一致性校验（15 条） |
| TC-VLC-01~15 | 单元 | V-LC 插件: Lifecycle 校验（15 条） |
| TC-VAS-01~15 | 单元 | V-AS 插件: Semantic Layer 校验（15 条） |
| TC-V-ORG-01~XX | 单元 | 组织校验: 部门/岗位 完整性 |
| TC-V-MANIFEST-01~11 | 单元 | Manifest V01~V11 保留校验 |
| TC-V-INTEGRATION-01 | 集成 | 多插件同时运行 → 聚合报告 |
| TC-V-INTEGRATION-02 | 集成 | 空本体 → 空错误列表 |

### 3.4 行为/状态机（对应 US-P04）

| 测试 | 类型 | 说明 |
|------|------|------|
| TC-P04-01 | 单元 | ActionDefinition 实体创建校验 |
| TC-P04-02 | 单元 | StateMachine 初始状态唯一校验 |
| TC-P04-03 | 单元 | StateTransition 可达性校验 |
| TC-P04-04 | API | GET /api/v1/ontologies/{id}/actions |
| TC-P04-05 | API | 按 entityId 过滤行为 |

### 3.5 事件/因果链（对应 US-P05）

| 测试 | 类型 | 说明 |
|------|------|------|
| TC-P05-01 | 单元 | DomainEvent 实体创建 |
| TC-P05-02 | 单元 | Causality 无环校验 |
| TC-P05-03 | API | GET /api/v1/ontologies/{id}/events |

### 3.6 EPC 图结构（新增 v3）

| 测试 | 类型 | 说明 |
|------|------|------|
| TC-EPC-01 | 单元 | EpcChain/Node/Edge/ModelRef 实体创建 |
| TC-EPC-02 | API | GET /api/v1/ontologies/{id}/epc-chains |
| TC-EPC-03 | API | GET /api/v1/ontologies/{id}/epc-nodes |
| TC-EPC-04 | API | GET /api/v1/ontologies/{id}/epc-edges |
| TC-EPC-05 | API | GET /api/v1/ontologies/{id}/epc-model-refs |

### 3.7 Semantic Layer（新增 v3）

| 测试 | 类型 | 说明 |
|------|------|------|
| TC-SEM-01 | 单元 | resolveIntent: 精确匹配 triggerPhrases |
| TC-SEM-02 | 单元 | resolveIntent: 模糊匹配（子串） |
| TC-SEM-03 | 单元 | resolveIntent: 无匹配 → null |
| TC-SEM-04 | API | POST /api/v2/semantic/resolve-intent |
| TC-SEM-05 | 单元 | AgentIntent CRUD |
| TC-SEM-06 | 单元 | SemanticRelation CRUD |

### 3.8 Lifecycle（新增 v3）

| 测试 | 类型 | 说明 |
|------|------|------|
| TC-LC-01 | 单元 | EntityLifecycleSnapshot 实体创建 |
| TC-LC-02 | API | GET /api/v1/ontologies/{id}/lifecycles |
| TC-LC-03 | API | 按 entityId 过滤 |

### 3.9 MCP Tools（对应 US-P07~P11）

| 测试 | 类型 | 说明 |
|------|------|------|
| TC-MCP-01 | 单元 | tools/list 返回固定工具列表 |
| TC-MCP-02 | 单元 | resolve_intent: Semantic Layer 优先 → keywords 回退 |
| TC-MCP-03 | 单元 | resolve_intent: "查询" → QUERY |
| TC-MCP-04 | 单元 | RBAC domain 过滤 |
| TC-MCP-05 | 单元 | RBAC WRITE 拒绝 READER |
| TC-MCP-06 | 单元 | validate_instruction: 不存在 action → error |
| TC-MCP-07 | 单元 | query_ontology: 包含 lifecycle |
| TC-MCP-08 | 单元 | query_ontology: 包含 epcCoverage |

### 3.10 治理（对应 US-P08）

| 测试 | 类型 | 说明 |
|------|------|------|
| TC-P08-01 | API | POST /api/v1/governance/tokens → 签发 |
| TC-P08-02 | API | GET /api/v1/governance/tokens → 列表 |
| TC-P08-03 | API | DELETE /api/v1/governance/tokens/{id} → 吊销 |
| TC-P08-04 | API | 无效 token → 401 |
| TC-P08-05 | 集成 | 审批流: 提交 → PENDING → 审批通过 |

### 3.11 Manifest Round-trip（对应 US-P03, P03b）

| 测试 | 类型 | 说明 |
|------|------|------|
| TC-P03-01 | 集成 | 导入 → preview → publish → export 闭环 |
| TC-P03-02 | 集成 | 导出 YAML → 重新导入 → 内容一致 |
| TC-P03-03 | 集成 | 多个版本导入 → 版本历史可追溯 |

### 3.12 Upload + Import

| 测试 | 类型 | 说明 |
|------|------|------|
| TC-GA-01 | 仓储 | UploadTask CRUD |
| TC-GA-02 | 仓储 | ImportTask CRUD |
| TC-GA-03 | API | 上传 → 创建 UploadTask |
| TC-GA-04 | API | ImportTask 列表查询 |

### 3.13 BusinessScenario（新增 v3）

| 测试 | 类型 | 说明 |
|------|------|------|
| TC-BS-01 | 仓储 | BusinessScenario CRUD（MyBatis） |
| TC-BS-02 | 单元 | create() 工厂方法 |
| TC-BS-03 | API | POST/GET/DELETE BusinessScenario |

### 3.14 跨项目 E2E（v3 扩展）

| 测试 | 类型 | 说明 |
|------|------|------|
| CROSS-1 | E2E | 导入 + 预览（完整 JSON） |
| CROSS-2 | E2E | 发布 |
| CROSS-3 | E2E | 按 Agent 名称导出 |
| CROSS-4 | E2E | 按 ID 精确导出 |
| CROSS-5 | E2E | 最小合法清单 |
| CROSS-6 | E2E | 非法 JSON → 400 |

---

## 4. 测试基础设施

```bash
# 后端全部测试（174+）
mvn test

# V2 Import 测试
mvn test -Dtest="*Exchange*"

# 校验器插件测试
mvn test -Dtest="*Validator*"

# 跨项目 E2E
mvn test -pl ontology-application -am -Dtest="Project1ToProject2E2ETest"

# MCP Server 测试
cd mcp-server && npm test

# Testcontainers IT（需要 Docker）
mvn test -pl ontology-infrastructure -Dtest=*IT
```

## 5. 当前状态

| 层级 | 用例数 | 全部通过 | 覆盖率 |
|------|:------:|:--------:|:------:|
| 单元测试 | 120+ | ✅ | — |
| IT 测试 | 80+ | ✅ | — |
| MCP 测试 | 8+ | ✅ | — |
| 跨项目 E2E | 6 | ✅ | — |
| **合计** | **~215** | **✅** | — |
