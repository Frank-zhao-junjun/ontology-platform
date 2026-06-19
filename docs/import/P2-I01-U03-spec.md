# P2-I01-U03：Sheet C（Scenario）→ ObjectType 导入适配器

| Unit ID | P2-I01-U03 |
|:-------:|:----------:|
| **所属 US** | [URS-P2-I01](./URS-P2-I01.md) |
| **状态** | 草稿 |
| **依赖** | P2-I01-U02（同模式） |
| **预估文件** | 2 个（适配器 + 测试） |

## 1. 目标

将项目1 Excel 文件 Sheet C（Scenario 场景）的每行数据导入为项目2的 `ObjectType` 实体，隶属于指定 Ontology。

## 2. 范围

### 字段映射

| 项目1 Excel 列 | ObjectType 字段 | 说明 |
|:--------------:|:---------------:|:----|
| ID | `name` | 必填，如 SC-001 |
| 名称 | `displayName` | 必填 |
| 英文名 | — | 暂不映射 |
| 描述 | `description` | 可选 |
| 语义(JSON) | — | 校验 JSON 格式 |
| 父节点ID | `parentId` | 必填，指向 B 的 ID |

### 冲突检测

同一 Ontology 下 `ObjectType.name` 已存在 → skipped。

## 3. 接口签名

```java
ImportResult<ObjectType> execute(InputStream xlsxStream, String ontologyId, String createdBy)
```

## 4. PRD + TC

| # | 场景 | 预期 |
|:-:|------|------|
| TC-1 | 1 行数据 | 1 ObjectType，字段正确映射 |
| TC-2 | 多行数据 | 全部创建 |
| TC-3 | 缺 ID | errors |
| TC-4 | 缺父节点ID | errors |
| TC-5 | name 冲突 | skipped |
| TC-6 | 空 Sheet | 0 imported |
| TC-7 | Sheet 不存在 | 异常 |
| TC-8 | 非法文件 | 异常 |

## 5. 六步验证

- [ ] ① URS（本文档）
- [ ] ② PRD（§4 ×8 TCs）
- [ ] ③ Testing case（同上）
- [ ] ④ Coding — **Claude Code**
- [ ] ⑤ Unit test — **Copilot ACP**
- [ ] ⑥ E2E N/A
