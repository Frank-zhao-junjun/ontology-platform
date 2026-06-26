# Spec: 项目1→项目2 本体模型发布管道 — Import API

**Version:** v0.1 (Draft)
**Author:** Hermes
**Date:** 2026-06-26

---

## Version History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| v0.1 | 2026-06-26 | Hermes | Initial Draft |

---

## 1. Problem Statement

当前项目1（Ontology 设计台）导出的本体模型 JSON 没有自动推送通道：

- 手动导出 JSON → 手动复制 → 手动导入平台，流程断裂
- 已有 ManifestService 用内存存储，重启后数据丢失
- 已有 V2 迁移表 `manifest_import` 但 Service 未接入
- 平台端口配置异常（当前 -1）
- MCP Server 的 `upload_ontology_model` tool 只能从终端 JSON 导入，无法从 DB 恢复

**做**：打通项目1→项目2 的发布管道，第一步是让平台能接收项目1格式并持久化。

---

## 2. Success Metrics

| Metric | Target | How to measure |
|--------|--------|----------------|
| import API 可用 | POST 200 | `curl -X POST localhost:8081/api/v1/ontologies/import -d @sample.json` 返回 draftId |
| DB 持久化 | 100% | 调用后查询 `manifest_import` 表有对应记录 |
| 重启不丢失 | ✓ | import 后重启 Spring Boot，数据仍在 `manifest_import` 表 |
| 端口可访问 | 8081 | `curl localhost:8081/health` 返回 200 |
| MCP Server 重启后可见 | ✓ | 重启 MCP Server → `tools/list` 包含 search_*/get_* |

> **注**：MCP Server 可见性依赖现有 `upload_ontology_model` + 持久化文件机制。本 Phase 先将数据写到 DB，MCP 改读 DB 在 Feature 3。

---

## 3. User Stories

见 `docs/user-stories.md` — 包含 US-01 到 US-04。

---

## 4. Acceptance Criteria

```gherkin
Scenario: 导入项目1导出 JSON
  Given Spring Boot 运行在 8081
  When POST /api/v1/ontologies/import
    | header | Content-Type: application/json |
    | body | {"version":"v1","project":{"name":"测试","id":"TP-001"},"entities":[...]} |
  Then 返回 200
  And body.success == true
  And body.draftId 不为空
  And body.importedCounts.entities == N

Scenario: 导入非法 JSON → 400
  When POST /api/v1/ontologies/import with body "not json"
  Then 返回 400
  And body.message 包含"解析失败"

Scenario: 导入缺少必要字段 → 400
  When POST /api/v1/ontologies/import with body {"version":"v1"} (无 entities)
  Then 返回 400
  And body.message 包含"缺少"

Scenario: 唯一约束冲突 → 409
  Given 同一 project.id + version 已导入
  When 再次导入相同 project.id + version
  Then 返回 409
  And body.message 包含"已存在"

Scenario: 端口修复
  Given application.yml 中 server.port 为 -1
  When 启动 Spring Boot
  Then 日志显示"Tomcat initialized with port 8081"
  And GET localhost:8081/health 返回 200
```

---

## 5. Non-Goals

| 不做 | 原因 |
|-----|------|
| 格式转换（project1 → ManifestDocument） | 保持 raw_content 原始存储，减少耦合 |
| MCP Server 直接读 DB | 后续 Feature 3 实现 |
| 项目1 前端按钮 | 后续 Feature 2 实现 |
| 改写 ManifestServiceImpl（内存→DB） | 现有接口是为 ManifestDocument 设计，Scope外 |
| Flyway V18 建新表 | 复用 V2 `manifest_import` 表 |
| 认证/鉴权 | 开发环境不需要，生产环境再补 |
| 权限控制 | 同上 |

---

## 6. Constraints

| 约束 | 说明 |
|------|------|
| 必须复用 V2 `manifest_import` 表 | 不新建表，schema 已存在 |
| 必须使用 MyBatis-Plus | 项目已有 Mapper 层 |
| 不引入新依赖 | 现有 pom.xml 范围 |
| port 改为 8081 | 避免 8080 冲突 |
| 支持 dev profile | 当前 dev profile 已禁用 Flyway |
| API 路径 `/api/v1/ontologies/import` | 与现有的 `/api/v1/manifests/import` 并存 |
| 响应格式 | 复用 `ApiResponse<T>` 统一响应 |

---

## 7. Testing Cases

### TC-01: 正常导入项目1 JSON

```gherkin
Given 项目1导出 JSON：
  {
    "version": "v1",
    "project": { "name": "测试模型", "id": "TP-001" },
    "entities": [
      { "id": "material", "name": "物料", "nameEn": "material", "attributes": [], "relations": [] }
    ],
    "stateMachines": [],
    "rules": [],
    "metrics": [],
    "dataSources": [],
    "businessChain": { "valueDomains": [], "capabilities": [], "scenarios": [], "epcProcesses": [] },
    "governance": { "roles": [] }
  }
When POST /api/v1/ontologies/import
Then 返回 200
And body.success == true
And body.data.draftId 不为 null
And body.data.externalId == "TP-001"
And body.data.importedCounts.entities == 1
And body.data.importedCounts.stateMachines == 0
And manifest_import 表中有对应记录
And raw_content 包含完整 JSON
```

### TC-02: 非法 JSON

```gherkin
When POST /api/v1/ontologies/import
  body: "not valid json at all"
Then 返回 400
And body.code == "PARSE_ERROR"
```

### TC-03: 缺少必要字段

```gherkin
When POST /api/v1/ontologies/import
  body: {"version": "v1"} （无 project 和 entities）
Then 返回 400
And body.code == "VALIDATION_ERROR"
And body.message 包含"缺少"
```

### TC-04: 重复导入（唯一约束冲突）

```gherkin
Given 已导入 project.id="TP-001" + version="v1"
When POST /api/v1/ontologies/import 再次导入同一 project.id + version
Then 返回 409
And body.code == "DUPLICATE"
And body.message 包含"已存在"
```

### TC-05: 空 JSON

```gherkin
When POST /api/v1/ontologies/import
  body: "{}"
Then 返回 400
And body.code == "VALIDATION_ERROR"
```

### TC-06: counts 正确性

```gherkin
Given 模型有 5 个 entities, 3 个 stateMachines, 10 个 rules
When 导入成功
Then body.data.importedCounts == {
  "entities": 5, "stateMachines": 3, "rules": 10,
  "metrics": 0, "dataSources": 0, "businessChain": 0, "governance": 0
}
```

---

## Function Spec Details

### API: POST /api/v1/ontologies/import

**Request:**
```json
{
  "rawContent": "{...}",
  "createdBy": "admin"
}
```

`rawContent` = 项目1导出的 JSON 字符串（OntologyModel 格式）

**Response (200):**
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "draftId": "550e8400-e29b-41d4-a716-446655440000",
    "externalId": "TP-001",
    "importedCounts": {
      "entities": 10,
      "stateMachines": 2,
      "rules": 15,
      "metrics": 5,
      "dataSources": 3,
      "businessChain": 1,
      "governance": 1
    }
  },
  "meta": { "requestId": "...", "timestamp": "..." }
}
```

**Response (400/422/409):**
```json
{
  "code": 400,
  "message": "PARSE_ERROR: JSON 解析失败：...",
  "meta": { "requestId": "...", "timestamp": "..." }
}
```
