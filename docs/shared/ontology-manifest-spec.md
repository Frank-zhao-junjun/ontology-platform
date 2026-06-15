# OntologyManifest 规范

> 版本：v1.0 | 状态：Final | 2026-06-15  
> 对应平台校验器：`ManifestValidator`（V01–V11）  
> 样例：[examples/manufacturing-manifest.yaml](./examples/manufacturing-manifest.yaml)

---

## 1. 概述

OntologyManifest 是设计台（项目1）导出、治理平台（项目2）导入的**标准交换格式**。Agent 运行时仅消费 **已发布（published）** 的 Manifest。

| 字段 | 值 |
|------|-----|
| `apiVersion` | `ontology.platform/v1`（当前唯一受支持版本） |
| `kind` | `OntologyManifest` |
| 传输格式 | YAML 或 JSON |
| 导入 API | `POST /api/v1/manifests/import` |

---

## 2. 顶层结构

```yaml
apiVersion: ontology.platform/v1
kind: OntologyManifest
metadata:
  id: manufacturing-ontology      # 文档内唯一标识
  version: "0.1.0"                # semver x.y.z
  name: 生产制造本体
  boundedContext: 生产制造
  source: ontology-designer
  status: draft                   # draft | published
spec:
  semantic:    { ... }            # 语义层：上下文、对象类型
  behavior:    { ... }            # 行为层：动作、规则、状态机
  events:      { ... }            # 事件层：领域事件、因果
  governance:  { ... }            # 治理层：角色、Agent 策略
  dataSources: [ ... ]             # 可选：外部数据源（凭证用 *Ref）
  epc:         [ ... ]            # 可选：EPC 流程步骤
```

Java 领域模型见 `ontology-domain/.../vo/manifest/ManifestDocument.java`。

---

## 3. 语义层（spec.semantic）

| 元素 | 说明 |
|------|------|
| `boundedContext` | 限界上下文 id / name / nameEn |
| `businessScenarios` | 业务场景，可关联 `applicableObjectTypeIds` |
| `valueObjects` | 值对象及属性 |
| `objectTypes` | 对象类型；`kind` 为 `aggregate_root` 或 `entity` |

**entity 规则**：每个 `kind: entity` 必须引用存在的 `aggregateRootId`（V04）。

**aggregate_root 规则**：至少 1 个 `kind: aggregate_root`（V03）。

---

## 4. 行为层（spec.behavior）

| 元素 | 说明 |
|------|------|
| `actions` | 业务动作；须绑定有效 `aggregateRootId`（V05） |
| `rules` | 前置/后置规则；`preRuleIds` 须引用存在的 rule（V06） |
| `stateMachines` | 状态机；每个状态机恰 1 个 `isInitial: true`（V09） |

动作可声明 `publishesEventIds`，须引用 events 层存在的 event id（V07）。

---

## 5. 事件层（spec.events）

| 元素 | 说明 |
|------|------|
| `domainEvents` | 领域事件；`nameEn` 建议过去式（V08 为 warning） |
| `causalities` | 因果链（causeEventId → effectEventId） |

---

## 6. 治理层（spec.governance）

| 元素 | 说明 |
|------|------|
| `roles` | Agent 角色与权限 |
| `agentPolicies` | MCP 工具白名单、沙箱策略 |

---

## 7. 数据源（spec.dataSources）

- 连接信息使用 `credentialRef` / `authSecretRef`，**禁止明文密码或 API Key**（V10）。
- `connectionConfig` 中不得出现 `password`、`apikey`、`secret` 等明文字段。

---

## 8. 校验规则 V01–V11

| 编号 | 规则 | 级别 |
|------|------|:----:|
| V01 | `apiVersion` 必须为 `ontology.platform/v1` | error |
| V02 | `metadata.version` 符合 semver `x.y.z` | error |
| V03 | 至少 1 个 `aggregate_root` | error |
| V04 | 每个 `entity` 的 `aggregateRootId` 可解析 | error |
| V05 | 每个 `action` 的 `aggregateRootId` 可解析 | error |
| V06 | `preRuleIds` 引用存在的 `rules` | error |
| V07 | `publishesEventIds` 引用存在的 `domainEvents` | error |
| V08 | 领域事件 `nameEn` 过去式（未满足为 warning） | warn |
| V09 | 每个状态机恰 1 个 `isInitial: true` | error |
| V10 | 无明文凭证 | error |
| V11 | 文档内 `id` 全局唯一 | error |

校验失败时 API 返回 AC-4 结构：`elementType` + `id` + `field` + `message`（HTTP 422）。

实现：`ontology-application/.../manifest/ManifestValidator.java`  
测试：`ManifestValidatorTest`（13+ 用例）

---

## 9. 平台工作流

```
设计台导出 YAML
  → POST /api/v1/manifests/import（V01–V11 校验）
  → draft 入库
  → POST /api/v1/manifests/{id}/preview（可选）
  → POST /api/v1/manifests/{id}/publish
  → Agent / MCP 消费 published 版本
  → GET /api/v1/manifests/{id}/export（round-trip 验证）
```

---

## 10. 相关文档

- [API 契约 — Manifest 端点](./API契约-本体建模平台-v2.0.yaml)
- [Phase 1 Spec — Manifest 实施](../superpowers/specs/phase1-spec-v1.md)
- [故事地图 — US-P01~P03](./PRD-本体建模平台-UserStoryMap-v1.2.md)
