# Tasks: 项目1→项目2 Import API

**Based on:** plan.md → tasks.md

---

### Prerequisites

- [ ] ~~确认 `ManifestImportPOMapper` 存在~~ → 不存在，需要新建

---

### Task 1: ManifestImportPO + ManifestImportPOMapper

**Objective:** PO 对象 + MyBatis-Plus Mapper

**Files:**
- Create: `ontology-infrastructure/.../persistence/ManifestImportPO.java`（@TableName("manifest_import")）
- Create: `ontology-infrastructure/.../persistence/ManifestImportPOMapper.java`（extends BaseMapper）
- 参考 `OntologyPO` / `OntologyPOMapper` 模式

**PO 字段映射：**
| PO field | 表 column | Type |
|----------|-----------|------|
| id | id | String (UUID) |
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

### Task 2: OntologyImportRequest DTO

**Objective:** 定义请求体结构

**Files:**
- Create: `ontology-api/.../controller/OntologyImportRequest.java`

**Acceptance:**
- 有 `rawContent` 字段（String）
- 有 `createdBy` 字段（String, optional）
- 可被 Jackson 反序列化

**Verify:** `javac` 编译通过

---

### Task 2: OntologyImportResponse DTO

**Objective:** 定义响应体结构

**Files:**
- Create: `ontology-api/.../controller/OntologyImportResponse.java`

**Acceptance:**
- 有 `draftId`（String）
- 有 `externalId`（String）
- 有 `importedCounts`（Map<String,Integer>）
- 可被 Jackson 序列化

**Verify:** `javac` 编译通过

---

### Task 3: OntologyImportController

**Objective:** POST /api/v1/ontologies/import 端点

**Files:**
- Create: `ontology-api/.../controller/OntologyImportController.java`

**Steps:**
1. 注入 `ManifestImportPOMapper`
2. `@PostMapping("/api/v1/ontologies/import")`
3. 解析 `rawContent` → JSONObject
4. 校验：version, project, entities 存在
5. 计算 importedCounts
6. 构造 `ManifestImportPO` 并 insert
7. try-catch `DataIntegrityViolationException` → 409
8. 返回 `ApiResponse`

**Acceptance:**
- TC-01 ~ TC-06 全通过

**Verify:** `mvn test` 通过

---

### Task 4: 单元测试

**Objective:** OntologyImportControllerTest

**Files:**
- Create: `ontology-api/src/test/java/.../controller/OntologyImportControllerTest.java`

**Test Cases:**
1. testImportSuccess — 正常导入 (TC-01)
2. testParseError — 非法 JSON (TC-02)
3. testValidationError — 缺少字段 (TC-03, TC-05)
4. testDuplicate — 重复导入 (TC-04)
5. testCountsCorrectness — 统计正确性 (TC-06)

**Acceptance:** 全部测试通过

**Verify:** `mvn test`

---

### Task 5: 全量回归

**Objective:** 确认不破坏已有功能

**Steps:**
1. `mvn compile -Dmaven.test.skip=true`
2. `mvn test`

**Acceptance:** 编译 0 error, 全部单元测试通过
