# URS-P2-I01：Excel Sheet A（ValueDomain）导入到 Ontology

## 用户需求

项目1（Ontology 建模工具）导出的 Excel 文件中，Sheet A（ValueDomain 价值域）的每一行需要能被导入到项目2（ontology-platform）中，创建为一个 Ontology 本体。

## 输入格式

项目1 Excel Sheet A 的列定义（来源：`src/lib/excel/excel-schema.ts`）：

| 列 | 必填 | 类型 | 含义 |
|:--:|:----:|:----:|------|
| ID | ✅ | string | ValueDomain 唯一标识 |
| 名称 | ✅ | string | 中文显示名称 |
| 英文名 | ❌ | string | 英文名称 |
| 描述 | ❌ | string | 业务描述 |
| 语义(JSON) | ❌ | json | 语义块（terms/triggerPhrases） |

示例行：

| ID | 名称 | 英文名 | 描述 | 语义(JSON) |
|:--:|:----:|:------:|:----:|:----------:|
| VD-001 | 生产制造 | manufacturing | 生产制造领域 | {"terms":["生产","制造"],"triggerPhrases":["安排生产"]} |
| VD-002 | 财务会计 | finance | 财务核算领域 | {"terms":["财务","会计"]} |

## 输出模型

项目2 `Ontology` 聚合根（`ontology-domain/.../entity/Ontology.java`）：

| 字段 | 值来源 | 说明 |
|:----:|:------:|------|
| `id` | UUID 自动生成 | 主键 |
| `name` | `ID` 列 | 唯一标识，如 `VD-001` |
| `displayName` | `名称` 列 | 显示名称 |
| `description` | `描述` 列 | 描述 |
| `version` | `"0.1.0"` | 初始版本 |
| `status` | `DRAFT` | 初始草稿状态 |
| `createdBy` | 传入用户参数 | 导入操作用户 |
| `semantics` | `语义(JSON)` 列 | JSON 原样存储 |

## 约束

1. 同一 Excel 文件的多个 A 行应合并到**一个** Ontology？还是每行建一个 Ontology？
   → **先每行建一个 Ontology**
2. 已存在的 `name` 应如何处理？→ 跳过并记录冲突
3. 导入后 Ontology 状态：DRAFT（草稿）
