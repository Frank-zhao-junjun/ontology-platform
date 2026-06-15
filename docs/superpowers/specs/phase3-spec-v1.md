# ontology-platform Phase 3 Spec v1.0

> 版本: v1.0 | 状态: **Draft** | 2026-06-15  
> 主题: **项目1 对接 — `ontology.platform/v2` 导入、持久化、校验、MCP**  
> 交换契约: [ontology-manifest-spec-v2.md](../../shared/ontology-manifest-spec-v2.md)  
> 前置: Phase 2 v1.0 Final + Phase 1 v1.2 Final  
> 差距分析: [项目1-项目2对接差距分析.md](../../shared/项目1-项目2对接差距分析.md)

## Version History

| 版本 | 日期 | 变更 |
|------|------|------|
| v1.0 Draft | 2026-06-15 | 初稿 — v2 契约 + 分阶段实施 + DDL/API 轮廓 |

---

## 1. 范围

| 板块 | 说明 | 阶段 |
|------|------|:----:|
| v2 交换契约 | `OntologyExchange`，`spec.project` 对齐 `OntologyProject`（方案 A） | Spec ✅ |
| Import 管道 | JSON/YAML 全量 + Excel parsedData + v1 upcast | 3a–3b |
| 校验分层 | V01–V11 + VE/VM/VX/V-LC/V-AS 插件 | 3a–3d |
| DDL 扩展 | 场景/组织/EPC 图/Semantic 等 | 3a–3d |
| MCP 对接 | resolve_intent ← SemanticLayer | 3c |
| Legacy | v1 `/api/v1/manifests/*` 保留 + upcast | 3a |

**不在 Phase 3**：HR 同步运行时、ReferenceDocument 存储、项目1 设计台 UI。

---

## 2. 架构决策

| 决策 | 选择 | 理由 |
|------|------|------|
| 契约版本 | `ontology.platform/v2`，**方案 A**：`spec.project` = `OntologyProject` 1:1 | 设计台零映射导出 |
| 导入 API 路径 | `/api/v2/exchanges/*` | 与 v1 manifests 分离 |
| 未就绪模型 | JSONB 快照 + 延迟关系化 | 项目1 P6 代码渐进，平台先存后拆 |
| 校验 | 责任链 + 分层 severity | strict/warn 可配置 |
| v1 迁移 | `ManifestUpcaster` 服务 | 旧 YAML 自动 upcast |
| EPC | 双形态：chain（目标）+ profile（当前） | 对齐 EPC-Upgrade-Spec v3.1 |
| Semantic | 独立表群 + 发布时编译 MCP intent 索引 | resolve_intent 热路径 |
| Lifecycle | 导入时可选编译 `spec.lifecycle`；Semantic 读 `project.agentSemanticLayer` | MCP resolve_intent |

---

## 3. 数据模型（Flyway V9+ 概要）

> 详细 DDL 在 3a 实施时拆分为 V9a/V9b…；此处为 Spec 级表清单。

### 3a 核心（阻塞首联调）

| 表 | 用途 |
|----|------|
| `exchange_import` | v2 导入草稿（JSONB 全量快照 + status） |
| `exchange_version` | 发布版本（关联 ontology_id） |
| `business_scenario` | 业务场景 |
| `object_type` 扩展 | + `entity_role`, `parent_aggregate_id`, `business_scenario_id`, `attributes_jsonb` |

### 3b 平台扩展

| 表 | 用途 |
|----|------|
| `department`, `position`, `position_responsibility` | 组织体系 |
| `metadata_template` | 元数据模板 |
| `master_data_definition` | 主数据定义（实例可选） |
| `business_metric` | 指标 |
| `orchestration`, `process_step` | 流程模型 |

### 3c 语义与生命周期

| 表 | 用途 |
|----|------|
| `intent`, `intent_slot` | Agent 意图 |
| `business_term`, `semantic_relation` | 术语与关系 |
| `agent_policy_semantic` | Semantic AgentPolicy（区别于 governance agent_policy） |
| `error_recovery`, `semantic_field_mapping` | 恢复与字段映射 |
| `entity_lifecycle_snapshot` | 按实体 JSONB 聚合（可选） |

### 3d EPC 图

| 表 | 用途 |
|----|------|
| `epc_chain`, `epc_node`, `epc_edge`, `epc_model_ref` | 全域关联图 |
| `epc_profile` | Legacy EpcAggregateProfile JSONB |

---

## 4. REST API 契约

### 4.1 v2 Exchange（新增）

| 方法 | 端点 | 说明 |
|------|------|------|
| POST | `/api/v2/exchanges/import` | 导入 v2 JSON/YAML |
| POST | `/api/v2/exchanges/import/excel` | Excel parsedData 或 xlsx |
| POST | `/api/v2/exchanges/{draftId}/validate` | 重算校验 |
| POST | `/api/v2/exchanges/{draftId}/preview` | diff |
| POST | `/api/v2/exchanges/{draftId}/publish` | 发布 |
| GET | `/api/v2/exchanges/{id}/export` | round-trip |

**POST /api/v2/exchanges/import 请求体**（JSON）:

```json
{
  "document": { "...": "完整 OntologyExchange" },
  "externalId": "manufacturing-ontology",
  "validationMode": "strict"
}
```

### 4.2 Legacy v1（保留）

| 端点 | Phase 3 行为 |
|------|-------------|
| `/api/v1/manifests/import` | Upcast v1→v2 → 走 v2 管道 |
| 其他 v1 端点 | 不变 |

### 4.3 查询扩展

| 方法 | 端点 | 说明 |
|------|------|------|
| GET | `/api/v1/ontologies/{id}/lifecycle/{entityId}` | EntityLifecycle 聚合 |
| GET | `/api/v1/ontologies/{id}/semantic-layer` | AgentSemanticLayer |
| GET | `/api/v1/ontologies/{id}/epc/coverage` | EPC 覆盖报告 |

---

## 5. 校验器架构

```
ExchangeValidator
├── StructuralValidator     (V01–V11, upcast-aware)
├── EpcVeValidator          (VE-01..17)
├── EpcVmValidator          (VM-* 39 rules)
├── EpcVxValidator          (VX-01..15)
├── LifecycleValidator      (V-LC-01..15)
├── SemanticValidator       (V-AS-01..15)
└── OrganizationValidator   (VM-O, V-XL-O, VM-HR)
```

每 validator 实现 `ValidationPlugin` 接口；`validationMode` 控制 error vs warning。

---

## 6. MCP 变更

| 工具 | Phase 3 变更 |
|------|-------------|
| `resolve_intent` | 读 `intent` 表 + `triggerPhrases` 匹配 → `actionId` |
| `query_ontology` | 增加 lifecycle / epc coverage / semantic coverage |
| `execute_action` | 读 Action Lifecycle 字段（confirmation, idempotencyKeyTemplate） |
| `tools/list` | 发布版本变更时刷新；Semantic 覆盖率低于阈值 → deprecation warning |

---

## 7. 实施顺序

| Phase | 内容 | 交付物 | 依赖 |
|-------|------|--------|------|
| **3a** | Upcaster + v2 import + V01–V11 + data/behavior/rules/events/gov 落库 | V9a DDL, ExchangeController, ManifestUpcaster | 无 |
| **3b** | organization + metrics + metadata + excel path | V9b DDL, ExcelExchangeMapper | 3a |
| **3c** | semanticLayer 表 + MCP resolve_intent + lifecycle 编译 | V9c DDL, SemanticService | 3a |
| **3d** | epc chain + VE/VM/VX 全量 + coverage API | V9d DDL, EpcGraphService | 3a, 项目1 EpcChain 类型 |

### 3a 任务分解

| Task | 说明 |
|------|------|
| 3a-1 | `OntologyExchangeDocument` + `OntologyProject` Java VO（字段名与 ontology.ts 1:1） |
| 3a-2 | `ManifestUpcasterV1ToV2` |
| 3a-3 | `ExchangeImportService` + `exchange_import` 表 |
| 3a-4 | `StructuralValidator` 扩展（entityRole, businessScenarioId） |
| 3a-5 | `ExchangeController` + OpenAPI v2.2 |
| 3a-6 | 单元测试 + golden `manufacturing-exchange-v2.json` |

---

## 8. 测试策略

| 层 | 场景 |
|----|------|
| Upcaster | v1 YAML → v2 JSON golden diff |
| Import | v2 JSON → DB 计数 = importedCounts |
| Validator | 每条 VE-01 至少 1 负例 |
| MCP | intent phrase → correct actionId |
| E2E | 项目1 export JSON → 项目2 import → publish → tools/call |

---

## 9. 验证清单

### 9.1 Phase 3a（Spec 就绪后可开工）

| # | 项 | 状态 |
|---|-----|:----:|
| 1 | v2 Spec 评审通过 | 🔄 |
| 2 | `manufacturing-exchange-v2.json` import 201 | ⏳ |
| 3 | v1 YAML upcast + import 等价 | ⏳ |
| 4 | businessScenarioId 持久化可查 | ⏳ |
| 5 | child_entity → parentAggregateId 约束 | ⏳ |

### 9.2 Phase 3c

| # | 项 | 状态 |
|---|-----|:----:|
| 1 | resolve_intent 命中 manufacturing intent | ⏳ |
| 2 | GET semantic-layer 与导入一致 | ⏳ |

---

## 10. 兼容性

| 项 | 说明 |
|----|------|
| Phase 1/2 API | 全部保留；v2 为 additive |
| v1 Manifest | Legacy，经 upcast 支持 |
| 已发布本体 | 不自动迁移；需 re-import v2 |
| 项目1 P6 | 未导出字段以 JSONB 存储，校验逐步收紧 |

---

> Phase 3 完成后平台达到 **v4.0**（与项目1 12 模型体系对接就绪）。
