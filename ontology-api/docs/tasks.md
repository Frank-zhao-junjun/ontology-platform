# Tasks: 项目1→项目2 Import API

**Based on:** plan.md → tasks.md

---

### Prerequisites

- [x] `ManifestImportPOMapper` 已创建（extends BaseMapper<ManifestImportPO>）

---

### Task 1: ManifestImportPO + ManifestImportPOMapper ✅

**Objective:** PO 对象 + MyBatis-Plus Mapper — **已完成**

| PO field | 表 column | Type |
|----------|-----------|------|
| id | id | String (UUID, @TableId ASSIGN_UUID) |
| ontologyId | ontology_id | String |
| externalId | external_id | String |
| tenantId | tenant_id | String |
| status | status | String |
| apiVersion | api_version | String |
| manifestVersion | manifest_version | String |
| sourceFormat | source_format | String |
| rawContent | raw_content | String (JSON) |
| importedCounts | imported_counts | String (JSON) |
| validationErrors | validation_errors | String (JSON) |
| createdBy | created_by | String |
| createdAt | created_at | Instant |
| updatedAt | updated_at | Instant |
| publishedAt | published_at | Instant |

**Acceptance:** `mvn compile` 通过

**Verify:** `javac` 0 error

---

### Task 2: OntologyImportRequest DTO ✅

**Objective:** 定义请求体结构 — **已完成**

**Acceptance:**
- [x] 有 `rawContent` 字段（String）
- [x] 有 `createdBy` 字段（String, optional）
- [x] 可被 Jackson 反序列化

**Verify:** `javac` 编译通过

---

### Task 2: OntologyImportResponse DTO ✅

**Objective:** 定义响应体结构 — **已完成**

**Acceptance:**
- [x] 有 `draftId`（String）
- [x] 有 `externalId`（String）
- [x] 有 `importedCounts`（Map<String,Integer>）
- [x] 可被 Jackson 序列化

**Verify:** `javac` 编译通过

---

### Task 3: OntologyImportController ✅

**Objective:** POST /api/v1/ontologies/import 端点 — **已完成**

**Acceptance:**
- [x] TC-01 ~ TC-06 全通过

**Verify:** `mvn test` 通过

---

### Task 4: 单元测试 ✅

**Objective:** OntologyImportControllerTest — **已完成**

**Test Cases (7/7 通过):**
- [x] testImportSuccess — 正常导入 (TC-01)
- [x] testParseError — 非法 JSON (TC-02)
- [x] testValidationError — 缺少字段 (TC-03, TC-05)
- [x] testDuplicate — 重复导入 (TC-04)
- [x] testCountsCorrectness — 统计正确性 (TC-06)
- [x] import_shouldPersistToDB — 验证 insert 参数
- [x] import_shouldReturn422_whenEmptyBody — 空 JSON (补充验证)

**Verify:** `mvn test`

---

### Task 5: 全量回归 ✅

**Objective:** 确认不破坏已有功能 — **已完成**

**Steps:**
1. [x] `mvn compile` → 0 error
2. [x] `mvn test` → 174/174 通过（包括修复的 95 个 @WebMvcTest）
