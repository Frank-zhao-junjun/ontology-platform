# Phase 3 工作计划

> 开始：2026-06-19
> 基于：Phase 3 Spec v1.0 (Draft) + ontology-manifest-spec-v2.md

## 范围

Phase 3a — v2 交换契约核心管道

| Task | 内容 | 交付物 |
|:----:|------|--------|
| 3a-0 | golden JSON test fixture | `manufacturing-exchange-v2.json` |
| 3a-1 | OntologyExchangeDocument + OntologyProject Java VO | 1:1 匹配 ontology.ts 字段名 |
| 3a-2 | ManifestUpcasterV1ToV2 | v1 YAML → v2 Exchange |
| 3a-3 | ExchangeImportService + exchange_import 表 | Flyway V9 DDL |
| 3a-4 | ExchangeController + 路由 | POST /api/v2/exchanges/* |
| 3a-5 | 单元测试 + 回归 | ≥6 测试用例 |

## V2 核心映射

| v1 (`spec.semantic.objectTypes[]`) | v2 (`spec.project.dataModel.entities[]`) |
|-------------------------------------|------------------------------------------|
| `kind: aggregate_root` → `entityRole: aggregate_root` | |
| `aggregateRootId` → `parentAggregateId` | |
| `businessScenarioIds[]` → `businessScenarioId` | |
| `properties[]` → `attributes[]` | |
| `relations[].targetObjectTypeId` → `relations[].targetEntity` | |
| `relations[].cardinality` → `relations[].type` | |

## V1 → V2 Upcast 路径

| v1 路径 | v2 路径 |
|---------|---------|
| `metadata` | `metadata` (不变) |
| `spec.semantic.boundedContext` | `spec.project.dataModel` |
| `spec.semantic.objectTypes` | `spec.project.dataModel.entities` |
| `spec.semantic.valueObjects` | `spec.project.dataModel.entities` (entityRole=child_entity) |
| `spec.semantic.businessScenarios` | `spec.project.dataModel.businessScenarios` |
| `spec.semantic.stateMachines` | `spec.project.behaviorModel.stateMachines` |
| `spec.behavior.actions` | `spec.project.behaviorModel.actions` |
| `spec.behavior.rules` | `spec.project.ruleModel.rules` |
| `spec.events.domainEvents` | `spec.project.eventModel.events` |
| `spec.governance` | `spec.project.governanceModel` |
| `spec.dataSources` | `spec.project.dataSourcesModel.sources` |
