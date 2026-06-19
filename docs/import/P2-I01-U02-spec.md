# P2-I01-U02：Sheet B（Capability）→ ObjectType 导入适配器

| Unit ID | P2-I01-U02 |
|:-------:|:----------:|
| **所属 US** | [URS-P2-I01](./URS-P2-I01.md) |
| **状态** | 草稿 |
| **依赖** | P2-I01-U01（Ontology 已创建） |
| **预估文件** | 2 个（适配器 + 测试） |

## 1. 目标

将项目1 Excel 文件 Sheet B（Capability 能力）的每行数据导入为项目2的 `ObjectType` 实体，隶属于指定 Ontology。

## 2. 范围

### In Scope

- 读取 Sheet "B"
- 解析 ID/名称/英文名/描述/语义JSON/父节点ID 列
- 必填校验：ID、名称、父节点ID
- 映射为 ObjectType（ontologyId 由调用方传入）
- 冲突检测：该 Ontology 下 name 已存在 → skipped

### Out of Scope

- 父节点ID 交叉解析（B→A 的引用在后续合并 Unit 处理）
- 其他 Sheet

## 3. 技术设计

### 新增文件

| 文件 | 说明 |
|:----|:-----|
| `infrastructure/imports/ExcelBImportAdapter.java` | Sheet B 导入适配器 |
| (测试) `.../imports/ExcelBImportAdapterTest.java` | 单元测试 |

### 字段映射

| 项目1 Excel 列 | ObjectType 字段 | 处理 |
|:--------------:|:---------------:|:----|
| ID | `name` | 唯一标识，如 CAP-001 |
| 名称 | `displayName` | 中文显示名 |
| 英文名 | — | 暂不映射 |
| 描述 | `description` | 可选 |
| 语义(JSON) | — | 暂不映射 |
| 父节点ID | `parentId` | 原样存储 |

### 接口签名

```java
ImportResult<ObjectType> execute(InputStream xlsxStream, String ontologyId, String createdBy)
```

### 冲突检测

同一 Ontology 下 `ObjectType.name` 已存在 → skipped。

## 4. PRD 验收条款

| # | 验收项 | 验证方式 |
|:-:|--------|----------|
| AC-1 | 1 行数据 → 1 个 ObjectType，字段正确映射 | 单测 |
| AC-2 | 多行数据 → 全部创建 | 单测 |
| AC-3 | 缺 ID → errors | 单测 |
| AC-4 | 缺 父节点ID → errors | 单测 |
| AC-5 | name 冲突 → skipped | 单测 |
| AC-6 | 空 Sheet → 0 imported | 单测 |
| AC-7 | Sheet 不存在 → 异常 | 单测 |

## 5. 测试用例

**测试文件**：`ExcelBImportAdapterTest.java`

| # | 场景 | 输入 | 预期 |
|:-:|------|------|------|
| TC-1 | 正常单行 | 1 行 B 数据 | 1 imported, 字段正确 |
| TC-2 | 正常多行 | 3 行数据 | 3 imported |
| TC-3 | 缺 ID | ID 列为空 | errors |
| TC-4 | 缺父节点ID | parentId 列为空 | errors |
| TC-5 | name 冲突 | name 已存在 | skipped |
| TC-6 | 空 Sheet | 仅标题行 | 0 imported |
| TC-7 | Sheet 不存在 | 无 Sheet B | Exception |

## 6. 六步验证

- [ ] ① URS（本文档）
- [ ] ② PRD（§4 ×7 ACs）
- [ ] ③ Testing case（§5 ×7 TC）
- [ ] ④ Coding（适配器）
- [ ] ⑤ Unit test（7/7 pass）
- [ ] ⑥ E2E

## 7. 验证命令

```bash
cd D:\AI\ontology-platform
mvn test -pl ontology-infrastructure -am -Dtest=ExcelBImportAdapterTest -Dsurefire.failIfNoSpecifiedTests=false
```
