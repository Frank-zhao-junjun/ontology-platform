# US-P2-I01-U01：Sheet A → Ontology 导入适配器

| Unit ID | US-P2-I01-U01 |
|:-------:|:-------------:|
| **所属 US** | [US-P2-I01](./URS-P2-I01.md) |
| **状态** | 草稿 |
| **依赖** | 无（纯 Java + Apache POI + Jackson） |
| **预估文件** | 3 个（适配器 + DTO + 测试） |

## 1. 目标

将项目1导出的 Excel 文件中 Sheet A 的行数据解析为项目2的 `Ontology` 实体列表，支持冲突检测。

## 2. 范围

### In Scope

- 读取 `.xlsx` 文件中的 Sheet "A"
- 逐行解析 ID、名称、英文名、描述、语义(JSON) 列
- 转换为 `Ontology` 实体（状态=DRAFT）
- 检测 name（=ID）冲突：已存在则跳过
- 返回解析结果（成功列表 + 跳过列表 + 错误列表）

### Out of Scope

- 其他 Sheet（B/C/EPC/E1-E8）— 后续 Unit
- 多 Sheet 交叉校验
- Excel 模板生成
- 文件上传/分片上传逻辑

## 3. 技术设计

### 新增文件

| 文件 | 说明 |
|:----|:-----|
| `ontology-domain/.../dto/ExcelOntologyRow.java` | Excel 行数据 DTO |
| `ontology-domain/.../service/ExcelOntologyImportAdapter.java` | 领域服务：解析 + 映射 |
| (测试) `.../ExcelOntologyImportAdapterTest.java` | 单元测试 |

### 核心流程

```
xlsx 文件流
    │
    ├─ Apache POI 读取 Sheet "A"
    │
    ├─ 逐行解析 → ExcelOntologyRow（含校验）
    │
    ├─ 映射 row → Ontology
    │   name=row.id, displayName=row.name, ...
    │
    ├─ 冲突检测：OntologyRepository.existsByName(name)
    │   ├─ 不存在 → 创建 Ontology（DRAFT）
    │   └─ 存在 → 加入 skipped 列表
    │
    └─ 返回 ImportResult<Ontology>
```

### ExcelOntologyRow DTO

```java
public class ExcelOntologyRow {
    private String id;           // 必填
    private String name;         // 必填（中文名称）
    private String nameEn;       // 可选
    private String description;  // 可选
    private String semantics;    // 可选（JSON字符串）
}
```

### ImportResult

```java
public class ImportResult<T> {
    List<T> imported;    // 成功创建
    List<String> skipped; // 因冲突跳过（记录冲突原因）
    List<String> errors;  // 解析错误（记录错误原因）
}
```

## 4. PRD 验收条款

| # | 验收项 | 验证方式 |
|:-:|--------|----------|
| AC-1 | 1 行有效 Sheet A 数据 → 创建 1 个 Ontology（DRAFT） | 单测 |
| AC-2 | 2 行有效数据 → 创建 2 个 Ontology | 单测 |
| AC-3 | 缺少必填列 ID → 加入 errors 列表 | 单测 |
| AC-4 | 缺少必填列 名称 → 加入 errors 列表 | 单测 |
| AC-5 | name 已存在 → 加入 skipped 列表，不创建 | 单测 |
| AC-6 | 语义JSON 列非合法 JSON → 加入 errors 列表 | 单测 |
| AC-7 | 英文名/描述/语义 可空列不存在 → 正常创建，空值填充 | 单测 |
| AC-8 | 空 Sheet → 返回空结果（0 imported） | 单测 |
| AC-9 | 不存在的 Sheet 名 → 抛出明确异常 | 单测 |

## 5. 测试用例

**测试文件**：`ontology-domain/src/test/java/com/ontology/platform/domain/service/ExcelOntologyImportAdapterTest.java`

| # | 场景 | 输入 | 预期 |
|:-:|------|------|------|
| TC-1 | 正常单行 | 有效的 xlsx 含 1 行 A 数据 | 1 imported, 0 errors, 0 skipped |
| TC-2 | 正常多行 | 含 2 行有效数据 | 2 imported |
| TC-3 | 缺 ID | ID 列为空 | errors 含行号+字段名 |
| TC-4 | 缺名称 | 名称列为空 | errors 含行号+字段名 |
| TC-5 | name 冲突 | ID 与已有 Ontology 重复 | skipped 含冲突信息 |
| TC-6 | 语义JSON 非法 | 语义列写入非 JSON 字符串 | errors 含"JSON格式错误" |
| TC-7 | 可空列为空 | 英文名/描述/语义 均未填 | 正常创建 |
| TC-8 | 空 Sheet | Sheet 有标题行无数据 | 0 imported |
| TC-9 | Sheet 不存在 | xlsx 没有叫 "A" 的 Sheet | 抛出 IllegalArgumentException |
| TC-10 | 非法 xlsx | 非 Excel 文件的字节流 | 抛出 IOException |
| TC-11 | 多 Sheet 只取 A | xlsx 含 A/B/C 三个 Sheet | 只解析 A Sheet 的行 |

## 6. 六步验证

- [ ] ① URS（本文档）
- [ ] ② PRD（§4 ×9 ACs）
- [ ] ③ Testing case（§5 ×11 TC）
- [ ] ④ Coding（适配器 + DTO）
- [ ] ⑤ Unit test（TC-1~11 全部通过）
- [ ] ⑥ E2E

## 7. 验证命令

```bash
cd D:\AI\ontology-platform
mvn test -pl ontology-domain -Dtest=ExcelOntologyImportAdapterTest -Dsurefire.useFile=false
```
