# P2-I02-U01：Manifest 格式转换器 — 项目1简化模型 → 项目2 ManifestDocument

| Unit ID | P2-I02-U01 |
|:-------:|:----------:|
| **预估文件** | 2 个（转换器 + 测试） |

## 1. 目标

将项目1简化模型的 `OntologyManifest`（JSON）转换为项目2的 `ManifestDocument`，使现有 ManifestController 能正确识别项目1的导入。

## 2. 映射方案

| 项目1字段 | 项目2 ManifestDocument 路径 |
|:---------|:--------------------------|
| `metadata.name` | `.metadata.name` |
| `spec.semantic` 中的 A/B/C | `.spec.semantic.objectTypes`（kind=A/B/C） |
| `spec.behavior` | `.spec.behavior.actions/.rules/.stateMachines` |
| `spec.events` | `.spec.events.domainEvents` |
| `spec.process`（EPC） | `.spec.epc[]` |

## 3. 接口

```java
ManifestDocument convert(String project1ManifestJson)
```

## 4. 测试

| # | 场景 | 预期 |
|:-:|------|------|
| TC-1 | 完整 Manifest → 所有字段正确映射 | |
| TC-2 | 空字段 → 空列表 | |
| TC-3 | 非法 JSON → 异常 | |
