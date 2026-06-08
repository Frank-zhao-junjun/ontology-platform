# US-A01 校验证据 — manufacturing Manifest

> 执行环境：`E:\00 - AI\Ontology\repo-main`（设计台）  
> 执行时间：2026-06-04

## 1. 自动化测试（设计台）

```powershell
cd "E:\00 - AI\Ontology\repo-main"
corepack pnpm vitest run tests/unit/manifest-validator.spec.ts tests/unit/manifest-export.spec.ts tests/unit/manifest-manufacturing-golden.spec.ts
```

**结果**

| 指标 | 值 |
|------|-----|
| Test Files | 3 passed (3) |
| Tests | **16 passed (16)** |
| Duration | ~1.2s |

覆盖范围：

- `manifest-validator.spec.ts` — V01、V03、V04、V10、STRUCTURE 等规则
- `manifest-export.spec.ts` — 导出包结构
- `manifest-manufacturing-golden.spec.ts` — **本交接包主 YAML** 与 golden 对齐

## 2. 规范校验规则 V01–V11

| 编号 | 规则 | 主 YAML `manufacturing-manifest.yaml` | 最小导出 `manufacturing-manifest-compiler-minimal.yaml` |
|------|------|:-------------------------------------:|:--------------------------------------------------------:|
| V01 | `apiVersion` 受支持 | 通过（`ontology.platform/v1`） | 通过 |
| V02 | `metadata.version` semver | 通过（`0.1.0`） | 通过（`1.0.0`） |
| V03 | 至少 1 个 `aggregate_root` | 通过 | 通过 |
| V04 | 每个 `entity` 有有效 `aggregateRootId` | 通过 | 通过 |
| V05 | 每个 `action` 绑定存在的 `aggregateRootId` | 通过 | 通过 |
| V06 | `preRuleIds` 引用存在的 `rules` | 通过 | 通过 |
| V07 | `publishesEventIds` 引用存在的 `domainEvents` | 通过 | 通过 |
| V08 | 领域事件 `nameEn` 过去式（警告级） | 无 error | 无 error |
| V09 | 每状态机恰 1 个 `isInitial: true` | 通过 | 通过 |
| V10 | 无明文凭证（仅 `*SecretRef`） | 通过 | 通过（无 dataSources） |
| V11 | 文档内 `id` 唯一 | 通过 | 通过 |

**汇总**：设计台 validator 对两份 YAML 均为 **valid=true，errors=[]**（以 vitest golden / validator 用例为准）。

## 3. 类型检查

```powershell
corepack pnpm run ts-check
```

**结果**：passed（与 Round-1 交接清单一致）。

## 4. Manifest 摘要（主输入，AC-5 对齐）

| 项 | 值 |
|----|-----|
| `metadata.id` | `manufacturing-ontology` |
| `metadata.version` | `0.1.0` |
| `metadata.boundedContext` | 生产制造 |
| `metadata.source` | `ontology-designer` |
| `metadata.status` | `draft` |
| 聚合根（`kind: aggregate_root`） | 4（production-order, material, bom, routing） |
| 实体 | 1（operation） |
| 行为 `actions` | 3 |
| 规则 `rules` | 2 |
| 领域事件 | 3 |
| 角色 | 2 |
| Agent 沙箱策略 | 1（MCP 三件套白名单） |
| 数据源 | 1（`authSecretRef`，无明文） |

## 5. 与平台样例一致性

主 YAML 与 `ontology-platform/docs/shared/examples/manufacturing-manifest.yaml` **内容一致**（同源 golden），便于项目2 直接 diff / import smoke。
