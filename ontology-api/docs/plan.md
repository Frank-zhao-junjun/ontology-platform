# Plan: 项目1→项目2 Import API

**Version:** v1.0
**Based on:** Spec v0.1

---

## 1. Architecture Decision

| 决策 | 选择 | 理由 |
|------|------|------|
| 新建 Controller 还是复用 | **新建** `OntologyImportController` | 不碰现有 Manifest 体系，职责分离 |
| 存储层方式 | **MyBatis-Plus Mapper** + V2 `manifest_import` 表 | 已有表结构 + 项目规范 |
| 不写 Service | Controller 直接调 Mapper | 逻辑简单（解析→校验→insert），不需要 Service 层隔离 |
| ID 生成 | UUID | 已有表主键为 UUID |
| externalId | `model.project.id` | 满足 `uq_external_version` 唯一约束 |
| 版本号 | `model.version` | 映射到 `manifest_version` 字段 |
| 响应格式 | 复用 `ApiResponse<T>` | 项目已有统一包装 |
| 错误码 | `PARSE_ERROR` / `VALIDATION_ERROR` / `DUPLICATE` | 与现有错误处理一致 |
| Mapper 位置 | `persistence` 包 | 项目已有 Mapper 均在此 |

---

## 2. Module Breakdown

| 层 | 类名 | 职责 | 文件路径 |
|---|------|------|---------|
| **Controller** | `OntologyImportController` | 接收 POST 请求，解析 JSON，调 Mapper，返回结果 | `ontology-api/.../controller/OntologyImportController.java` |
| **DTO (Request)** | `OntologyImportRequest` | `{ rawContent, createdBy }` | 嵌入 Controller 或独立文件 |
| **DTO (Response)** | `OntologyImportResponse` | `{ draftId, externalId, importedCounts }` | 嵌入或独立 |
| **Mapper** | `ManifestImportMapper` (已有) | MyBatis-Plus insert | `infrastructure/persistence/ManifestImportPOMapper.java` (已有) |
|| **Config** | `application-dev.yml` | port 已改为 8081 |

**不新建：**
- Service 层（逻辑太简单，不需要）
- Flyway V18（复用 V2 表）
- 新表（复用 V2 `manifest_import`）

---

## 3. Interface Contracts

### POST /api/v1/ontologies/import

**Request:**
```json
{
  "rawContent": "{...}",
  "createdBy": "admin"
}
```

**Response (200):**
```json
{
  "success": true,
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
  }
}
```

**Response (400 — 解析失败):**
```json
{
  "success": false,
  "message": "JSON 解析失败：Unexpected token...",
  "code": "PARSE_ERROR"
}
```

**Response (400 — 校验失败):**
```json
{
  "success": false,
  "message": "模型 JSON 格式不正确：缺少 version / project / entities 字段",
  "code": "VALIDATION_ERROR"
}
```

**Response (409 — 重复):**
```json
{
  "success": false,
  "message": "该模型版本已存在：TP-001 / v1",
  "code": "DUPLICATE"
}
```

**Response (500):**
```json
{
  "success": false,
  "message": "服务器内部错误",
  "code": "INTERNAL_ERROR"
}
```

### DB Maps 对照

| JSON field | manifest_import column | Type |
|-----------|----------------------|------|
| `rawContent` (全文) | `raw_content` | JSONB |
| `project.id` | `external_id` | VARCHAR |
| `version` | `manifest_version` | VARCHAR |
| computed counts | `imported_counts` | JSONB |
| — | `id` | UUID (auto) |
| `createdBy` | `created_by` | VARCHAR |
| — | `status` | DEFAULT 'DRAFT' |
| — | `created_at` | DEFAULT now() |
| — | `api_version` | 'v1' |
| — | `ontology_id` | UUID (随机生成) |
| — | `tenant_id` | DEFAULT 'default' |
| — | `source_format` | 'JSON' |

---

## 4. Risk Assessment

| 风险 | 概率 | 影响 | 缓解 |
|------|------|------|------|
| rawContent JSON 体积过大 | 低 | 低 | MyBatis-Plus 自动处理 JSONB；JVM 默认无限制 |
| `uq_external_version` 约束冲突 | 中 | 中 | Controller 中 try-catch `DataIntegrityViolationException` 返回 409 |
| 项目1 导出格式未来变化 | 中 | 低 | raw_content 原始存储，格式变化不影响存储层 |
| `ManifestImportPOMapper` 不存在 | 低 | 高 | 确认已有该 Mapper；如无则新建 |
| 字段计算（counts）耗时 | 低 | 低 | JSON.parse 在 Java 侧算 counts |

---

## 5. Implementation Order

```
Task 1: 确认现有 Mapper（ManifestImportPOMapper）存在且可用
         ├── 检查文件是否存在
         ├── 检查 XML 映射是否存在
         └── 确认 V2 迁移已执行

Task 2: 写 OntologyImportRequest DTO（请求体）
         ├── 字段：rawContent, createdBy
         └── 校验：rawContent 非空

Task 3: 写 OntologyImportResponse DTO（响应体）
         └── 字段：draftId, externalId, importedCounts

Task 4: 写 OntologyImportController
         ├── POST /api/v1/ontologies/import
         ├── 解析 rawContent → JSON
         ├── 校验 version/project/entities
         ├── counts 计算
         ├── try-catch DataIntegrityViolationException
         └── 返回 ApiResponse

Task 5: 写单元测试
         ├── 正常导入
         ├── 非法 JSON
         ├── 缺少字段
         └── 重复导入（409）

Task 6: mvn compile + mvn test 验证
         └── 全量通过
```
