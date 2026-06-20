# P2-I04: 导入管道完整链路分析与格式映射

| 项目 | 内容 |
|:----|:-----|
| **文档ID** | P2-I04 |
| **最后更新** | 2026-06-20 |
| **分析范围** | ManifestConverter → ManifestValidator → ManifestService → ManifestDocument 全链路 + Excel 导入适配器 |
| **目标** | 描述 Project 1 导出格式到 ManifestDocument 的字段映射、导入管道链路图、必要最小字段清单、E2E 场景数据准备 |

---

## 目录

1. [导入管道全景图](#1-导入管道全景图)
2. [路径 A: Manifest JSON/YAML 直接导入](#2-路径-a-manifest-jsonyaml-直接导入)
3. [路径 B: ManifestConverter 适配 (Project 1 简化 JSON → ManifestDocument)](#3-路径-b-manifestconverter-适配)
4. [路径 C: Excel 导入 (xlsx → OntologyExchangeDocument)](#4-路径-c-excel-导入)
5. [ManifestDocument 目标模型完整结构](#5-manifestdocument-目标模型完整结构)
6. [Validator 验证规则详解](#6-validator-验证规则详解)
7. [必要最小字段清单](#7-必要最小字段清单)
8. [E2E 场景数据准备](#8-e2e-场景数据准备)
9. [字段映射对照表 (Project 1 格式 → ManifestDocument)](#9-字段映射对照表)
10. [Excel Sheets → OntologyExchangeDocument 映射](#10-excel-sheets--ontologyexchangedocument-映射)

---

## 1. 导入管道全景图

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          Project 1 (导出源)                             │
│                                                                         │
│  ┌─────────────────┐  ┌──────────────────┐  ┌───────────────────────┐  │
│  │ YAML Manifest   │  │ JSON Manifest    │  │ Excel (.xlsx)         │  │
│  │ manufacturing-  │  │ (K8s-style JSON) │  │ Sheets A/B/C/EPC/E1~E8│  │
│  │ manifest.yaml   │  │                  │  │                       │  │
│  └────────┬────────┘  └────────┬─────────┘  └───────────┬───────────┘  │
│           │                    │                         │              │
└───────────┼────────────────────┼─────────────────────────┼──────────────┘
            │                    │                         │
            ▼                    ▼                         ▼
┌───────────────────────┐ ┌────────────────────┐ ┌──────────────────────┐
│ P2-I03-U01            │ │ ManifestConverter  │ │ ExcelExchangeMapper  │
│ YAML 解析器           │ │ (P2-I02-U01)       │ │ (P2-I01-U01~U05)    │
│ (SnakeYAML → DTO)     │ │                    │ │                     │
│                       │ │ Project1 JSON →    │ │ Sheet A→ExcelOntology│
│ YAML DTO →            │ │ ManifestDocument   │ │   ImportAdapter     │
│ ManifestDocument      │ │ 字段级映射转换     │ │ Sheet B→ExcelBImport │
│ (通过 Jackson)        │ │                    │ │   Adapter           │
│                       │ │ 特殊处理:          │ │ Sheet C→ExcelCImport │
│                       │ │ - semantic.state-  │ │   Adapter           │
│                       │ │   Machines 合并入   │ │ Sheet EPC→ExcelEpc  │
│                       │ │   behavior         │ │   ImportAdapter     │
│                       │ │ - spec.process →   │ │ Sheet E1~E8→ExcelE  │
│                       │ │   spec.epc         │ │   DimImportAdapter  │
│                       │ │                    │ │                     │
└──────────┬────────────┘ └────────┬───────────┘ └──────────┬───────────┘
           │                      │                         │
           └──────────────────────┼─────────────────────────┘
                                  │
                                  ▼
           ┌──────────────────────────────────────────┐
           │          ManifestController              │
           │    POST /api/v1/manifests/import         │
           │                                          │
           │  接收: ImportManifestRequest             │
           │    { sourceFormat, rawContent, createdBy }│
           └──────────────────┬───────────────────────┘
                              │
                              ▼
           ┌──────────────────────────────────────────┐
           │         ManifestServiceImpl              │
           │                                          │
           │  Step 1: Jackson 反序列化 rawContent      │
           │     → ManifestDocument                    │
           │                                          │
           │  Step 2: ManifestValidator.validate()     │
           │     11 条规则 (V00-V11)                   │
           │     ↓ valid → 进入 DRAFT                  │
           │     ↓ invalid → 返回错误列表              │
           │                                          │
           │  Step 3: 存入 importStore (内存 Map)      │
           │     状态: DRAFT                           │
           │                                          │
           │  Step 4: 返回 ImportManifestResponse      │
           │     { draftId, externalId, counts }       │
           └──────────────────┬───────────────────────┘
                              │
                              ▼
     ┌─────────────────────────────────────────────────────────┐
     │                    后续生命周期                          │
     │                                                         │
     │  POST /{id}/preview  → 比较差异 (当前版本 vs 上次发布)   │
     │  POST /{id}/publish  → DRAFT → PUBLISHED, 写入 version  │
     │  GET  /{id}/export   → 从已发布版本导出 ManifestDocument │
     └─────────────────────────────────────────────────────────┘
```

---

## 2. 路径 A: Manifest JSON/YAML 直接导入

### 入口
```
POST /api/v1/manifests/import
Content-Type: application/json

{
  "sourceFormat": "JSON",       // 或 "YAML"
  "rawContent": "{...}",        // ManifestDocument 期望的完整 JSON/YAML 字符串
  "createdBy": "admin"
}
```

### 处理流程
```
ImportManifestRequest.rawContent
       │
       ▼
Jackson ObjectMapper.readValue(rawContent, ManifestDocument.class)
       │
       ▼
ManifestValidator.validate(doc)  ← 11 条验证规则
       │
  ┌────┴────┐
  │         │
  valid   invalid
  │         │
  ▼         ▼
生成 importId      返回 { valid: false, errors: [...] }
存入 importStore
返回 { valid: true, draftId, externalId, importedCounts }
```

### 注意
- `rawContent` 必须是 **ManifestDocument 结构的完整 JSON/YAML**，即 Project 2 内部格式
- 不支持直接输入 Project 1 的老格式 JSON — 需要使用 ManifestConverter 或 YAML 解析器先转换

---

## 3. 路径 B: ManifestConverter 适配

**类**: `ManifestConverter.java`
**位置**: `ontology-application/.../service/manifest/ManifestConverter.java`
**用途**: 将 Project 1 的简化模型 JSON 转换成 ManifestDocument

### 输入格式 (Project 1 OntologyManifest JSON)

```json
{
  "apiVersion": "ontology.platform/v1",
  "kind": "OntologyManifest",
  "metadata": {
    "id": "manufacturing-ontology",
    "name": "生产制造本体",
    "version": "0.1.0",
    "description": "制造域参考模型"
  },
  "spec": {
    "semantic": {
      "objectTypes": [
        {
          "id": "po",
          "name": "生产订单",
          "nameEn": "ProductionOrder",
          "kind": "aggregate_root",
          "description": "生产订单聚合根"
        }
      ],
      "stateMachines": [
        {
          "id": "sm-po",
          "name": "生产订单状态机",
          "states": [
            { "name": "created", "isInitial": true, "isFinal": false },
            { "name": "in_progress", "isInitial": false, "isFinal": false },
            { "name": "completed", "isInitial": false, "isFinal": true }
          ],
          "transitions": [
            { "from": "created", "to": "in_progress", "trigger": "start" },
            { "from": "in_progress", "to": "completed", "trigger": "finish" }
          ]
        }
      ]
    },
    "behavior": {
      "actions": [
        {
          "id": "create-po",
          "name": "创建生产订单",
          "nameEn": "CreatePO",
          "description": "创建新生产订单"
        }
      ],
      "rules": []
    },
    "events": {
      "domainEvents": [
        {
          "id": "po-created",
          "name": "生产订单已创建",
          "nameEn": "POCreated"
        }
      ]
    },
    "governance": {
      "roles": [
        { "id": "planner", "name": "计划员" }
      ]
    },
    "process": {
      "orchestrations": [
        {
          "id": "orc-po",
          "name": "生产订单流程",
          "steps": [
            { "actionId": "create-po", "type": "manual" }
          ]
        }
      ]
    },
    "dataSources": [
      { "id": "ds-erp", "name": "ERP系统", "sourceType": "REST_API" }
    ]
  }
}
```

### ManifestConverter 转换逻辑 (逐字段)

| Project 1 JSON 路径 | ManifestDocument 路径 | 转换方式 | 说明 |
|:--------------------|:---------------------|:---------|:-----|
| `apiVersion` | `apiVersion` | 原值传递 | |
| `kind` | `kind` | 原值传递 | |
| `metadata.id` | `metadata.id` | 原值传递 | |
| `metadata.name` | `metadata.name` | 原值传递 | |
| `metadata.name` | `metadata.displayName` | **取 metadata.name** | 复用 name 作为 displayName |
| `metadata.version` | `metadata.version` | 原值传递 | |
| `metadata.description` | `metadata.description` | 原值传递 | |
| `spec.semantic.objectTypes[].id` | `spec.semantic.objectTypes[].id` | 原值传递 | |
| `spec.semantic.objectTypes[].name` | `spec.semantic.objectTypes[].name` | 原值传递 | |
| `spec.semantic.objectTypes[].nameEn` | `spec.semantic.objectTypes[].nameEn` | 原值传递 | |
| `spec.semantic.objectTypes[].kind` | `spec.semantic.objectTypes[].kind` | 原值传递 | |
| `spec.semantic.objectTypes[].description` | `spec.semantic.objectTypes[].description` | 原值传递 | |
| `spec.semantic.stateMachines[]` | → 合并入 `spec.behavior.stateMachines` | 从 semantic 提取后追加到 behavior.stateMachines | **重要: Project 1 状态机在 semantic 下, Project 2 在 behavior 下** |
| `spec.behavior.actions[].id` | `spec.behavior.actions[].id` | 原值传递 | |
| `spec.behavior.actions[].name` | `spec.behavior.actions[].name` | 原值传递 | |
| `spec.behavior.actions[].nameEn` | `spec.behavior.actions[].nameEn` | 原值传递 | |
| `spec.behavior.actions[].description` | `spec.behavior.actions[].description` | 原值传递 | |
| `spec.behavior.rules[].id` | `spec.behavior.rules[].id` | 原值传递 | |
| `spec.behavior.rules[].name` | `spec.behavior.rules[].name` | 原值传递 | |
| `spec.behavior.rules[].description` | `spec.behavior.rules[].description` | 原值传递 | |
| `spec.behavior.stateMachines[]` | `spec.behavior.stateMachines[]` | 原值传递 (如果 behavior 下也有) | |
| `spec.events.domainEvents[].id` | `spec.events.domainEvents[].id` | 原值传递 | |
| `spec.events.domainEvents[].name` | `spec.events.domainEvents[].name` | 原值传递 | |
| `spec.events.domainEvents[].nameEn` | `spec.events.domainEvents[].nameEn` | 原值传递 | |
| `spec.governance.roles[].id` | `spec.governance.roles[].id` | 原值传递 | |
| `spec.governance.roles[].name` | `spec.governance.roles[].name` | 原值传递 | |
| `spec.process.orchestrations[].id` | `spec.epc[].id` | 映射 process→epc | **命名差异: Project 1 用 "process", Project 2 用 "epc"** |
| `spec.process.orchestrations[].name` | `spec.epc[].flowName` | 映射 name→flowName | |
| `spec.process.orchestrations[].steps[].actionId` | `spec.epc[].steps[].actionId` | 原值传递 | |
| `spec.process.orchestrations[].steps[].type` | `spec.epc[].steps[].conditions` | 包装到 List 中 | step.type → conditions[0] |
| — | `spec.epc[].steps[].stepOrder` | **自动生成** | 从 0 开始递增 |
| `spec.dataSources[].id` | `spec.dataSources[].id` | 原值传递 | |
| `spec.dataSources[].name` | `spec.dataSources[].name` | 原值传递 | |
| `spec.dataSources[].sourceType` | `spec.dataSources[].sourceType` | 原值传递 | |

> **状态机合并细节**: ManifestConverter 会从 `spec.semantic.stateMachines` 中提取状态机列表，然后合并到 `spec.behavior.stateMachines` 中。如果 behavior 已经存在，则在原有 stateMachines 列表后追加；如果 behavior 不存在，则创建一个只含 stateMachines 的 Behavior 对象。

---

## 4. 路径 C: Excel 导入

### 架构

```
.xlsx (Project 1 导出的 Excel 文件)
  │
  ├── Sheet "A" (ValueDomain)              → ExcelOntologyImportAdapter    → Ontology 实体
  ├── Sheet "B" (Capability)               → ExcelBImportAdapter          → ObjectType (aggregate_root)
  ├── Sheet "C" (Scenario)                 → ExcelCImportAdapter          → ObjectType (child_entity)
  ├── Sheet "EPC" (EpcProcess)             → ExcelEpcImportAdapter        → EpcParsedRow
  └── Sheet "E1"~"E8" (Dimension Elements)  → ExcelEDimImportAdapter       → ExcelEDimRow
       │
       ▼
  ExcelExchangeMapper.mapFromXlsx()
       │
       ▼
  OntologyExchangeDocument (v2 交换格式)
       │
       ▼
  [可序列化为 JSON 后走 ManifestController /import 管道]
```

### Sheet A 列定义 (ExcelOntologyImportAdapter)

| 列 | 索引 | 字段名 | 必填 | 映射目标 |
|:--|:----|:-------|:----|:---------|
| A | 0 | ID | ✅ | Ontology.name |
| B | 1 | 名称 | ✅ | Ontology.displayName |
| C | 2 | 英文名 | ❌ | — |
| D | 3 | 描述 | ❌ | Ontology.description |
| E | 4 | 语义(JSON) | ❌ | JSON 格式校验后暂存 |

### Sheet B 列定义 (ExcelBImportAdapter)

| 列 | 索引 | 字段名 | 必填 | 映射目标 |
|:--|:----|:-------|:----|:---------|
| A | 0 | ID | ✅ | ObjectType.id |
| B | 1 | 名称 | ✅ | ObjectType.name |
| C | 2 | 英文名 | ❌ | — |
| D | 3 | 描述 | ❌ | ObjectType.description |
| E | 4 | 语义(JSON) | ❌ | JSON 格式校验 |
| F | 5 | 父节点ID | ✅ | ObjectType.parentId |

### Sheet C 列定义 (ExcelCImportAdapter)

与 Sheet B 完全相同: ID | 名称 | 英文名 | 描述 | 语义(JSON) | 父节点ID

### Sheet EPC 列定义 (ExcelEpcImportAdapter)

| 列 | 索引 | 字段名 | 必填 | 映射目标 |
|:--|:----|:-------|:----|:---------|
| A | 0 | ID | ✅ | EpcParsedRow.epcId |
| B | 1 | 名称 | ✅ | EpcParsedRow.flowName |
| C | 2 | 英文名 | ❌ | — |
| D | 3 | 描述 | ❌ | EpcParsedRow.description |
| E | 4 | 语义(JSON) | ❌ | JSON 格式校验 |
| F | 5 | 父节点ID | ✅ | EpcParsedRow.parentId |
| G | 6 | 归属场景ID | ✅ | EpcParsedRow.scenarioId |
| H | 7 | 步骤(JSON) | ✅ | EpcParsedRow.steps (解析为 List\<EpcStepItem\>) |

步骤 JSON 格式:
```json
[
  {
    "id": "s1",
    "name": "下达",
    "elementRef": {
      "dimension": "E2",
      "elementId": "ACT-001",
      "versionPin": "latest_confirmed"
    }
  }
]
```

### Sheet E1~E8 列定义 (ExcelEDimImportAdapter)

| 列 | 索引 | 字段名 | 必填 | 映射目标 |
|:--|:----|:-------|:----|:---------|
| A | 0 | ID | ✅ | ExcelEDimRow.elementId |
| B | 1 | 名称 | ✅ | ExcelEDimRow.name |
| C | 2 | 英文名 | ❌ | ExcelEDimRow.nameEn |
| D | 3 | 维度 | ✅ | ExcelEDimRow.dimension (E1~E8) |
| E | 4 | 可见性 | ❌ | ExcelEDimRow.visibility (project/domain_scoped/private_draft) |
| F | 5 | 描述 | ❌ | ExcelEDimRow.description |

### ExcelExchangeMapper 整合逻辑

```
ExcelExchangeMapper.mapFromXlsx(xlsxStream, externalId)
  │
  ├── 1. Sheet A 解析 → 获取 projectId/projectName/description
  │
  ├── 2. Sheet B 解析 → 每个 ObjectType → OntologyExchangeDocument.Entity(entityRole="aggregate_root")
  │
  ├── 3. Sheet C 解析 → 每个 ObjectType → OntologyExchangeDocument.Entity(entityRole="child_entity", parentAggregateId)
  │
  └── 4. 构建 OntologyExchangeDocument 信封:
         apiVersion: "ontology.platform/v2"
         kind: "OntologyExchange"
         metadata: { id, version: "0.1.0", name, description, source: "excel-import", status: "draft", projectId }
         spec.project: { id, name, description, dataModel: { id, name, version, entities } }
```

---

## 5. ManifestDocument 目标模型完整结构

```java
ManifestDocument {
    String apiVersion;         // = "ontology.platform/v1"
    String kind;               // = "OntologyManifest"
    Metadata metadata;
    Spec spec;
}

Metadata {
    String id;                  // 本体唯一标识
    String version;             // 语义化版本 (x.y.z)
    String name;                // 名称
    String displayName;         // 显示名称
    String description;         // 描述
    String boundedContext;      // 限界上下文
    List<String> domainTags;    // 领域标签
    String compiledAt;          // 编译时间
    String source;              // 来源
    String status;              // 状态
}

Spec {
    Semantic semantic;
    Behavior behavior;
    Events events;
    Governance governance;
    List<DataSource> dataSources;
    List<Epc> epc;
}

Semantic {
    BoundedContext boundedContext;
    List<BusinessScenario> businessScenarios;
    List<ValueObject> valueObjects;
    List<ObjectType> objectTypes;
}

ObjectType {
    String id;
    String name;
    String nameEn;
    String kind;                 // "aggregate_root" | "entity" | "value_object"
    List<String> businessScenarioIds;
    String description;
    String aggregateRootId;
    List<PropertyDef> properties;
}

PropertyDef {
    String id; String name; String nameEn;
    String dataType; Boolean required;
    List<String> enumValues; String description;
    Boolean isPrimary; Boolean isState;
}

Behavior {
    List<ActionDef> actions;
    List<RuleDef> rules;
    List<StateMachineDef> stateMachines;
}

ActionDef {
    String id; String name; String nameEn;
    String aggregateRootId; String description;
    Map<String, Object> inputSchema;
    List<String> preRuleIds; List<String> postRuleIds;
    List<String> publishesEventIds;
    String domain; String riskLevel;
}

StateMachineDef {
    String id; String name; String aggregateRootId;
    String entityId; List<StateDef> states; List<TransitionDef> transitions;
}

StateDef { String name; boolean isInitial; boolean isFinal; }
TransitionDef { String from; String to; String trigger; String guard; }

Events {
    List<EventDef> domainEvents;
    List<CausalityDef> causalities;
}

EventDef {
    String id; String name; String nameEn;
    String eventType; String severity; String aggregateRootId;
    Map<String, Object> payloadSchema;
}

CausalityDef {
    String id; String causeEventId; String effectEventId; String description;
}

Governance {
    List<RoleDef> roles;
    List<AgentPolicyDef> agentPolicies;
}

RoleDef { String id; String name; String code; List<String> permissions; }
AgentPolicyDef { String id; String name; String agentRoleId; List<String> allowedTools; }

DataSource {
    String id; String name; String code;
    String sourceType; Map<String, Object> connectionConfig; String credentialRef;
}

Epc {
    String id; String flowName; List<EpcStepDef> steps;
}

EpcStepDef {
    Integer stepOrder; String triggerEventId;
    String actionId; List<String> conditions; List<String> guards;
}
```

---

## 6. Validator 验证规则详解

**类**: `ManifestValidator.java`
**位置**: `ontology-application/.../service/manifest/ManifestValidator.java`

| 编码 | 规则 | 检查对象 | 详细逻辑 | 严重性 |
|:----|:-----|:---------|:---------|:-------|
| **V00** | 非空 | doc | `if doc == null` → error | ERROR |
| **V01** | apiVersion | doc.apiVersion | 必须等于 `"ontology.platform/v1"` | ERROR |
| **V02** | 语义化版本 | metadata.version | 必须匹配正则 `^\d+\.\d+\.\d+$` | ERROR |
| **V03** | 至少一个聚合根 | objectTypes[].kind | 至少有 1 个 objectType 的 kind == `"aggregate_root"` | ERROR |
| **V04** | 实体引用有效 | objectType.aggregateRootId | aggregateRootId 必须引用已存在的 objectType id | ERROR |
| **V05** | Action 引用有效 | action.aggregateRootId | action.aggregateRootId 必须引用已存在的 objectType id | ERROR |
| **V06** | 规则引用有效 | action.preRuleIds | preRuleIds 中的每个 id 必须在 rules 中存在 | ERROR |
| **V07** | 事件引用有效 | action.publishesEventIds | publishesEventIds 中的每个 id 必须在 domainEvents 中存在 | ERROR |
| **V08** | 事件名时态 | event.nameEn | nameEn 应以 "ed" 或 "d" 结尾（过去时） | **WARNING** |
| **V09** | 状态机初始状态 | stateMachine.states | 每个 stateMachine 必须有**恰好 1 个** `isInitial=true` | ERROR |
| **V10** | 明文凭据禁止 | dataSource.connectionConfig | connectionConfig.toString() 不得包含 "password"/"apikey"/"secret" | ERROR |
| **V11** | ID 全局唯一 | objectTypes/actions/events/rules/stateMachines | 所有元素的 id 在各自集合内不得重复（5 组分别检查） | ERROR |

### 验证通过的「最小合法 Manifest」示例 (来自 ManifestValidatorTest.validDoc)

```json
{
  "apiVersion": "ontology.platform/v1",
  "metadata": {
    "id": "test",
    "version": "0.1.0"
  },
  "spec": {
    "semantic": {
      "objectTypes": [
        { "id": "po", "kind": "aggregate_root", "name": "PO" },
        { "id": "item", "kind": "entity", "aggregateRootId": "po", "name": "Item" }
      ]
    }
  }
}
```

---

## 7. 必要最小字段清单

### 通过 Validator 的「绝对最小」ManifestDocument

| 层级 | 字段 | 值要求 | 说明 |
|:----|:-----|:-------|:-----|
| root | `apiVersion` | `"ontology.platform/v1"` | 硬编码字符串 |
| metadata | `id` | 非 null 字符串 | 本体标识 |
| metadata | `version` | 匹配 `^\d+\.\d+\.\d+$` | 语义化版本 |
| spec.semantic.objectTypes[] | `至少 1 个` | kind=`"aggregate_root"` | 必须有聚合根 |
| — | `id` | 全局唯一 | 所有 id 不重复 |
| — | `kind` | 字符串 | 至少一个为 `"aggregate_root"` |

### 如果包含可选扩展，各模块的最低要求

| 模块 | 最低要求 |
|:----|:---------|
| **behavior.actions** | 如果引入：必须指定 `id`，`aggregateRootId` 必须引用已存在的 objectType id，`preRuleIds`/`publishesEventIds` 必须引用已存在的 rule/event id |
| **behavior.rules** | 如果引入：必须指定 `id`，供 actions.preRuleIds 引用 |
| **behavior.stateMachines** | 如果引入：必须有恰好 1 个 `isInitial=true` 的 state |
| **events.domainEvents** | 如果引入：必须指定 `id`，供 actions.publishesEventIds 引用；nameEn 最好以 "ed"/"d" 结尾（仅警告） |
| **governance.roles** | 无强制验证要求 |
| **governance.agentPolicies** | 无强制验证要求 |
| **dataSources** | 如果引入：connectionConfig 不能含明文密码/API Key/Secret |
| **epc** | 无强制验证要求 |

---

## 8. E2E 场景数据准备

### 场景 1: 最小 JSON Manifest 直接导入

**用途**: 通过 `/api/v1/manifests/import` 导入最小验证通过的数据

```json
// rawContent 的内容
{
  "apiVersion": "ontology.platform/v1",
  "kind": "OntologyManifest",
  "metadata": {
    "id": "e2e-minimal",
    "version": "0.1.0",
    "name": "最小测试本体",
    "displayName": "最小测试本体"
  },
  "spec": {
    "semantic": {
      "objectTypes": [
        {
          "id": "order",
          "name": "订单",
          "nameEn": "Order",
          "kind": "aggregate_root",
          "description": "订单聚合根"
        },
        {
          "id": "order-item",
          "name": "订单项",
          "nameEn": "OrderItem",
          "kind": "entity",
          "aggregateRootId": "order",
          "description": "订单行项目"
        }
      ]
    }
  }
}
```

### 场景 2: 完整 JSON Manifest (含所有模块)

**用途**: 测试全量字段映射和所有验证规则覆盖

```json
{
  "apiVersion": "ontology.platform/v1",
  "kind": "OntologyManifest",
  "metadata": {
    "id": "e2e-full",
    "version": "1.0.0",
    "name": "完整测试本体",
    "displayName": "完整测试本体",
    "description": "包含所有模块的完整测试用例",
    "boundedContext": "制造执行",
    "domainTags": ["生产", "质量"],
    "status": "draft"
  },
  "spec": {
    "semantic": {
      "objectTypes": [
        {
          "id": "po",
          "name": "生产订单",
          "nameEn": "ProductionOrder",
          "kind": "aggregate_root",
          "description": "生产订单根实体"
        },
        {
          "id": "wo",
          "name": "工单",
          "nameEn": "WorkOrder",
          "kind": "entity",
          "aggregateRootId": "po",
          "description": "工单子实体"
        }
      ]
    },
    "behavior": {
      "actions": [
        {
          "id": "create-po",
          "name": "创建生产订单",
          "nameEn": "CreatePO",
          "aggregateRootId": "po",
          "description": "创建新生产订单",
          "publishesEventIds": ["po-created"],
          "domain": "manufacturing",
          "riskLevel": "MEDIUM"
        }
      ],
      "rules": [
        {
          "id": "rule-1",
          "name": "数量校验",
          "description": "订单数量必须大于0"
        }
      ],
      "stateMachines": [
        {
          "id": "sm-po",
          "name": "生产订单状态机",
          "aggregateRootId": "po",
          "states": [
            { "name": "created", "isInitial": true, "isFinal": false },
            { "name": "in_progress", "isInitial": false, "isFinal": false },
            { "name": "closed", "isInitial": false, "isFinal": true }
          ],
          "transitions": [
            { "from": "created", "to": "in_progress", "trigger": "start" },
            { "from": "in_progress", "to": "closed", "trigger": "complete" }
          ]
        }
      ]
    },
    "events": {
      "domainEvents": [
        {
          "id": "po-created",
          "name": "生产订单已创建",
          "nameEn": "POCreated",
          "eventType": "DOMAIN",
          "severity": "INFO",
          "aggregateRootId": "po"
        }
      ]
    },
    "governance": {
      "roles": [
        {
          "id": "planner",
          "name": "计划员",
          "permissions": ["po:create", "po:update"]
        }
      ]
    },
    "dataSources": [
      {
        "id": "erp-system",
        "name": "ERP系统",
        "sourceType": "REST_API",
        "connectionConfig": { "baseUrl": "https://erp.example.com/api" },
        "credentialRef": "vault://erp-cred"
      }
    ],
    "epc": [
      {
        "id": "epc-po-flow",
        "flowName": "生产订单创建流程",
        "steps": [
          { "stepOrder": 0, "actionId": "create-po", "conditions": ["manual"] }
        ]
      }
    ]
  }
}
```

### 场景 3: ManifestConverter 测试数据 (Project 1 格式)

**用途**: 验证 ManifestConverter 的转换逻辑

```json
{
  "apiVersion": "ontology.platform/v1",
  "kind": "OntologyManifest",
  "metadata": {
    "id": "p1-export",
    "name": "Project1导出",
    "version": "0.2.0",
    "description": "从Project1设计台导出的本体"
  },
  "spec": {
    "semantic": {
      "objectTypes": [
        { "id": "agg1", "name": "订单", "nameEn": "Order", "kind": "aggregate_root" }
      ],
      "stateMachines": [
        {
          "id": "sm1",
          "name": "订单状态",
          "states": [
            { "name": "new", "isInitial": true, "isFinal": false },
            { "name": "done", "isInitial": false, "isFinal": true }
          ],
          "transitions": [
            { "from": "new", "to": "done", "trigger": "complete" }
          ]
        }
      ]
    },
    "behavior": {
      "actions": [
        { "id": "act1", "name": "创建", "nameEn": "Create" }
      ],
      "rules": []
    },
    "events": {},
    "governance": {},
    "process": {
      "orchestrations": []
    },
    "dataSources": []
  }
}
```

**预期转换行为**:
- `spec.semantic.stateMachines[0]` → 合并到 `spec.behavior.stateMachines[0]`
- `spec.process` → 映射为 `spec.epc` (空列表)
- 各字段按 1:1 映射

### 场景 4: Excel 文件导入

**用途**: 通过 ExcelExchangeMapper 读取 xlsx 并构建 OntologyExchangeDocument

| Sheet | 数据要求 |
|:------|:---------|
| **Sheet A** | 至少 1 行数据 (ID, 名称) |
| **Sheet B** | 至少 1 行数据 (ID, 名称, 父节点ID) |
| **Sheet C** | 可选，至少 1 行 (ID, 名称, 父节点ID) |
| **Sheet EPC** | 可选，至少 1 行 (ID, 名称, 父节点ID, 归属场景ID, 步骤JSON数组) |
| **Sheet E1~E8** | 可选，至少 1 行 (ID, 名称, 维度=E1~E8) |

### 场景 5: 验证失败场景 (用于测试 Validator 各规则)

| 测试 | 构造方式 | 期望错误码 |
|:-----|:---------|:-----------|
| null manifest | `validator.validate(null)` | V00 |
| 错误 apiVersion | `apiVersion = "v2"` | V01 |
| 无效 semver | `metadata.version = "abc"` | V02 |
| 无 aggregate_root | 所有 objectType.kind ≠ "aggregate_root" | V03 |
| 损坏的 entityRef | objectType.aggregateRootId = "ghost" | V04 |
| 损坏的 actionRef | action.aggregateRootId = "ghost" | V05 |
| 损坏的 ruleRef | action.preRuleIds = ["ghost"] | V06 |
| 损坏的 eventRef | action.publishesEventIds = ["ghost"] | V07 |
| 过去时警告 | event.nameEn = "CreateOrder" | V08 (WARNING) |
| 初始状态数量错误 | stateMachine 有 0 或 2+ 个 isInitial=true | V09 |
| 明文密码 | dataSource.connectionConfig = {"password": "admin123"} | V10 |
| 重复 ID | 两个 objectType 使用相同 id | V11 |

---

## 9. 字段映射对照表 (Project 1 格式 → ManifestDocument)

### 顶层字段

| 层级 | Project 1 JSON | ManifestDocument | 映射类型 |
|:-----|:---------------|:-----------------|:---------|
| root | `apiVersion` | `apiVersion` | 1:1 直接映射 |
| root | `kind` | `kind` | 1:1 直接映射 |
| root | `metadata` | `metadata` | 详见下表 |
| root | `spec` | `spec` | 详见下表 |

### metadata 字段

| Project 1 JSON | ManifestDocument | 映射说明 | 必填/可选 |
|:---------------|:-----------------|:---------|:----------|
| `metadata.id` | `metadata.id` | 直接映射 | ✅ 必填 (V02 校验) |
| `metadata.name` | `metadata.name` | 直接映射 | 建议必填 |
| `metadata.name` | `metadata.displayName` | **复用 name 值** | 建议必填 |
| `metadata.version` | `metadata.version` | 直接映射 | ✅ 必填 (V02 校验 semver) |
| `metadata.description` | `metadata.description` | 直接映射 | 可选 |
| — | `metadata.boundedContext` | 仅 Project 2 内部字段 | 可选 |
| — | `metadata.domainTags` | 仅 Project 2 内部字段 | 可选 |
| — | `metadata.compiledAt` | 仅 Project 2 内部字段 | 可选 |
| — | `metadata.source` | 仅 Project 2 内部字段 | 可选 |
| — | `metadata.status` | 仅 Project 2 内部字段 | 可选 |

### spec.semantic 字段

| Project 1 JSON | ManifestDocument | 映射说明 | 必填/可选 |
|:---------------|:-----------------|:---------|:----------|
| `spec.semantic.objectTypes[]` | `spec.semantic.objectTypes[]` | **至少 1 个** kind=aggregate_root | ✅ 必填 (V03) |
| `spec.semantic.objectTypes[].id` | `objectType.id` | 直接映射 | ✅ 必填 |
| `spec.semantic.objectTypes[].name` | `objectType.name` | 直接映射 | ✅ 必填 |
| `spec.semantic.objectTypes[].nameEn` | `objectType.nameEn` | 直接映射 | 可选 |
| `spec.semantic.objectTypes[].kind` | `objectType.kind` | 直接映射 | ✅ 必填 (V03) |
| `spec.semantic.objectTypes[].description` | `objectType.description` | 直接映射 | 可选 |
| — | `objectType.businessScenarioIds` | 仅 Project 2 | 可选 |
| — | `objectType.aggregateRootId` | 仅 Project 2 (V04 校验) | 可选 |
| — | `objectType.properties[]` | 仅 Project 2 | 可选 |
| `spec.semantic.stateMachines[]` | → `spec.behavior.stateMachines[]` | **合并到 behavior** | 可选 |

### spec.behavior 字段

| Project 1 JSON | ManifestDocument | 映射说明 | 必填/可选 |
|:---------------|:-----------------|:---------|:----------|
| `spec.behavior.actions[]` | `spec.behavior.actions[]` | 直接映射 | 可选 |
| `spec.behavior.actions[].id` | `action.id` | 直接映射 | ✅ 必填 (如果存在) |
| `spec.behavior.actions[].name` | `action.name` | 直接映射 | 建议必填 |
| `spec.behavior.actions[].nameEn` | `action.nameEn` | 直接映射 | 可选 |
| `spec.behavior.actions[].description` | `action.description` | 直接映射 | 可选 |
| — | `action.aggregateRootId` | 仅 Project 2 (V05 校验) | 可选但建议 |
| — | `action.inputSchema` | 仅 Project 2 | 可选 |
| — | `action.preRuleIds` | 仅 Project 2 (V06 校验) | 可选 |
| — | `action.postRuleIds` | 仅 Project 2 | 可选 |
| — | `action.publishesEventIds` | 仅 Project 2 (V07 校验) | 可选 |
| — | `action.domain` | 仅 Project 2 | 可选 |
| — | `action.riskLevel` | 仅 Project 2 | 可选 |
| `spec.behavior.rules[]` | `spec.behavior.rules[]` | 直接映射 | 可选 |
| `spec.behavior.rules[].id` | `rule.id` | 直接映射 | ✅ 必填 (如果存在) |
| `spec.behavior.rules[].name` | `rule.name` | 直接映射 | 建议必填 |
| `spec.behavior.rules[].description` | `rule.description` | 直接映射 | 可选 |
| — | `rule.expression` | 仅 Project 2 | 可选 |
| `spec.behavior.stateMachines[]` | `spec.behavior.stateMachines[]` | 直接映射 | 可选 |
| `spec.semantic.stateMachines[]` | → `spec.behavior.stateMachines[]` | **从 semantic 合并** | 可选 |

### spec.events 字段

| Project 1 JSON | ManifestDocument | 映射说明 | 必填/可选 |
|:---------------|:-----------------|:---------|:----------|
| `spec.events.domainEvents[]` | `spec.events.domainEvents[]` | 直接映射 | 可选 |
| `spec.events.domainEvents[].id` | `event.id` | 直接映射 | ✅ 必填 (如果存在) |
| `spec.events.domainEvents[].name` | `event.name` | 直接映射 | 建议必填 |
| `spec.events.domainEvents[].nameEn` | `event.nameEn` | 直接映射 (V08 时态校验) | 建议必填 |
| — | `event.eventType` | 仅 Project 2 | 可选 |
| — | `event.severity` | 仅 Project 2 | 可选 |
| — | `event.aggregateRootId` | 仅 Project 2 | 可选 |
| — | `event.payloadSchema` | 仅 Project 2 | 可选 |
| — | `causalities[]` | 仅 Project 2 | 可选 |

### spec.governance 字段

| Project 1 JSON | ManifestDocument | 映射说明 |
|:---------------|:-----------------|:---------|
| `spec.governance.roles[].id` | `governance.roles[].id` | 1:1 |
| `spec.governance.roles[].name` | `governance.roles[].name` | 1:1 |
| — | `role.code` | 仅 Project 2 |
| — | `role.permissions` | 仅 Project 2 |
| — | `agentPolicies[]` | 仅 Project 2 |

### spec 特殊字段映射

| Project 1 JSON | ManifestDocument | 映射说明 |
|:---------------|:-----------------|:---------|
| `spec.process.orchestrations[]` | `spec.epc[]` | **命名差异**: process → epc |
| `spec.process.orchestrations[].id` | `epc.id` | 1:1 |
| `spec.process.orchestrations[].name` | `epc.flowName` | name → flowName |
| `spec.process.orchestrations[].steps[].actionId` | `epc.steps[].actionId` | 1:1 |
| `spec.process.orchestrations[].steps[].type` | `epc.steps[].conditions` | type 包装到 List |
| — | `epc.steps[].stepOrder` | 自动生成递增序号 |
| `spec.dataSources[].id` | `dataSources[].id` | 1:1 |
| `spec.dataSources[].name` | `dataSources[].name` | 1:1 |
| `spec.dataSources[].sourceType` | `dataSources[].sourceType` | 1:1 |
| — | `dataSource.code` | 仅 Project 2 |
| — | `dataSource.connectionConfig` | 仅 Project 2 (V10 校验) |
| — | `dataSource.credentialRef` | 仅 Project 2 |

---

## 10. Excel Sheets → OntologyExchangeDocument 映射

| Sheet | 角色 | 映射为 OntologyExchangeDocument 中 |
|:------|:-----|:-----------------------------------|
| A (ValueDomain) | 本体定义 | `metadata.id/name/description`, `spec.project.id/name/description` |
| B (Capability) | 聚合根实体 | `spec.project.dataModel.entities[]` (entityRole="aggregate_root") |
| C (Scenario) | 子实体 | `spec.project.dataModel.entities[]` (entityRole="child_entity") |
| EPC | 流程编排 | 待映射到 `spec.project.processModel.orchestrations[]` |
| E1~E8 | 维度要素 | 待映射到 `spec.project.epcModel` 或对应维度模型 |

### ExcelExchangeMapper 输出示例 (OntologyExchangeDocument JSON)

```json
{
  "apiVersion": "ontology.platform/v2",
  "kind": "OntologyExchange",
  "metadata": {
    "id": "manufacturing-ontology",
    "version": "0.1.0",
    "name": "生产制造本体",
    "description": "制造域参考模型",
    "source": "excel-import",
    "status": "draft",
    "projectId": "manufacturing-ontology",
    "exportedAt": "2026-06-20T10:00:00Z"
  },
  "spec": {
    "project": {
      "id": "manufacturing-ontology",
      "name": "生产制造本体",
      "description": "制造域参考模型",
      "dataModel": {
        "id": "manufacturing-ontology-data",
        "name": "生产制造本体 DataModel",
        "version": "0.1.0",
        "entities": [
          {
            "id": "PO",
            "name": "生产订单",
            "entityRole": "aggregate_root",
            "parentAggregateId": null
          },
          {
            "id": "WO",
            "name": "工单",
            "entityRole": "child_entity",
            "parentAggregateId": "PO"
          }
        ]
      }
    }
  }
}
```

---

## 附录: 关键类路径汇总

| 类名 | 路径 |
|:-----|:-----|
| ManifestDocument | `ontology-domain/.../vo/manifest/ManifestDocument.java` |
| ManifestValidator | `ontology-application/.../service/manifest/ManifestValidator.java` |
| ManifestValidationResult | `ontology-domain/.../vo/manifest/ManifestValidationResult.java` |
| ValidationError | `ontology-domain/.../vo/manifest/ValidationError.java` |
| ManifestConverter | `ontology-application/.../service/manifest/ManifestConverter.java` |
| ManifestService | `ontology-application/.../service/manifest/ManifestService.java` |
| ManifestServiceImpl | `ontology-application/.../service/manifest/ManifestServiceImpl.java` |
| ManifestController | `ontology-api/.../controller/ManifestController.java` |
| ImportManifestRequest | `ontology-application/.../dto/manifest/ImportManifestRequest.java` |
| ImportManifestResponse | `ontology-application/.../dto/manifest/ImportManifestResponse.java` |
| ExcelExchangeMapper | `ontology-infrastructure/.../imports/ExcelExchangeMapper.java` |
| ExcelOntologyImportAdapter | `ontology-infrastructure/.../imports/ExcelOntologyImportAdapter.java` |
| ExcelBImportAdapter | `ontology-infrastructure/.../imports/ExcelBImportAdapter.java` |
| ExcelCImportAdapter | `ontology-infrastructure/.../imports/ExcelCImportAdapter.java` |
| ExcelEpcImportAdapter | `ontology-infrastructure/.../imports/ExcelEpcImportAdapter.java` |
| ExcelEDimImportAdapter | `ontology-infrastructure/.../imports/ExcelEDimImportAdapter.java` |
| OntologyExchangeDocument | `ontology-domain/.../dto/imports/OntologyExchangeDocument.java` |
| ExcelOntologyRow | `ontology-domain/.../dto/imports/ExcelOntologyRow.java` |
| ExcelEDimRow | `ontology-domain/.../dto/imports/ExcelEDimRow.java` |
| EpcParsedRow | `ontology-domain/.../dto/imports/EpcParsedRow.java` |
| EpcStepItem | `ontology-domain/.../dto/imports/EpcStepItem.java` |
| ImportResult | `ontology-domain/.../dto/imports/ImportResult.java` |
| ManifestValidatorTest | `ontology-application/src/test/.../ManifestValidatorTest.java` |
