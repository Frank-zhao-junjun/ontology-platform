# OntologyManifest 规范 v2

> **apiVersion**: `ontology.platform/v2`  
> **状态**: Draft（Spec-first，待与项目1 联合评审）  
> **日期**: 2026-06-15  
> **对齐策略**: **方案 A** — `spec.project` 与 `OntologyProject` **字段名 1:1**（`ontology.ts` 为唯一权威）  
> **对齐来源**: 项目1 `D:\AI\Ontology\src\types\ontology.ts`  
> **前置**: [`ontology-manifest-spec.md`](./ontology-manifest-spec.md)（v1，Legacy）  
> **平台实施**: [`phase3-spec-v1.md`](../superpowers/specs/phase3-spec-v1.md)

---

## 1. 目的与范围

v2 交换契约用于 **项目1（建模工具）→ 项目2（治理平台 + MCP）** 的正式对接。

**核心原则**：设计台可将 `OntologyProject` **原样嵌入** `spec.project`，项目2 **无需键名映射**即可解析。

| 能力 | v1 | v2 |
|------|:--:|:--:|
| `OntologyProject` 1:1 嵌入 | ❌ | ✅ |
| 业务场景 `businessScenarioId` | ❌ | ✅ |
| 实体角色 `child_entity` | ❌ | ✅ |
| Organization / Lifecycle / Semantic | ❌ | ✅ |
| 分层校验 VE/VM/VX/V-LC/V-AS | ❌ | ✅ |

**非范围**：`ReferenceDocument` 原文、HR 同步运行时、Zustand 草稿编辑态。

---

## 2. 文档顶层结构

### 2.1 Envelope + Project（方案 A）

```yaml
apiVersion: ontology.platform/v2
kind: OntologyExchange
metadata:                           # 交换包元信息（平台用，非 OntologyProject 字段）
  id: manufacturing-ontology        # 交换包 ID（可与 project.id 不同）
  version: "1.0.0"                  # 与 ProjectVersion.version / 发布 semver 对齐
  name: 生产制造本体
  displayName: Manufacturing Ontology
  description: ...
  source: ontology-designer         # ontology-designer | excel_import | api
  status: draft                     # draft | pending_review | published | rejected | archived
  projectId: proj-xxx               # 必须 === spec.project.id
  exportedAt: "2026-06-15T10:00:00Z"
  exporterVersion: "2.0.0"
spec:
  project: OntologyProject          # §3 — 与 ontology.ts 完全一致
  extensions: { ... }               # §4 — 可选，OntologyProject 外的全局数据
  epc: { ... }                      # §5 — EpcChain 目标态（可选）
  lifecycle: { ... }                # §6 — 编译聚合（可选）
  validation: { ... }               # §7 — 校验报告（可选）
```

JSON / YAML 等价。设计台导出逻辑：

```typescript
// 伪代码 — 零字段改名
const envelope: OntologyExchange = {
  apiVersion: 'ontology.platform/v2',
  kind: 'OntologyExchange',
  metadata: { /* 交换元信息 */, projectId: project.id },
  spec: {
    project: useOntologyStore.getState().project,  // OntologyProject 原样
    extensions: { metadataList },                   // 若需导出全局元数据
  },
};
```

### 2.2 与 v1 路径对照

| v1 | v2（方案 A） |
|----|-------------|
| `spec.semantic.objectTypes[]` | `spec.project.dataModel.entities[]` |
| `spec.behavior.actions` | `spec.project.behaviorModel.actions` |
| `spec.events.domainEvents` | `spec.project.eventModel.events` |
| `spec.governance` | `spec.project.governanceModel` |
| `spec.dataSources` | `spec.project.dataSourcesModel.sources` |
| Agent 语义（无） | `spec.project.agentSemanticLayer` |

---

## 3. spec.project — OntologyProject（1:1）

**权威定义**：`D:\AI\Ontology\src\types\ontology.ts` → `export interface OntologyProject`

```typescript
interface OntologyProject {
  id: string;
  name: string;
  description?: string;
  domain: Domain;                              // { id, name, nameEn, description?, icon?, color? }
  dataModel: DataModel | null;
  behaviorModel: BehaviorModel | null;
  ruleModel: RuleModel | null;
  processModel: ProcessModel | null;
  eventModel: EventModel | null;
  epcModel?: EpcModel | null;
  governanceModel?: GovernanceModel | null;
  dataSourcesModel?: DataSourcesModel | null;
  metricsModel?: MetricsModel | null;
  organizationModel?: OrganizationModel | null;
  agentSemanticLayer?: AgentSemanticLayer | null;
  createdAt: string;
  updatedAt: string;
}
```

> **禁止** 使用缩短键名（如 `data` / `behavior` / `rules`）。旧版 Spec 草案中的 `spec.models.*` 已废弃。

### 3.1 各模型容器字段名（必须与 ontology.ts 一致）

| 字段 | 类型 | 说明 |
|------|------|------|
| `dataModel` | `DataModel` | `projects`, `businessScenarios`, `entities` |
| `behaviorModel` | `BehaviorModel` | `stateMachines`, `actions`, `functions`, `transactionBoundaries`, … |
| `ruleModel` | `RuleModel` | `rules[]` |
| `processModel` | `ProcessModel` | `orchestrations[]` |
| `eventModel` | `EventModel` | `events[]`, `subscriptions[]` |
| `epcModel` | `EpcModel` | `profiles[]`（当前代码） |
| `governanceModel` | `GovernanceModel` | `roles`, `fieldPermissions`, `agentPolicies` |
| `dataSourcesModel` | `DataSourcesModel` | `sources[]` |
| `metricsModel` | `MetricsModel` | `metrics[]` |
| `organizationModel` | `OrganizationModel` | `departments`, `positions` |
| `agentSemanticLayer` | `AgentSemanticLayer` | `intents`, `businessTerms`, … |

### 3.2 关键内部字段（易错点）

| 类型 | 正确字段 | 错误写法 |
|------|----------|----------|
| Entity | `entityRole`, `parentAggregateId`, `businessScenarioId`, `projectId` | v1 `kind`, `aggregateRootId` |
| Attribute | `dataType`, `metadataTemplateId`, `referenceKind`, `masterDataType` | — |
| Relation | `targetEntity`, `type: one_to_one\|one_to_many\|many_to_many` | v1 `targetObjectTypeId` |
| StateMachine | `entity`（实体 id）, `statusField` | — |
| Transition | `guardCondition`（**表达式 string**）, `preConditions`（规则 id 数组） | 勿把 rule id 写入 guardCondition |
| Action | `targetEntityId`, `actionType` | v1 `aggregateRootId` |
| Rule | `entity`, `condition: RuleCondition` | — |
| EventDefinition | `entity`, `payload[]`, `payloadFields[]`, `isDomainEvent` | v1 `aggregateRootId` |
| GovernanceRole | `permissions[].objectTypeId`, `ops` | — |
| GovernanceAgentPolicy | `roleId`, `allowedMcpTools` | 区别于 Semantic `AgentPolicy` |
| IntentSlot | `examples: string[]`（必填） | 不可省略 |

### 3.3 Semantic 与 Governance 两套 Policy

| 位置 | 类型 | 用途 |
|------|------|------|
| `project.governanceModel.agentPolicies` | `GovernanceAgentPolicy` | MCP 工具白名单 |
| `project.agentSemanticLayer.agentPolicies` | `AgentPolicy` | Agent 行为边界（allow/deny/confirm） |

---

## 4. spec.extensions — OntologyProject 外可选数据

设计台 Zustand 中 **不在** `OntologyProject` 内、但导入平台时需要的数据：

```typescript
spec.extensions?: {
  /** 全局元数据模板（store.metadataList） */
  metadataList?: Metadata[];
  /** 主数据定义 + 可选实例（ProjectVersion.metamodels.masterData） */
  masterData?: {
    definitions: MasterData[];
    records?: Record<string, MasterDataRecord[]>;
  };
}
```

未提供时平台仅依赖 `project` 内引用（如 `Attribute.metadataTemplateId`）做延迟校验。

---

## 5. spec.epc — EpcChain 目标态（可选）

当前 `project.epcModel` 为 `EpcModel.profiles[]`（`ontology.ts` 已有）。  
EPC-Upgrade-Spec v3.1 的 `EpcChain` / `EpcNode` / `EpcEdge` **尚未写入 ontology.ts** 时，可放扩展块：

```yaml
spec:
  epc:
    format: chain
    chains:
      - id: chain-po-release
        aggregateRootId: production-order
        nodes: [...]
        edges: [...]
```

类型落地后，可迁入 `project.epcModel` 或新增 `project.epcChains`（需项目1 先改 `ontology.ts`）。

---

## 6. spec.lifecycle — 实体生命周期聚合（可选）

可由平台自 `behaviorModel` + `ruleModel` + `eventModel` + `governanceModel` **编译**；结构与 `EntityLifecycle` 一致：

```yaml
spec:
  lifecycle:
    byEntityId:
      production-order:
        entityId: production-order
        entityNameEn: ProductionOrder
        statusField: status
        stateMachine: { ... }
        actionsByState: { ... }
        rulesByState: { ... }
        eventsByState: { ... }
        rolesByState: { ... }
        auditTrail: []
        stats: { ... }
```

同项目1 `GET /api/entity-lifecycle?entityId=` 响应。

---

## 7. spec.validation — 校验报告（可选）

```yaml
spec:
  validation:
    isValid: false
    validatedAt: "2026-06-15T10:00:00Z"
    summary: { errors: 2, warnings: 5 }
    issues:
      - { code: V04, severity: error, elementType: entity, id: operation, field: parentAggregateId, message: "..." }
```

校验分层：V01–V11、VE×17、VM×39、VX×15、V-LC×15、V-AS×15、V-XL-O×23、VM-HR×4（见 Phase 3 Spec）。

---

## 8. 交换形态与 API

### 8.1 项目1 导出

```typescript
// 推荐：直接序列化 project
POST /api/export/exchange
Body: OntologyExchange { spec: { project: ontologyProject } }
```

| 形态 | 说明 |
|------|------|
| 全量 JSON | `OntologyExchange`，`spec.project` = `OntologyProject` |
| Excel | `ExcelParsedData` → 平台编译为 `OntologyProject` 再入库 |
| 裸 Project | 仅 `OntologyProject` JSON 也可被接受（平台自动包 envelope） |

### 8.2 项目2 导入

| 方法 | 端点 |
|------|------|
| POST | `/api/v2/exchanges/import` |
| POST | `/api/v2/exchanges/import/excel` |
| POST | `/api/v2/exchanges/{draftId}/publish` |

v1 `/api/v1/manifests/import` 经 `ManifestUpcaster` → 编译为 `spec.project` 结构。

---

## 9. v1 Manifest → spec.project（Upcast）

| v1 | v2 `spec.project` |
|----|-------------------|
| `spec.semantic.objectTypes[].kind: entity` | `dataModel.entities[].entityRole: child_entity` |
| `spec.semantic.objectTypes[].aggregateRootId` | `dataModel.entities[].parentAggregateId` |
| `spec.behavior.actions[].aggregateRootId` | `behaviorModel.actions[].targetEntityId` |
| `spec.events.domainEvents` | `eventModel.events` |
| `spec.governance` | `governanceModel` |

Upcast 输出必须符合 §3 `OntologyProject` 字段名。

---

## 10. 版本与兼容

| 规则 | 说明 |
|------|------|
| apiVersion | 仅 `ontology.platform/v2` |
| 字段权威 | `ontology.ts` 新增字段自动允许（向前兼容） |
| 未知字段 | 平台 JSONB 原样保留 |
| metadata.projectId | 必须等于 `spec.project.id` |

---

## 11. 分阶段落地

| 阶段 | 读取路径 |
|:----:|----------|
| 3a | `project.dataModel`, `behaviorModel`, `ruleModel`, `eventModel`, `governanceModel` |
| 3b | `organizationModel`, `metricsModel`, `dataSourcesModel`, `extensions.metadataList` |
| 3c | `project.agentSemanticLayer`, `spec.lifecycle` |
| 3d | `epcModel` + `spec.epc.chains` |

---

## 12. 样例

| 资产 | 路径 |
|------|------|
| v2 JSON（方案 A） | [`examples/manufacturing-exchange-v2.json`](./examples/manufacturing-exchange-v2.json) |
| v1 Legacy | [`examples/manufacturing-manifest.yaml`](./examples/manufacturing-manifest.yaml) |
| 类型权威 | 项目1 `src/types/ontology.ts` |

---

## 13. 相关文档

- [项目1-项目2对接差距分析.md](./项目1-项目2对接差距分析.md)
- [phase3-spec-v1.md](../superpowers/specs/phase3-spec-v1.md)
