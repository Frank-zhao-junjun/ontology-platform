# {{UNIT_ID}}：{{SHEET}} → {{TARGET_ENTITY}} 导入适配器

| Unit ID | {{UNIT_ID}} |
|:-------:|:-----------:|
| **所属 US** | [URS-P2-I01](./URS-P2-I01.md) |
| **状态** | 草稿 |
| **依赖** | {{DEPENDENCIES}} |
| **预估文件** | {{FILE_COUNT}} 个 |

## 1. 目标

{{GOAL}}

## 2. 范围

### In Scope

{{IN_SCOPE}}

### Out of Scope

- 其他 Sheet（后续 Unit）
- 多 Sheet 交叉校验
- 文件上传/分片上传逻辑

## 3. 技术设计

### 新增文件

| 文件 | 说明 |
|:----|:-----|
| `{{DTO_PATH}}` | Excel 行数据 DTO（如与已有 DTO 共用则删除此行） |
| `{{REPO_PATH}}` | Repository 接口（如已有则删除此行） |
| `{{ADAPTER_PATH}}` | 导入适配器 |
| (测试) `{{TEST_PATH}}` | 单元测试 |

### 字段映射

| 项目1 Excel 列 | 项目2 领域对象字段 | 特殊处理 |
|:--------------:|:-----------------:|:--------:|
| {{COL_1}} | {{FIELD_1}} | {{HANDLING_1}} |
| {{COL_2}} | {{FIELD_2}} | {{HANDLING_2}} |
| ... | ... | ... |

### 冲突检测

{{CONFLICT_RULE}}

## 4. PRD 验收条款

| # | 验收项 | 验证方式 |
|:-:|--------|----------|
| AC-1 | {{AC_1}} | 单测 |
| AC-2 | {{AC_2}} | 单测 |
| AC-3 | {{AC_3}} | 单测 |
| AC-4 | {{AC_4}} | 单测 |
| AC-5 | 非法文件 → 异常 | 单测 |

## 5. 测试用例

**测试文件**：`{{TEST_PATH}}`

| # | 场景 | 输入 | 预期 |
|:-:|------|------|------|
| TC-1 | {{TC_1}} | {{TC_1_INPUT}} | {{TC_1_EXPECT}} |
| TC-2 | {{TC_2}} | {{TC_2_INPUT}} | {{TC_2_EXPECT}} |
| TC-3 | {{TC_3}} | {{TC_3_INPUT}} | {{TC_3_EXPECT}} |
| TC-4 | {{TC_4}} | {{TC_4_INPUT}} | {{TC_4_EXPECT}} |
| TC-5 | 空 Sheet | 仅标题行 | 0 imported |
| TC-6 | Sheet 不存在 | 无此 Sheet | 异常 |
| TC-7 | 非法文件 | 非 Excel 字节流 | 异常 |

## 6. 六步验证

- [ ] ① URS（本文档）
- [ ] ② PRD（§4 ×5 ACs）
- [ ] ③ Testing case（§5 ×7+ TC）
- [ ] ④ Coding（适配器 + DTO + 可能 Repository）
- [ ] ⑤ Unit test（全部通过）
- [ ] ⑥ E2E

## 7. 验证命令

```bash
cd D:\AI\ontology-platform
mvn test -pl ontology-infrastructure -am -Dtest={{TEST_CLASS}} -Dsurefire.failIfNoSpecifiedTests=false
```
