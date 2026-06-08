# OntologyManifest 规范

> **版本**：`ontology.platform/v1`  
> **状态**：draft（与 [User Story Map v1.2.1](./PRD-本体建模平台-UserStoryMap-v1.2.md)、[TDD v2.0 §5](./TDD-本体建模平台-v2.0.md) 对齐）  
> **适用**：项目1 Ontology 设计台（导出） · 项目2 ontology-platform（导入/发布） · Agent/MCP 运行时（消费 published）

---

## 1. 目的与范围

`OntologyManifest` 是两项目之间的**唯一机器可读交接契约**：

| 项目 | 角色 |
|------|------|
| **Ontology 设计台** | 编辑草稿 → **导出** YAML/JSON Manifest |
| **ontology-platform** | **导入** → 校验 → 版本化 → **发布** → MCP 查询 |
| **Agent 运行时** | 仅消费平台 `published` 版本；不直接读设计台草稿 |

### 1.1 包含（In Scope）

- 语义层：限界上下文、业务场景、对象类型、属性、关系、状态机、数据获取元数据  
- 行为层：行为、校验规则、指标定义、事务边界、副作用（可选）  
- 事件层：领域事件、集成事件、路由、处理器（逻辑配置；事件存储连接可选）  
- 治理层：角色、对象/字段权限、Agent 沙箱策略  
- **不含**：业务实例数据、明文密钥、运行时审计日志条目  

### 1.2 非目标（Out of Scope）

- 不替代 ERP/MES 业务表结构  
- 不在 Manifest 内执行 SQL/API（仅描述如何获取）  
- 不承载平台运维事件（如 `OntologyPublished`）  

---

## 2. 文档顶层结构

```yaml
apiVersion: ontology.platform/v1   # 必填，破坏性变更升 v2
kind: OntologyManifest               # 必填，固定值
metadata: { ... }                    # 标识、版本、编译信息
spec: { ... }                        # 五层本体 + 数据源
```

### 2.1 `metadata`（必填）

| 字段 | 类型 | 必填 | 说明 |
|------|------|:----:|------|
| `id` | string | ✓ | 本体稳定标识，如 `manufacturing-ontology` |
| `version` | string | ✓ | 语义化版本，与平台发布版本**严格一致**（如 `1.0.0`） |
| `name` | string | ✓ | 显示名称 |
| `displayName` | string | | 中文名 |
| `description` | string | | |
| `boundedContext` | string | ✓ | 限界上下文名称，如 `生产制造` |
| `domainTags` | string[] | | 领域标签：`生产制造`、`质量管理` 等 |
| `compiledAt` | string (ISO8601) | | 编译/导出时间 |
| `compiledBy` | string | | 导出人/服务 |
| `source` | enum | | `ontology-designer` \| `ontology-platform` |
| `status` | enum | | 设计台导出默认为 `draft`；平台发布后由平台改写 |

### 2.2 `spec` 七段（与 US-A01 对齐）

```yaml
spec:
  semantic: { ... }      # 语义层
  behavior: { ... }      # 行为层
  events: { ... }        # 事件层
  governance: { ... }    # 治理层
  dataSources: [ ... ]   # 数据源元数据（也可嵌在 semantic 内引用）
```

---

## 3. 语义层 `spec.semantic`

### 3.1 限界上下文 `boundedContext`

```yaml
spec:
  semantic:
    boundedContext:
      id: bc-manufacturing
      name: 生产制造
      nameEn: Manufacturing
      description: 生产执行限界上下文
      ontologyId: manufacturing-ontology   # 与 metadata.id 一致或映射
```

### 3.2 业务场景 `businessScenarios[]`

| 字段 | 类型 | 必填 | 说明 |
|------|------|:----:|------|
| `id` | string | ✓ | |
| `name` / `nameEn` | string | ✓ | 如 `面向库存生产` / `MTS` |
| `description` | string | | 场景业务背景（EPC/Agent 可用） |
| `applicableObjectTypeIds` | string[] | | 适用对象类型 ID |

### 3.3 对象类型 `objectTypes[]`

统一 **ObjectType** 概念（平台 `ObjectType` ≡ 设计台 `Entity`）。

| 字段 | 类型 | 必填 | 说明 |
|------|------|:----:|------|
| `id` | string | ✓ | 全局唯一（上下文内） |
| `name` / `nameEn` | string | ✓ | |
| `kind` | enum | ✓ | `aggregate_root` \| `entity` \| `value_object` |
| `aggregateRootId` | string | | `kind=entity` 时指向所属聚合根 |
| `businessScenarioIds` | string[] | | 适用场景 |
| `description` | string | | |
| `properties` | Property[] | ✓ | 见 3.4 |
| `relations` | Relation[] | | 见 3.5 |

**聚合根规则（与 DDD 一致）**

- 至少 1 个 `kind: aggregate_root`  
- `entity` 必须带 `aggregateRootId`  
- Agent **仅通过聚合根** 访问聚合内对象（治理层可再收紧）  

### 3.4 属性 `properties[]`

| 字段 | 类型 | 必填 | 说明 |
|------|------|:----:|------|
| `id` | string | ✓ | |
| `name` / `nameEn` | string | ✓ | |
| `dataType` | enum | ✓ | 见下表 |
| `required` | boolean | | |
| `unique` | boolean | | |
| `default` | string | | |
| `enumValues` | string[] | | `dataType=enum` 时 |
| `reference` | object | | `dataType=reference` 时 |
| `valueObjectRef` | string | | 引用 `valueObjects[].id` |
| `sensitive` | boolean | | `true` 触发字段级权限 |
| `metadataTemplateId` | string | | 设计台元数据模板 |
| `description` | string | | |

**`dataType` 枚举（v1）**

`string` | `integer` | `decimal` | `boolean` | `date` | `datetime` | `enum` | `text` | `reference` | `json`

**`reference` 对象**

```yaml
reference:
  kind: entity | masterData
  targetObjectTypeId: material          # kind=entity
  masterDataType: 供应商主数据           # kind=masterData
  masterDataField: 供应商编码
```

### 3.5 关系 `relations[]`

| 字段 | 类型 | 必填 | 说明 |
|------|------|:----:|------|
| `id` | string | ✓ | |
| `name` | string | ✓ | |
| `sourceObjectTypeId` | string | ✓ | |
| `targetObjectTypeId` | string | ✓ | |
| `cardinality` | enum | ✓ | `1:1` \| `1:N` \| `N:M` |
| `relationKind` | enum | | `composition` \| `aggregation` \| `reference` |
| `viaObjectTypeId` | string | | N:M 中间对象 |
| `description` | string | | |

### 3.6 值对象 `valueObjects[]`（可复用）

```yaml
valueObjects:
  - id: vo-quantity
    name: 数量
    nameEn: Quantity
    properties:
      - { name: 数值, nameEn: amount, dataType: decimal, required: true }
      - { name: 单位, nameEn: unit, dataType: string, required: true }
```

### 3.7 状态机 `stateMachines[]`

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | string | |
| `name` | string | |
| `objectTypeId` | string | 绑定聚合根/实体 |
| `statusField` | string | 状态字段名，默认 `status` |
| `states[]` | | `name`, `code`, `isInitial`, `isFinal` |
| `transitions[]` | | `from`, `to`, `trigger`, `actionId?` |

---

## 4. 行为层 `spec.behavior`

### 4.1 行为 `actions[]`

| 字段 | 类型 | 必填 | 说明 |
|------|------|:----:|------|
| `id` | string | ✓ | |
| `name` / `nameEn` | string | ✓ | 如 `生产订单下达` |
| `aggregateRootId` | string | ✓ | 入口聚合根 |
| `businessScenarioIds` | string[] | | |
| `parameters[]` | | | `name`, `nameEn`, `dataType`, `required` |
| `preRuleIds` | string[] | | 前置校验规则 |
| `publishesEventIds` | string[] | | 成功后声明发布的领域事件 |
| `allowedStateFrom` | string[] | | 允许的起始状态码（可选） |
| `mcpToolName` | string | | 平台生成的 MCP 工具名（可选，US-A04） |

### 4.2 校验规则 `rules[]`

| 字段 | 类型 | 必填 | 说明 |
|------|------|:----:|------|
| `id` | string | ✓ | 如 `rule-kitting` |
| `name` | string | ✓ | 物料齐套校验 |
| `type` | enum | ✓ | `precondition` \| `field_validation` \| `cross_field` \| `cross_entity` |
| `expression` | object | ✓ | 结构化条件（见 4.2.1） |
| `errorMessage` | string | ✓ | 人类可读 |
| `failurePayloadSchema` | object | | **Agent 可读**结构化失败（US-B03） |
| `enabled` | boolean | | 默认 true |

**4.2.1 失败响应 Schema（运行时，非 Manifest 正文）**

校验失败时执行引擎应返回（示例）：

```json
{
  "ruleId": "rule-kitting",
  "ruleName": "物料齐套校验",
  "passed": false,
  "failedItems": [
    { "materialCode": "MAT-001", "shortage": 50 }
  ],
  "message": "物料 MAT-001 库存不足，缺口 50 件"
}
```

Manifest 中通过 `failurePayloadSchema` 声明字段形状。

### 4.3 指标 `metrics[]`

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | string | |
| `name` | string | 准时完工率 |
| `formula` | string | 表达式 DSL 或自然语言+机器解析 |
| `dataSourceRef` | string | 指向事件或数据源 |
| `dimensions` | string[] | 聚合维度 |
| `period` | enum | `day` \| `week` \| `month` |

平台与 Manifest **不执行计算**，只存元数据（US-B05）。

### 4.4 事务边界 `transactionBoundaries[]`

```yaml
transactionBoundaries:
  - id: tx-release-order
    actionId: action-release-order
    description: 下达工单原子操作
    operations:
      - { type: update_state, objectTypeId: production-order, field: status, value: RELEASED }
      - { type: reserve_material, objectTypeId: material }
      - { type: create_child, objectTypeId: operation }
```

### 4.5 副作用 `sideEffects[]`（可选，US-B07）

与领域事件区分：副作用可异步重试；事件表事实。

---

## 5. 事件层 `spec.events`

### 5.1 领域事件 `domainEvents[]`

| 字段 | 类型 | 必填 | 说明 |
|------|------|:----:|------|
| `id` | string | ✓ | |
| `name` / `nameEn` | string | ✓ | **过去式**，如 `ProductionOrderReleased` |
| `triggerActionId` | string | | 触发行为 |
| `payloadSchema` | object | ✓ | JSON Schema 或简化字段列表 |
| `aggregateRootId` | string | ✓ | |

### 5.2 集成事件 `integrationEvents[]`（US-E05）

面向外部系统的集成契约，可映射到 Kafka/MQ 消息格式。

### 5.3 路由 `routes[]`

```yaml
routes:
  - id: route-order-released
    eventId: evt-order-released
    targets:
      - { boundedContext: 物料管理, system: WMS }
      - { boundedContext: 生产制造, system: MES }
```

### 5.4 处理器 `handlers[]`

```yaml
handlers:
  - id: handler-reserve-material
    routeId: route-order-released
    targetBoundedContext: 物料管理
    actionId: action-reserve-material
    businessScenarioIds: [ scenario-mts ]
```

### 5.5 事件存储 `eventStore`（可选，US-E02）

```yaml
eventStore:
  type: kafka | postgres | redis_stream
  connectionSecretRef: secret/event-store-prod    # 禁止明文
  topicOrTable: manufacturing.domain.events
```

---

## 6. 治理层 `spec.governance`

### 6.1 角色 `roles[]`

```yaml
roles:
  - id: role-planner
    name: 生产计划员
    permissions:
      - { objectTypeId: production-order, ops: [READ, EXECUTE], denyActions: [action-tech-close] }
```

**操作枚举**：`READ` | `WRITE` | `DELETE` | `EXECUTE`

### 6.2 字段权限 `fieldPermissions[]`

```yaml
fieldPermissions:
  - objectTypeId: production-order
    propertyNameEn: cost_price
    allowedRoleIds: [ role-cost-accountant ]
```

Agent Schema 返回时**省略**未授权字段（US-G02）。

### 6.3 Agent 沙箱 `agentPolicies[]`（US-G04）

**MVP 静态 MCP 工具三件套**（须写入 `allowedMcpTools`）：

| 工具 | 用途 | 故事 |
|------|------|------|
| `resolve_intent` | 自然语言 → 概念引用 + `alternatives` | US-A03 Step 1 |
| `query_ontology` | 精确引用 → 完整语义片段 | US-A03 Step 2 |
| `execute_action` | `action` + `parameters` 统一执行 | US-A05 |

每行为独立 Tool（如 `execute_production_order_release`）为 P1（US-A04）；MVP 与 TDD §5.2 采用 `execute_action`。

```yaml
agentPolicies:
  - id: sandbox-prod-assistant
    manifestVersion: "1.0.0"      # 绑定版本
    roleId: role-planner
    allowedMcpTools: [ resolve_intent, query_ontology, execute_action ]
    allowedAggregateRootIds: [ production-order ]
    allowedActionIds: [ action-create-order, action-release-order ]
    rateLimit: { maxCallsPerSecond: 10 }
    defaultDeny: true                 # 最小权限：白名单放行
```

---

## 7. 数据源 `spec.dataSources[]`

**禁止明文密钥**（P0 安全）。

```yaml
dataSources:
  - id: ds-sap-odata
    name: SAP S4 生产订单
    type: api | sql | mcp
    boundObjectTypeId: production-order
    api:
      baseUrl: https://sap.example/odata
      entitySet: ProductionOrders
      authSecretRef: secret/sap-oauth-prod    # 必填：引用密钥 ID
    sql:
      connectionSecretRef: secret/pg-mes-read
      queryTemplate: "SELECT * FROM orders WHERE id = :id"
    mcp:
      server: mes-mcp
      tool: get_production_order
```

| 禁止字段 | 替代 |
|----------|------|
| `password`, `apiKey`, `token`, `clientSecret` | `*SecretRef` 字符串 |

---

## 8. 校验规则（导入时）

平台与 design-time linter **必须**校验：

| 编号 | 规则 |
|------|------|
| V01 | `apiVersion` 受支持 |
| V02 | `metadata.version` 符合 semver |
| V03 | 至少 1 个 `aggregate_root` |
| V04 | 每个 `entity` 有有效 `aggregateRootId` |
| V05 | 每个 `action` 绑定存在的 `aggregateRootId` |
| V06 | `preRuleIds` 引用存在的 `rules` |
| V07 | `publishesEventIds` 引用存在的 `domainEvents` |
| V08 | 领域事件 `nameEn` 建议过去式（警告级） |
| V09 | 状态机有且仅有 1 个 `isInitial: true`（每机） |
| V10 | 无 `secretRef` 以外形式的凭证字段 |
| V11 | 所有 `id` 在文档内唯一 |

失败时返回 US-A01 AC-4 风格错误：`元素类型 + id + 缺失字段`。

---

## 9. 版本与兼容

| 变更类型 | 策略 |
|----------|------|
| 新增可选字段 | 同 `v1` 小版本 |
| 重命名必填字段 | 升 `ontology.platform/v2` |
| 设计台导出 | `metadata.source: ontology-designer` |
| 平台发布 | 写入 `publishedAt`，不可原地覆盖已发布版本 |

**Round-trip 验收（US-A01 AC-3）**

设计台导出 → 平台导入 → 再导出 → 语义层核心对象（上下文、场景、聚合根、行为）无丢失。

---

## 10. 与设计台类型映射（参考）

| Manifest | Ontology `ontology.ts` |
|----------|-------------------------|
| `objectTypes` | `Entity` + `entityRole` |
| `properties` | `Attribute` |
| `stateMachines` | `BehaviorModel.stateMachines` |
| `actions` | `Action` / 行为编辑器 |
| `rules` | `Rule` |
| `domainEvents` | `EventDefinition` |
| `handlers` | `Subscription` |

设计台导出器职责：将 `OntologyProject` 投影为本规范；**不**要求 Manifest 包含 EPC 全文（可 `extensions.epc` 可选）。

---

## 11. 与平台领域映射（参考）

| Manifest | ontology-platform |
|----------|-------------------|
| `metadata` | `Ontology` 聚合根 |
| `objectTypes` | `ObjectType` + `Property` |
| `relations` | `Relation` |
| `actions` | 待建 `ActionType`（US-B01） |
| `governance` | 待建权限/沙箱模块 |

---

## 12. 最小样例

完整制造域样例见：[examples/manufacturing-manifest.yaml](./examples/manufacturing-manifest.yaml)。

```yaml
apiVersion: ontology.platform/v1
kind: OntologyManifest
metadata:
  id: manufacturing-ontology
  version: 0.1.0
  name: 生产制造本体
  boundedContext: 生产制造
  domainTags: [生产制造]
  source: ontology-designer
  status: draft
spec:
  semantic:
    boundedContext: { id: bc-mfg, name: 生产制造, nameEn: Manufacturing }
    businessScenarios:
      - { id: scenario-mts, name: 面向库存生产, nameEn: MTS }
    objectTypes:
      - id: production-order
        name: 生产订单
        nameEn: ProductionOrder
        kind: aggregate_root
        properties:
          - { id: p1, name: 生产订单号, nameEn: order_id, dataType: string, required: true }
  behavior:
    actions: []
    rules: []
  events:
    domainEvents: []
  governance:
    roles: []
  dataSources: []
```

---

## 13. 变更记录

| 日期 | 版本 | 说明 |
|------|------|------|
| 2026-06-04 | draft | 初稿，对齐 UserStoryMap v1.2 与双产品边界 |
