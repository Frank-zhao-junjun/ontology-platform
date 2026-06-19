# P2-I01-U05：Sheet E1~E8 — 维度要素通用解析适配器

| Unit ID | P2-I01-U05 |
|:-------:|:----------:|
| **所属 US** | [URS-P2-I01](./URS-P2-I01.md) |
| **预估文件** | 2 个（适配器 + 测试） |

## 1. 目标

将项目1 Excel 文件中 Sheet E1~E8 任意一个的数据行解析为结构化维度要素 DTO。

## 2. 列结构（8 个 Sheet 通用）

| 项目1 Excel 列 | 解析字段 | 必填 |
|:--------------:|:--------:|:----:|
| ID | `elementId` | ✅ |
| 名称 | `name` | ✅ |
| 英文名 | `nameEn` | ❌ |
| 维度 | `dimension` | ✅（E1~E8） |
| 可见性 | `visibility` | ❌（enum: project/domain_scoped/private_draft） |
| 描述 | `description` | ❌ |

## 3. 接口

```java
ImportResult<ExcelEDimRow> execute(InputStream xlsxStream, String sheetName)
```

`sheetName` 可以是 E1, E2, ..., E8。

## 4. 测试场景

| # | 场景 | 预期 |
|:-:|------|------|
| TC-1 | 1 行 E1 数据 → 解析成功 | |
| TC-2 | 1 行 E2 数据 → 解析成功 | |
| TC-3 | 缺 ID → errors | |
| TC-4 | 缺 名称 → errors | |
| TC-5 | 缺 维度 → errors | |
| TC-6 | 可见性枚举值非法 → errors | |
| TC-7 | 空 Sheet → 0 | |
| TC-8 | Sheet 不存在 → 异常 | |
