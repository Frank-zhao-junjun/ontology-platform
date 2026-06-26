# 本体建模平台 PRD v3.0 — Phase 3 增量更新

> 版本：v3.0（增量）
> 日期：2026-06-26
> 基于：PRD v2.0 + Phase 2/3 实施完成

---

## 1. Phase 3 已交付能力

### 1.1 V12-V14 新域（19 表）

| 域 | 表 | 状态 |
|----|----|:----:|
| 组织 | department, position, position_responsibility | ✅ |
| EPC 图 | epc_chain, epc_node, epc_edge, epc_model_ref, epc_profile | ✅ |
| Lifecycle | entity_lifecycle_snapshot | ✅ |
| Semantic Layer | agent_intent, intent_slot, semantic_relation, semantic_field_mapping, business_term, agent_policy_semantic, error_recovery | ✅ |
| 流程 | orchestration, process_step | ✅ |
| 业务指标 | business_metric | ✅ |
| BusinessScenario | business_scenario | ✅ |

### 1.2 校验体系扩展

| 插件 | 规则数 | 覆盖范围 |
|------|:------:|---------|
| VE | 17 | EPC 要素一致性 |
| VM | 39 | EPC 覆盖率 |
| VX | 15 | 交叉一致性 |
| V-LC | 15 | Lifecycle |
| V-AS | 15 | Semantic Layer |
| V-ORG | — | 组织校验 |
| Manifest V01-V11 | 11 | 基础契约校验 |
| **合计** | **106+** | |

### 1.3 Import v2 管道

| 组件 | 说明 | 状态 |
|------|------|:----:|
| Project1JsonToExchangeConverter | 项目1 JSON → ExchangeDocument | ✅ |
| ExchangePhase3bPublisher | V2 全量发布（ObjectType+属性+EPC+Lifecycle+Semantic+组织） | ✅ |
| ExchangePhase3aPublisher | Legacy V01-V11 兼容 | ✅ |
| YamlManifestConverter | YAML → ExchangeDocument | ✅ |
| 跨项目 E2E 6 场景 | CROSS-1~6 | ✅ |

### 1.4 BusinessScenario 支持

| 组件 | 说明 | 状态 |
|------|------|:----:|
| business_scenario 表 (V20) | id, ontology_id, name, name_en, description, project_id | ✅ |
| BusinessScenario 实体 + PO + Mapper | MyBatis-Plus 全链路 | ✅ |
| Import 管道集成 | import 时自动持久化场景数据 | ✅ |

## 2. 测试统计

| 层级 | v2.0 | v3.0 |
|------|:----:|:----:|
| 后端单元测试 | 106 | 120+ |
| IT 测试 | 73 | 80+ |
| MCP 测试 | 6 | 8+ |
| 跨项目 E2E | — | 6 |
| **合计** | **185** | **~215** |

## 3. 待办

| 需求 | 说明 | 优先级 |
|------|------|--------|
| TDD v3 测试实现 | 对齐 TDD v3.0 文档中新列出的测试用例 | P2 |
| Docker Compose 集成 | mcp-server Dockerfile + docker-compose 编排 | P3 |
| CI 中 mcp-server 测试门 | 确保 npm test 在 CI 中执行 | P3 |
