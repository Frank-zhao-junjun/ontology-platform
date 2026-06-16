# 项目 1→2 数据模型映射

> 项目1: Ontology 设计工具 (Flask + React, :5000)
> 项目2: Ontology-platform 服务平台 (Spring Boot, :8080)
> 更新日期: 2026-06-16

---

## 1. 领域总览 → 本体

| 项目1 (Domain) | 项目2 (Ontology) | 数据库字段 | 匹配 |
|---------------|-----------------|-----------|------|
| `id` | `id` | `ontology.id UUID` | ✅ |
| `name` | `name` | `ontology.name VARCHAR(100)` | ✅ |
| `description` | `description` | `ontology.description TEXT` | ✅ |
| `tags` | → 无直接对应 | — | ❌ 可存入 `extended_data` |
| `created_at` | `createdAt` | `ontology.created_at` | ✅ |

**差异**: 项目2 多出 `tenant_id`, `display_name`, `version`, `status`, `published_at`, `object_type_count`, `action_type_count`, `created_by` 等字段。导入时填充默认值即可。

---

## 2. 维1_静态结构：实体 → 对象类型

| 项目1 (Entity) | 项目2 (ObjectType) | 数据库字段 | 匹配 |
|----------------|-------------------|-----------|------|
| `id` | `id` | `object_type.id UUID` | ✅ |
| `name` | `name` | `object_type.name VARCHAR(100)` | ✅ |
| — | `displayName` | `object_type.display_name` | ✅ 可用 name 填充 |
| `description` | `description` | `object_type.description TEXT` | ✅ |
| — | `primaryKey` | `object_type.primary_key` | ⚠️ 项目1 无主键概念，默认 `id` |
| `domain_id` | `ontologyId` | `object_type.ontology_id` | ✅ 关联到本体 |
| `subDomain` | → 无直接对应 | — | ❌ 可用 `parent_id` 模拟层级 |
| `scenario` | → 无直接对应 | — | ❌ |

---

## 3. 实体属性 → 属性定义

| 项目1 (Attribute) | 项目2 (Property) | 数据库字段 | 匹配 |
|-------------------|-----------------|-----------|------|
| `id` | `id` | `property_definition.id UUID` | ✅ |
| — | `objectTypeId` | `property_definition.object_type_id` | ✅ FK |
| `name` | `name` | `property_definition.name` | ✅ |
| `type` | `dataType` | `property_definition.data_type` | ⚠️ 类型枚举需映射 |
| — | `displayName` | `property_definition.display_name` | ✅ 可用 name 填充 |
| `required` | `isRequired` | `property_definition.is_required` | ✅ |
| `unique` | `isUnique` | `property_definition.is_unique` | ✅ |
| — | `isComputed` | `property_definition.is_computed` | ✅ 默认 false |
| — | `defaultValue` | `property_definition.default_value JSONB` | ✅ |

**数据类型的枚举映射**:

| 项目1 type | 项目2 dataType |
|-----------|---------------|
| `string`, `text` | `STRING` |
| `number`, `integer` | `INTEGER` |
| `float`, `double` | `FLOAT` |
| `boolean` | `BOOLEAN` |
| `date`, `datetime` | `DATE` |
| `object`, `array` | `JSON` |
| 其他 | `STRING` (兜底) |

---

## 4. 实体关系 → 关系定义

| 项目1 (Relation) | 项目2 (Relation) | 数据库字段 | 匹配 |
|------------------|-----------------|-----------|------|
| `type` (关系名) | `name` | `relation_definition.name` | ✅ |
| `source` | `sourceTypeId` | `relation_definition.source_type_id` | ✅ FK → object_type |
| `target` | `targetTypeId` | `relation_definition.target_type_id` | ✅ FK → object_type |
| `inverseOf` | `reverseName` | `relation_definition.reverse_name` | ✅ |
| — | `cardinality` | `relation_definition.cardinality` | ⚠️ 项目1 未定义基数，默认 `1:N` |
| — | `displayName` | `relation_definition.display_name` | ✅ 可用 name 填充 |
| — | `ontologyId` | `relation_definition.ontology_id` | ✅ FK → ontology |

---

## 5. 维2_动态行为：行为 → 行为定义

| 项目1 (Action) | 项目2 (ActionDefinition) | 字段 | 匹配 |
|----------------|-------------------------|------|------|
| `id` | `id` | `action_definition.id` | ✅ |
| `name` | `name` | `action_definition.name` | ✅ |
| `input` | `inputSchema` | `action_definition.input_schema JSONB` | ✅ |
| `output` | `outputSchema` | `action_definition.output_schema JSONB` | ✅ |
| `domain` | `domain` | `action_definition.domain` | ✅ |
| — | `entityId` | `action_definition.entity_id` | ⚠️ 需关联到 object_type |
| — | `actionType` | `action_definition.action_type` | ⚠️ 默认为 `CUSTOM` |
| — | `riskLevel` | `action_definition.risk_level` | ⚠️ 默认为 `READ` |

### 状态机 → StateMachine + StateTransition

| 项目1 (StateMachine) | 项目2 (StateMachine) | 字段 | 匹配 |
|---------------------|---------------------|------|------|
| `id` | `id` | `state_machine.id` | ✅ |
| `name` | `name` | `state_machine.name` | ✅ |
| — | `initialState` | `state_machine.initial_state` | ⚠️ 需从 states 推断 |
| — | `states` | `state_machine.states JSONB` | ✅ 存储完整状态列表 |

| 项目1 (Transition) | 项目2 (StateTransition) | 字段 | 匹配 |
|-------------------|------------------------|------|------|
| `from` | `fromState` | `state_transition.from_state` | ✅ |
| `to` | `toState` | `state_transition.to_state` | ✅ |
| `trigger` | `trigger` | `state_transition.trigger` | ✅ |
| — | `guardCondition` | `state_transition.guard_condition` | ⚠️ 可选 |

### 指标 → 无对应

项目1 `indicators` 在项目2 **无独立表**。可考虑扩展 `action_definition` 的 `output_schema` 或新建 `indicator` 表。

---

## 6. 维4_事件消息

### 事件 → DomainEvent

| 项目1 (EventType) | 项目2 (DomainEvent) | 字段 | 匹配 |
|-------------------|--------------------|------|------|
| `id` | `id` | `domain_event.id` | ✅ |
| `name` | `name` | `domain_event.name` | ✅ |
| — | `displayName` | `domain_event.display_name` | ✅ 可用 name 填充 |
| `severity` | `severity` | `domain_event.severity` | ✅ |
| `source` | `source` | `domain_event.source` | ✅ |
| `targetEntity` | `entityId` | `domain_event.entity_id` | ✅ |
| — | `eventType` | `domain_event.event_type` | ⚠️ 需默认值 |
| — | `payloadSchema` | `domain_event.payload_schema JSONB` | ✅ |

### 因果链 → Causality

| 项目1 (Causality) | 项目2 (Causality) | 字段 | 匹配 |
|-------------------|-------------------|------|------|
| `cause` | `causeEventId` | `causality.cause_event_id` | ✅ FK → domain_event |
| `effect` | `effectEventId` | `causality.effect_event_id` | ✅ FK → domain_event |
| — | `delayMs` | `causality.delay_ms` | ✅ 默认 0 |
| — | `condition` | `causality.condition` | ✅ 可空 |

---

## 7. EPC流程 → EpcStep

| 项目1 (EPC Step) | 项目2 (EpcStep) | 字段 | 匹配 |
|------------------|----------------|------|------|
| `event_trigger` | `triggerEventId` | `epc_step.trigger_event_id` | ⚠️ 需解析为 UUID |
| `action` | `actionId` | `epc_step.action_id` | ⚠️ 需解析为 UUID |
| `conditions` | `conditions` | `epc_step.conditions JSONB` | ✅ |
| `guards` | `guards` | `epc_step.guards JSONB` | ✅ |
| — | `flowName` | `epc_step.flow_name` | ⚠️ 需生成 |
| — | `stepOrder` | `epc_step.step_order` | ⚠️ 需按顺序编号 |
| — | `timeoutMs` | `epc_step.timeout_ms` | ✅ 默认 60000 |

---

## 8. 维3_规则约束 → ❌ 缺失

项目1 的规则约束包含：

| 项目1 类型 | 项目2 对应 | 状态 |
|-----------|-----------|------|
| `validations` (校验) | ❌ 无表 | 需新建 `validation_rule` 表 |
| `guardrails` (护栏) | ❌ 无表 | 需新建 `guardrail_rule` 表 |
| `policies` (策略) | ❌ 无表 | 可扩展 governance 模块 |
| `permissions` (权限) | → AgentRole/RolePermission | ⚠️ 概念不同，平台治理是 API 级别 |
| `probes` (探针) | ❌ 无表 | 需新建或由外部监控工具覆盖 |

---

## 9. 维5_外部接口 → ❌ 缺失

项目1 的外部接口包含：

| 项目1 类型 | 项目2 对应 | 状态 |
|-----------|-----------|------|
| `apis` (API定义) | ❌ 无表 | 需新建 `interface_definition` 表 |
| `queries` (查询) | ❌ 无表 | — |
| `compute` (计算) | ❌ 无表 | — |
| `notifications` (通知) | → Webhook | ⚠️ 概念部分重叠 |
| `reports` (报表) | ❌ 无表 | — |

---

## 10. 汇总

| 维度 | 项目1 Sheet | 项目2 对应表 | 匹配度 |
|------|------------|-------------|--------|
| 领域总览 | Sheet 1 | `ontology` | ✅ 100% |
| 静态结构·实体 | Sheet 2 | `object_type` | ✅ 90%（缺 subDomain/scenario） |
| 静态结构·属性 | Sheet 2 | `property_definition` | ✅ 100%（类型需映射） |
| 静态结构·关系 | Sheet 2 | `relation_definition` | ✅ 90%（缺 cardinality 推导） |
| 动态行为·行为 | Sheet 3 | `action_definition` | ✅ 90% |
| 动态行为·状态机 | Sheet 3 | `state_machine` + `state_transition` | ✅ 85% |
| 动态行为·指标 | Sheet 3 | ❌ 无 | **0%** |
| 规则约束·校验 | Sheet 4 | ❌ 无 | **0%** |
| 规则约束·护栏 | Sheet 4 | ❌ 无 | **0%** |
| 规则约束·策略 | Sheet 4 | ❌ 无 | **0%** |
| 规则约束·探针 | Sheet 4 | ❌ 无 | **0%** |
| 事件消息·事件 | Sheet 5 | `domain_event` | ✅ 90% |
| 事件消息·因果链 | Sheet 5 | `causality` | ✅ 100% |
| 外部接口·API | Sheet 6 | ❌ 无 | **0%** |
| 外部接口·查询/计算/通知/报表 | Sheet 6 | ❌ 无 | **0%** |
| EPC流程 | Sheet 7 | `epc_step` | ✅ 85%（需名称/顺序字段） |

**总体匹配度：约 65%** (表结构 95%，代码层 0%)

| 层次 | 状态 | 说明 |
|------|------|------|
| ✅ Flyway SQL (10张新表) | 已完成 | V9 + V10 迁移文件 |
| ✅ Domain Entity (10个) | 已完成 | POJO + create() 工厂方法 |
| ✅ PO + Mapper + XML (30个) | 已完成 | MyBatis 基础设施 |
| ✅ Service + DTO (30个) | 已完成 | Request/Response + Service CRUD |
| ✅ REST Controller (10个) | 已完成 | CRUD 端点 + OpenAPI |
| ✅ 导入适配器 | 已完成 | `scripts/import_from_project1.py` 可直接调用全链路 API |


- ✅ **可直导** (已有表+代码): ontology, object_type, property_definition, relation_definition, action_definition, state_machine, state_transition, domain_event, causality, epc_step
- ✅ **表已就绪** (V9+V10): validation_rule, guardrail_rule, policy_rule, probe_definition, api_definition, query_definition, compute_definition, notification_definition, report_definition, indicator_definition
- ❌ **代码未实现**: 以上 10 张新表均无 PO/Mapper/Service/Controller

### 修复记录 (2026-06-16)

| 操作 | 状态 | 说明 |
|------|------|------|
| ✅ Flyway V9 | 已完成 | 4张规则表 SQL |
| ✅ Flyway V10 | 已完成 | 6张接口+指标表 SQL |
| ❌ 代码层 | 未开始 | PO/Mapper/XML/Service/DTO/Controller 约 70文件 |
| ❌ 导入适配器 | 等待中 | 需 API 就绪后才能调通 |
