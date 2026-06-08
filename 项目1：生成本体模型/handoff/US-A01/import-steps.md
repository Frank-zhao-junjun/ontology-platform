# US-A01 导入步骤 — 项目2（ontology-platform）

> 面向：平台联调 / smoke（非生产发布）  
> 输入：[manufacturing-manifest.yaml](./manufacturing-manifest.yaml)  
> 平台分支：`web-ui`（2026-06-04）

## 前置条件

1. PostgreSQL 或 H2 测试 profile 已启动（本地验证可用 `@ActiveProfiles("h2")`）。
2. 已阅读 [ontology-manifest-spec.md §8](../../../ontology-platform/docs/shared/ontology-manifest-spec.md)（V01–V11）。
3. **已知缺口**：平台尚无「一键 Manifest import」REST；以下分 **自动（规划）** 与 **手工 smoke（当前）** 两档。

---

## A. 目标态（US-A01 正式 import API — 待实现）

1. `POST /api/v1/manifests/import`（或等价）上传 YAML/JSON body。
2. 服务端执行 V01–V11；失败返回 AC-4 结构：`elementType` + `id` + `field` + `message`。
3. 成功返回：`draftId`、`importedCounts`、`warnings`。
4. 管理员 `POST /contexts/{id}/approve` 触发 publish，写入 `manifests` 表（TDD §3）。

---

## B. 当前 smoke（手工映射到已落地 API）

以下用主 YAML 的 **语义层 + 治理层** 片段验证平台 Sprint 1/2 能力；行为/事件层仅做 JSON 快照比对，不要求全部落库。

### B.1 启动 API（H2 本地）

```powershell
cd "E:\00 - AI\本体建模\ontology-platform"
. .\use-java17.ps1
mvn -pl ontology-api -am spring-boot:run "-Dspring-boot.run.profiles=h2"
```

基址：`http://localhost:8080/api`（`context-path: /api`）。

### B.2 导入限界上下文（对应 `spec.semantic.boundedContext`）

```http
POST /v1/contexts
Content-Type: application/json

{
  "name": "生产制造",
  "code": "manufacturing",
  "description": "生产执行限界上下文",
  "domainTag": "manufacturing"
}
```

记录响应中的 `data.id` 为 `{contextId}`。

### B.3 导入聚合根与对象类型

对 YAML 中每个 `kind: aggregate_root` / `entity`：

```http
POST /v1/contexts/{contextId}/aggregate-roots
{ "name": "...", "code": "ProductionOrder", "description": "..." }

POST /v1/contexts/{contextId}/object-types
{ "name": "...", "code": "...", "objectKind": "ENTITY", "aggregateRootId": "..." }
```

`code` 建议与 YAML `objectTypes[].nameEn` 或 `id` 对齐（见 [issues-checklist.md](./issues-checklist.md)）。

### B.4 导入关系（可选）

```http
POST /v1/contexts/{contextId}/relationships
{
  "sourceObjectId": "...",
  "targetObjectId": "...",
  "name": "引用BOM",
  "code": "po-bom",
  "cardinality": "N:1",
  "relationKind": "REFERENCE"
}
```

### B.5 导入治理（Sprint 2 已落地）

```http
POST /v1/roles
{ "name": "生产计划员", "code": "production_planner", "contextId": "{contextId}" }

POST /v1/roles/{roleId}/object-permissions
{ "objectTypeId": "...", "permRead": true, "permExecute": true }

POST /v1/roles/{roleId}/field-permissions
{ "objectTypeId": "...", "fieldName": "cost_price", "isVisible": true, "isEditable": false }

POST /v1/sandboxes
{
  "name": "sandbox-prod-planner",
  "agentRoleId": "{roleId}",
  "allowedTools": ["resolve_intent", "query_ontology", "execute_action"]
}
```

### B.6 数据源（Sprint 2）

```http
POST /v1/data-sources
{ "name": "SAP 生产订单 OData", "code": "sap_s4hana_prod", "sourceType": "API", "connectionConfig": {}, "credentialRef": "secret/sap-oauth-prod" }
```

### B.7 提交审核 / 发布（G05 工作流）

```http
POST /v1/contexts/{contextId}/submit-review
POST /v1/contexts/{contextId}/approve
```

### B.8 行为 / 事件 / 完整 Manifest 快照

- **当前**：将 YAML `spec.behavior`、`spec.events` 存为联调附件，与 TDD Manifest 编译器输出做 JSON diff（平台 `manifests` 表 / import API 未就绪）。
- **通过标准（smoke）**：B.2–B.7 HTTP 201/200；无 4xx/5xx；`objectTypeId` 引用可解析。

---

## C. Round-trip（US-A01 AC-3，双方）

1. 设计台导出 YAML（本包）→ 平台 import（目标态 API 或 B 节手工）。
2. 平台再导出 / 编译 → 对比语义层：上下文、场景、聚合根、行为 id 无丢失。
3. 差异记入 [issues-checklist.md](./issues-checklist.md)。

---

## D. 推荐验证命令（平台）

```powershell
mvn -pl ontology-api -am test "-Dtest=GovernanceControllerTest,ModelingControllerTest,BoundedContextControllerTest" "-Dsurefire.failIfNoSpecifiedTests=false"
```
