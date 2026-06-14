# 本体建模平台 TDD v2.0

> 版本：v2.0
> 状态：Draft
> 日期：2026-06-14
> 基于：US-P01~P11, PRD v2.0, Phase 1 实施反馈

---

## 1. 测试分层策略

```
Layer 1: 单元测试 ──→ 领域层 + 应用层逻辑（JUnit 5 + Mockito）
Layer 2: 仓储测试 ──→ MyBatis+ 映射（@MybatisPlusTest + H2/Testcontainers）
Layer 3: API 测试 ──→ Controller 端到端（MockMvc + Testcontainers）
Layer 4: MCP 测试 ──→ Tools 逻辑（Vitest，无 Spring 依赖）
Layer 5: E2E 测试 ──→ 完整链路（Spring Boot + MCP Server）
```

---

## 2. 测试覆盖目标

| 层 | 模块 | 用例数 | 覆盖率目标 |
|----|------|:------:|:----------:|
| 单元 | domain 实体 | 15+ | 90%+ |
| 单元 | 应用层 service | 25+ | 85%+ |
| 单元 | Manifest Validator (V01~V11) | 22+ | 95%+ |
| 仓储 | MyBatis Mapper | 20+ | 80%+ |
| API | Controller | 40+ | 80%+ |
| MCP | Tools | 6+ | 80%+ |
| E2E | 完整链路 | 3+ | — |

---

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

### 3.2 行为/状态机（对应 US-P04）

| 测试 | 类型 | 说明 |
|------|------|------|
| TC-P04-01 | 单元 | ActionDefinition 实体创建校验 |
| TC-P04-02 | 单元 | StateMachine 初始状态唯一校验 |
| TC-P04-03 | 单元 | StateTransition 可达性校验 |
| TC-P04-04 | API | GET /api/v1/ontologies/{id}/actions |
| TC-P04-05 | API | 按 entityId 过滤行为 |

### 3.3 事件/因果链（对应 US-P05）

| 测试 | 类型 | 说明 |
|------|------|------|
| TC-P05-01 | 单元 | DomainEvent 实体创建 |
| TC-P05-02 | 单元 | Causality 无环校验 |
| TC-P05-03 | API | GET /api/v1/ontologies/{id}/events |

### 3.4 EPC 流程（对应 US-P06）

| 测试 | 类型 | 说明 |
|------|------|------|
| TC-P06-01 | 单元 | EpcStep 实体创建 |
| TC-P06-02 | API | GET /api/v1/ontologies/{id}/epc |
| TC-P06-03 | API | 按 flowName 过滤 |

### 3.5 MCP Tools（对应 US-P07~P11）

| 测试 | 类型 | 说明 |
|------|------|------|
| TC-MCP-01 | 单元 | tools/list 返回固定 4 工具 |
| TC-MCP-02 | 单元 | resolve_intent: "查询" → QUERY |
| TC-MCP-03 | 单元 | resolve_intent: "创建" → CREATE |
| TC-MCP-04 | 单元 | RBAC domain 过滤 |
| TC-MCP-05 | 单元 | RBAC WRITE 拒绝 READER |
| TC-MCP-06 | 单元 | validate_instruction: 不存在 action → error |

### 3.6 治理（对应 US-P08）

| 测试 | 类型 | 说明 |
|------|------|------|
| TC-P08-01 | API | POST /api/v1/governance/tokens → 签发 |
| TC-P08-02 | API | GET /api/v1/governance/tokens → 列表 |
| TC-P08-03 | API | DELETE /api/v1/governance/tokens/{id} → 吊销 |
| TC-P08-04 | API | 无效 token → 401 |
| TC-P08-05 | 集成 | 审批流: 提交 → PENDING → 审批通过 |

### 3.7 Manifest Round-trip（对应 US-P03, P03b）

| 测试 | 类型 | 说明 |
|------|------|------|
| TC-P03-01 | 集成 | 导入 → preview → publish → export 闭环 |
| TC-P03-02 | 集成 | 导出 YAML → 重新导入 → 内容一致 |
| TC-P03-03 | 集成 | 多个版本导入 → 版本历史可追溯 |

### 3.8 Upload + Import（Phase 1 G-A）

| 测试 | 类型 | 说明 |
|------|------|------|
| TC-GA-01 | 仓储 | UploadTask CRUD |
| TC-GA-02 | 仓储 | ImportTask CRUD |
| TC-GA-03 | API | 上传 → 创建 UploadTask |
| TC-GA-04 | API | ImportTask 列表查询 |

---

## 4. 测试基础设施

```bash
# 后端全部测试
mvn test

# 指定模块
mvn test -pl ontology-api -Dtest=ManifestControllerTest

# MCP Server 测试
cd mcp-server && npm test

# Testcontainers IT（需要 Docker）
mvn test -pl ontology-infrastructure -Dtest=*IT

# E2E（需要 Spring Boot + MCP Server 同时运行）
# 手动执行 smoke.test.ts 指向真实端点
```

## 5. 当前状态

| 层级 | 用例数 | 全部通过 | 覆盖率 |
|------|:------:|:--------:|:------:|
| 单元测试 | 106 | ✅ | — |
| IT 测试 | 73 (11 files) | ✅ | — |
| MCP 测试 | 6 | ✅ | — |
| **合计** | **185** | **✅** | — |
