# 项目2 — TODO

> 最后更新：2026-06-26
> 最新合并：`main ← 2d95763`

---

## ✅ 已完成

### P1 — 短期修复

| 状态 | # | 任务 | 说明 |
|:----:|:-:|------|------|
| ✅ | 1 | Controller 测试 mvn test 跑通 | 19 个测试文件 174/174 通过 |
| ✅ | 2 | Service create() 字段映射补全 | 11 Services 补全 mapRequest |
| ⏳ | 3 | Docker E2E 验证 | 本机无 Docker/PostgreSQL, 待环境就绪 |

### Phase 1 — 项目1 Import API (POST /api/v1/ontologies/import)

| 状态 | # | 任务 | 说明 |
|:----:|:-:|------|------|
| ✅ | P1-I01 | OntologyImportController + ManifestImportPO/Mapper | 7/7 tests |
| ✅ | P1-I02 | draftId client-side UUID (不依赖 ASSIGN_UUID 回写) | Testable |
| ✅ | P1-I03 | 404/422/409 错误码处理 | TC-01~TC-06 |

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
| ✅ | 3b V12 | 8表 | ~64 |
| ✅ | 3c V13 | 6表 | ~48 |
| ✅ | 3d V14 | 5表: EpcChain/EpcNode/EpcEdge/EpcModelRef/EpcProfile | ~40 |

### P2 — Agent 编排 + CI + 端到端

| 状态 | # | 任务 | 说明 | 测试 |
|:----:|:-:|------|------|:----:|
| ✅ | 4 | Agent 编排集成 | ACP 协议 REST API (kimi/claude/codex) | 11 tests |
| ✅ | 5 | CI 流水线 | GitHub Actions 自动编译+测试 | 1m26s ✅ |
| ✅ | 6 | 项目1→2 E2E 导入 | ManifestConverter + ManifestService 全链路 | 6 tests |

### 质量

| 状态 | 内容 | 统计 |
|:----:|------|:----:|
| ✅ | 全量测试 | **174 tests, 0 failures** |
| ✅ | mvn compile | BUILD SUCCESS |
| ✅ | CI (GitHub Actions) | ✅ 通过 |
| ✅ | 合并 main | 已推送 |

### 新增文件统计

| 批次 | 文件 | 行数 |
|:----:|:-----|:----:|
| P2 #4 | AgentController + AgentOrchestrationService + AgentBridgeService + 3 DTOs + 2 Tests | ~640 |
| P2 #5 | .github/workflows/ci.yml | 37 |
| P2 #6 | Project1ToProject2E2ETest + project1-manifest-export.json | ~280 |
| Docs | P2-I04-import-chain-analysis.md | 1004 |

---

## 📋 待办

### P3 — 长期

| 日期 | 内容 |
|------|------|
| 06-26 | Swagger DTO 补全 ✅ · README 更新 ✅ · Cursor agent CLI 不再维护（改用 Hermes）✅ |
| — | **P3 所有任务完成。新需求请在此添加。** |
