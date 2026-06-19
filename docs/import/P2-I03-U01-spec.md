# P2-I03-U01：YAML OntologyManifest 导入解析器

| Unit ID | P2-I03-U01 |
|:-------:|:----------:|
| **预估文件** | 4 个（YAML DTO + 解析器 + 转换器 + 测试） |
| **依赖** | SnakeYAML、P2-I02-U01 JSON 转换器 |

## 1. 目标

将项目1导出的 YAML OntologyManifest（`manufacturing-manifest.yaml` 格式）解析为项目2的领域实体，使 YAML 导入可与已有 Excel/JSON 导入融合。

## 2. 输入格式（YAML OntologyManifest）

项目1 YAML 导出采用 Kubernetes-style YAML frontmatter 结构：

```yaml
apiVersion: ontology.platform/v1
kind: OntologyManifest
metadata:
  id: manufacturing-ontology
  name: 生产制造本体
  displayName: 生产制造本体
  description: MVP 制造域参考模型
  boundedContext: 生产制造
  domainTags: [生产制造]
  status: draft
spec:
  semantic:
    boundedContext: { id, name, nameEn, description }
    businessScenarios: [{ id, name, nameEn, description, applicableObjectTypeIds }]
    valueObjects: [{ id, name, nameEn, properties: [{ id, name, nameEn, dataType, required }] }]
    objectTypes: [{ id, name, nameEn, kind, description, businessScenarioIds, properties, relations }]
    stateMachines: [{ id, name, objectTypeId, statusField, states, transitions }]
  behavior:
    actions: [{ id, name, nameEn, aggregateRootId, parameters, preRuleIds, events }]
    rules: [{ id, name, type, expression }]
  events:
    domainEvents: [{ id, name, nameEn, aggregateRootId, triggerActionId, payloadSchema }]
  governance:
    roles: [{ id, name, permissions }]
```

## 3. 映射方案

| YAML 路径 | 项目2 实体 | 映射逻辑 |
|:----------|:-----------|:---------|
| `metadata.id` | Ontology.name | YAML id → 本体唯一标识 |
| `metadata.displayName` | Ontology.displayName | 原样传递 |
| `metadata.description` | Ontology.description | 原样传递 |
| `metadata.boundedContext` | Ontology.description 追加 | 追加 "(限界上下文: xxx)" |
| `metadata.domainTags` | Ontology 语义 JSON | 存储为 semantics 字段 |
| `spec.semantic.objectTypes[]` | ObjectType | 每个 objectType → 一个 ObjectType 实体 |
| `spec.semantic.objectTypes[].properties[]` | Property | 内联属性列表 |
| `spec.semantic.objectTypes[].relations[]` | Relation | 内联关系列表 |
| `spec.semantic.businessScenarios[]` | ObjectType (kind=scenario) | 业务场景 → ObjectType |
| `spec.semantic.valueObjects[]` | ObjectType (kind=value_object) | 值对象 → ObjectType |
| `spec.semantic.stateMachines[]` | StateMachine | 状态机实体 |
| `spec.behavior.actions[]` | ActionDefinition | 动作定义 |
| `spec.events.domainEvents[]` | DomainEvent | 领域事件 |

## 4. 接口

```java
// 主解析器
YamlOntologyManifest parse(String yamlContent);

// 转换器
YamlImportResult convertToEntities(YamlOntologyManifest manifest);

// 结果 DTO
class YamlImportResult {
    Ontology ontology;
    List<ObjectType> objectTypes;
    List<StateMachine> stateMachines;
    List<ActionDefinition> actions;
    List<DomainEvent> domainEvents;
}
```

## 5. Testing Cases

| # | 场景 | 输入 | 预期 |
|:-:|------|------|------|
| TC-1 | 完整 YAML 解析 → 所有元数据正确 | manufacturing-manifest.yaml | `metadata.id=manufacturing-ontology`, `metadata.name=生产制造本体`, `spec.semantic.objectTypes[0].id=production-order` |
| TC-2 | ObjectType 解析 → properties/relations | manufacturing-manifest.yaml | production-order 有 5 properties + 1 relation |
| TC-3 | 业务场景解析 → 2 scenarios | manufacturing-manifest.yaml | 2 个 BusinessScenario: MTS + MTO |
| TC-4 | 值对象解析 → 1 ValueObject | manufacturing-manifest.yaml | 1 个 ValueObject: vo-quantity |
| TC-5 | 状态机解析 → 1 StateMachine | manufacturing-manifest.yaml | 1 个 StateMachine: sm-production-order |
| TC-6 | 行为解析 → 3 Actions | manufacturing-manifest.yaml | 3 个 ActionDefinition |
| TC-7 | 事件解析 → 3 DomainEvents | manufacturing-manifest.yaml | 3 个 DomainEvent |
| TC-8 | 全量转换为实体 → 非空校验 | manufacturing-manifest.yaml | ontology≠null, objectTypes.size()≥5 |
