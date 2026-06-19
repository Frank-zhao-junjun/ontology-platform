# 项目2 — TODO

> 最后更新：2026-06-19
> 最新合并：`main ← 2401b43`

---

## ✅ 已完成

### Phase 2 — 项目1导入功能

| 状态 | Unit | 说明 | 测试 |
|:----:|:----:|------|:----:|
| ✅ | P2-I01-U01~U05 | Excel Sheets A~E + EPC 导入适配器 | 46/46 |
| ✅ | P2-I02-U01 | JSON Manifest 转换器 | 4/4 |
| ✅ | P2-I03-U01 | YAML Manifest 解析器 | 13/13 |
| ✅ | V9+V10 | 规则约束表 + 接口指标表（10张新表全链路代码） | — |
| ✅ | V11 | exchange_import 表 + ExchangeImportService | — |

### Phase 3 — V2 交换契约 + 语义 + EPC

| 状态 | 阶段 | 内容 | 文件数 |
|:----:|:----:|------|:-----:|
| ✅ | 3a V2核心管道 | OntologyExchangeDocument + ManifestUpcaster + ExchangeController | ~30 |
| ✅ | 3b V12 | 8表: Department/Position/BusinessMetric/Orchestration/ProcessStep/MetadataTemplate/BusinessTerm/AgentIntent | ~64 |
| ✅ | 3c V13 | 6表: SemanticRelation/IntentSlot/AgentPolicySemantic/ErrorRecovery/SemanticFieldMapping/EntityLifecycleSnapshot | ~48 |
| ✅ | 3d V14 | 5表: EpcChain/EpcNode/EpcEdge/EpcModelRef/EpcProfile | ~40 |

### 质量

| 状态 | 内容 | 统计 |
|:----:|------|:----:|
| ✅ | Service 单测 | 111 tests, 0 failures |
| ✅ | Controller 集成测试 | 19 个测试文件 |
| ✅ | mvn compile | BUILD SUCCESS |
| ✅ | 合并 main | 已推送 |

---

## 📋 待办

### P1 — 短期（1天内）

| # | 任务 | 说明 |
|:-:|------|------|
| 1 | Controller 测试 mvn test 跑通 | 刚写的 19 个测试需验证编译+通过 |
| 2 | Service create() 字段映射补全 | V12/V13/V14 的 create() 还没把 request 字段映射到 entity |
| 3 | Docker E2E 验证 | 完整拉起 PostgreSQL + Redis + App，调通全链路 API |

### P2 — 中期（1周内）

| # | 任务 | 说明 |
|:-:|------|------|
| 4 | Agent 编排集成 | ACP 协议接入 Kimi/Claude/Codex → delegate_task 分发 |
| 5 | CI 流水线 | GitHub Actions 自动编译+测试 |
| 6 | Project 1 → 2 端到端导入测试 | 真实 Excel/JSON 文件导入验证 |

### P3 — 长期

| # | 任务 | 说明 |
|:-:|------|------|
| 7 | Swagger API 描述补全 | 补充 @Schema description |
| 8 | README 更新 | 新 API 模块说明 |
| 9 | Cursor agent CLI | 重新安装 |
