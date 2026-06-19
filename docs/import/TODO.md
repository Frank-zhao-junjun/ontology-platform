# 项目2 — TODO

> 最后更新：2026-06-19
> 最近提交：`1444a9e` — import module complete

---

## ✅ 已完成

### Phase 2 — 项目1导入功能

| 状态 | Unit | 说明 | 测试 |
|:----:|:----:|------|:----:|
| ✅ | P2-I01-U01 | Sheet A → Ontology 导入适配器 | 11/11 |
| ✅ | P2-I01-U02 | Sheet B → ObjectType | 8/8 |
| ✅ | P2-I01-U03 | Sheet C → ObjectType | 8/8 |
| ✅ | P2-I01-U04 | Sheet EPC → EPC 步骤解析 | 8/8 |
| ✅ | P2-I01-U05 | Sheet E1~E8 通用维度要素解析 | 11/11 |
| ✅ | P2-I02-U01 | JSON Manifest 格式转换器 | 4/4 |
| ✅ | P2-I03-U01 | YAML OntologyManifest 完整解析器 | 13/13 |
| | | **小计** | **59/59** |

### Phase 3 — V2 交换契约

| 状态 | Task | 说明 |
|:----:|:----:|------|
| ✅ | 3a-0 | golden JSON test fixture |
| ✅ | 3a-1 | OntologyExchangeDocument + OntologyProject VO |
| ✅ | 3a-2 | ManifestUpcasterV1ToV2 |
| ✅ | 3a-3 | ExchangeImportService + exchange_import 表 |
| ✅ | 3a-4 | ExchangeController + 单元测试 |
| ✅ | 3a-5 | E2E 测试 |

### 基础设施

| 状态 | 说明 |
|:----:|------|
| ✅ | V9: 规则约束表 (validation/guardrail/policy/probe) |
| ✅ | V10: 接口+指标表 (api/query/compute/notification/report/indicator) |
| ✅ | V11: exchange_import 表 |
| ✅ | V12: phase3b 表 |
| ✅ | V13: phase3c 表 |
| ✅ | V14: epc_chain 表 |
| ✅ | V1→V9/10/11/12/13/14 全链路 10张新表 Domain Entity/PO/Mapper/Service/DTO/Controller |

---

## 📋 待办

### Phase 3b — 语义层

| 优先级 | 任务 | 说明 |
|:------:|:----:|------|
| P1 | 补全 V12 表代码层 | AgentIntent 的 Service/DTO/Controller 尚未实现 |
| P1 | 补全 V13 表代码层 | 同上 |
| P1 | 补全 V14 表代码层 | EpcChain 的 Service/DTO/Controller 尚未实现 |
| P2 | SemanticService 集成测试 | 当前只有 Controller，缺少完整业务流程 |

### Phase 3c — EPC 链

| 优先级 | 任务 | 说明 |
|:------:|:----:|------|
| P1 | EpcChainService | 完整的 EPC 链业务逻辑 |
| P1 | EpcChainController | 增删改查端点 |
| P2 | EpcChain 导入适配器 | Excel EPC → EpcChain 转换 |

### 质量 & 运维

| 优先级 | 任务 | 说明 |
|:------:|:----:|------|
| P1 | `mvn compile` 全量编译验证 | 确保无编译错误 |
| P1 | 全量测试回归 | `mvn test` 通过 |
| P2 | Swagger API 文档补充 | 新 Controller 的 OpenAPI 注解 |
| P2 | 端到端启动验证 | Docker Compose 拉起 + API 调用 |

### 长期

| 任务 | 说明 |
|:----:|------|
| Cursor agent CLI | 重新安装 |
| ACP 协议集成 | Kimi / Claude 的 ACP 模式接入 Hermes 编排 |
| CI 流水线 | GitHub Actions 自动编译+测试 |
