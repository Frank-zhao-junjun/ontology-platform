# P2-I01-U04：Sheet EPC → EPC 步骤解析适配器

| Unit ID | P2-I01-U04 |
|:-------:|:----------:|
| **所属 US** | [URS-P2-I01](./URS-P2-I01.md) |
| **状态** | 草稿 |
| **依赖** | 项目1 Excel Sheet EPC 格式 |
| **预估文件** | 3 个（DTO + 适配器 + 测试） |

## 1. 目标

将项目1 Excel 文件 Sheet EPC（EpcProcess 流程）的每行数据解析为 EPC 步骤结构化数据。

## 2. 范围

### 字段映射

| 项目1 Excel 列 | 解析字段 | 必填 | 特殊处理 |
|:--------------:|:--------:|:----:|:---------|
| ID | `epcId` | ✅ | 如 EPC-001 |
| 名称 | `flowName` | ✅ | 流程名称 |
| 英文名 | — | ❌ | 暂不映射 |
| 描述 | `description` | ❌ | |
| 语义(JSON) | `semantics` | ❌ | 校验 JSON 格式 |
| 父节点ID | `parentId` | ✅ | 指向 C 的 ID |
| 归属场景ID | `scenarioId` | ✅ | 所属 C 场景 |
| 步骤(JSON) | `steps[]` | ✅ | 解析为 `EpcStepItem[]` |

### 步骤 JSON 格式（来自项目1）

每行 steps 列为 JSON 数组，每个元素：

```json
{
  "id": "s1",
  "name": "下达",
  "elementRef": {
    "dimension": "E2",
    "elementId": "ACT-001",
    "versionPin": "latest_confirmed"
  }
}
```

### 解析后的 DTO

```java
public class EpcParsedRow {
    String epcId;
    String flowName;
    String description;
    String parentId;
    String scenarioId;
    List<EpcStepItem> steps;
}

public class EpcStepItem {
    String stepId;
    String stepName;
    String dimension;     // E1-E8
    String elementId;
    String versionPin;
    int stepOrder;
}
```

## 3. 技术设计

### 新增文件

| 文件 | 说明 |
|:----|:-----|
| `domain/dto/imports/EpcParsedRow.java` | EPC 解析行 DTO |
| `domain/dto/imports/EpcStepItem.java` | EPC 步骤项 DTO |
| `infrastructure/imports/ExcelEpcImportAdapter.java` | EPC Sheet 解析适配器 |
| (测试) | 单元测试 |

### 校验规则

| 字段 | 规则 |
|:----|------|
| ID | 必填 |
| 名称(flowName) | 必填 |
| 父节点ID | 必填 |
| 归属场景ID | 必填 |
| 步骤(JSON) | 必填，必须为合法 JSON 数组，每步必须有 `id` |

## 4. PRD 验收条款

| # | 验收项 | 验证方式 |
|:-:|--------|----------|
| AC-1 | 1 行 EPC 数据 + 2 个 steps → 解析出 1 个 EpcParsedRow 含 2 个步骤 | 单测 |
| AC-2 | 步骤字段正确映射（stepId/stepName/dimension/elementId） | 单测 |
| AC-3 | 缺 ID → errors | 单测 |
| AC-4 | 缺 步骤JSON → errors | 单测 |
| AC-5 | 步骤非 JSON 数组 → errors | 单测 |
| AC-6 | 步骤中缺 id → errors | 单测 |
| AC-7 | 空 Sheet → 0 imported | 单测 |

## 5. 测试用例

| # | 场景 | 预期 |
|:-:|------|------|
| TC-1 | 1 行 + 2 steps | 1 row, 2 steps, 全部字段正确 |
| TC-2 | 步骤的 elementRef 完整 | dimension/E1-E8, elementId 正确 |
| TC-3 | 缺 ID | errors |
| TC-4 | 缺 步骤JSON | errors |
| TC-5 | 步骤不是 JSON 数组（非法 JSON） | errors |
| TC-6 | 步骤缺 id 字段 | errors |
| TC-7 | 空 Sheet | 0 imported |
| TC-8 | Sheet 不存在 | 异常 |
