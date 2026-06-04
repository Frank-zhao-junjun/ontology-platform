# US-A01 问题清单 — manufacturing Manifest 联调

> 状态图例：**Open** 待确认 · **Blocked** 阻塞联调 · **Resolved** 已关闭

## 0. Round 1 结果（Platform Import Smoke — dry-run）

| 项 | 结果 |
|----|------|
| 日期 | 2026-06-04 |
| 主输入 | `docs/handoff/US-A01/manufacturing-manifest.yaml` |
| 端点 | `POST /api/ontology/import/dry-run`、`POST /api/ontology/import/dry-run/us-a01-smoke` |
| 自动化 | `ManifestImportDryRunServiceTest` + `OntologyImportControllerTest` — **3/3 passed** |
| `valid` | **true** |
| `draftId` | `dry-run:manufacturing-ontology:0.1.0`（合成 ID，**未落库**） |
| V01–V11 | 全部通过（V08 无 error；dry-run 未产生 V08 warning） |

**importedCounts（Round 1 解析统计）**

| 键 | 数量 | 验收项 |
|----|------|--------|
| `boundedContext` | 1 | metadata/spec 信封 |
| `businessScenarios` | 2 | `applicableObjectTypeIds` 引用已校验 |
| `objectTypes` | 5 | 含 4× `aggregate_root` + 1× `entity` |
| `properties` | 10 | 含 `valueObjects` 属性 |
| `propertyFieldKeys` | 10 | `properties[].nameEn` → 平台字段键 |
| `relations` | 1 | `rel-po-bom` |
| `stateMachines` | 1 | `objectTypeId=production-order` |
| `actions` | 3 | `aggregateRootId` / `preRuleIds` / `publishesEventIds` 引用已校验 |
| `rules` | 2 | |
| `domainEvents` | 3 | |
| `roles` | 2 | 仅计数，Round 1 未写入 |
| `fieldPermissions` | 1 | `propertyNameEn=cost_price` 已解析 |
| `agentPolicies` | 1 | 仅计数 |
| `dataSources` | 1 | V10 无明文凭证 |

**warnings（预期）**

- `DRY_RUN: no database write; draftId is synthetic only`
- `IMPORT_SCOPE: governance parsed for counts only …`
- `IMPORT_SCOPE: behavior.metrics counted as metadata only …`
- `FIELD_KEY: stateMachine sm-production-order uses statusField=status …`

---

## 1. 阻塞项（平台 / 契约）

| ID | 问题 | 影响 | 建议 | 责任 | 状态 |
|----|------|------|------|------|------|
| P-01 | 平台无 **Manifest 一键 import** API（持久化草稿） | AC-2b 端到端仍不可 | 在 dry-run 之上实现 `POST …/import` 写库 | 项目2 | **Blocked**（Round 1 仅 dry-run **Resolved** 子项） |
| P-02 | 无 `manifests` 表持久化 / publish 流水线 | AC-3 round-trip 无法验收 | 按 TDD §3 编译器 + Flyway | 项目2 | **Blocked** |
| P-03 | `fieldPermissions` 键名：YAML 用 `propertyNameEn`，平台 G02 API 用 `fieldName` | 字段权限导入可能对不齐 | 统一为 `properties[].nameEn`（snake_case） | 双方 | **Open** |
| P-04 | `governance.roles[].permissions` 与平台 `POST /roles/{id}/object-permissions` 结构不一致 | 需映射层 | import 适配器或调整 YAML 扁平化 | 双方 | **Open** |
| P-05 | `agentPolicies` vs `POST /sandboxes` 字段名（`allowedMcpTools` / `allowedTools`） | 沙箱白名单可能漏导 | import 映射 + 文档 | 双方 | **Open** |

## 2. 字段约定（待平台书面确认）

| ID | 确认项 | 设计台结论 | 平台反馈（Round 1） | 状态 |
|----|--------|------------|---------------------|------|
| F-01 | DB 列名是否采用 `properties[].nameEn` | **是**（方案 A，snake_case） | dry-run 以 `nameEn` 统计 `propertyFieldKeys`（10 个） | **Resolved**（dry-run） |
| F-02 | `objectTypes[].id` 是否接受 kebab-case | **是** | `production-order` 等通过 V04/V05 引用校验 | **Resolved**（dry-run） |
| F-03 | `objectTypes[].id` 与 `code` 映射关系 | import 时 `code` 可用 `nameEn` 或 `id` | dry-run 未映射 `code` | Open |
| F-04 | import 失败错误 JSON 格式 | `{ code, elementType, id, field, message }` | dry-run `errors[]` 已按此结构 | **Resolved**（dry-run） |
| F-05 | import 成功最小返回 | `{ draftId, importedCounts, warnings }` | dry-run 已返回（`draftId` 为合成） | **Resolved**（dry-run） |

## 3. 设计台侧（非阻塞）

| ID | 问题 | 说明 | 状态 |
|----|------|------|------|
| D-01 | 编译器最小导出 vs 完整 golden 范围不同 | 最小包见 `manufacturing-manifest-compiler-minimal.yaml`；联调用完整包 | Open |
| D-02 | `value_object` 独立对象库 UI | P1，不阻塞 smoke | Open |
| D-03 | EPC 全文是否仅进 `extensions.epc` | 首轮平台不消费 | Open |
| D-04 | 每行为独立 MCP Tool（`mcpToolName`） | MVP 统一 `execute_action`；独立 tool 为 P1/US-A04 | Open |

## 4. 平台实现缺口（相对 Manifest 全量）

| Manifest 段 | 平台现状（web-ui @ 2026-06-04） | 状态 |
|-------------|----------------------------------|------|
| `spec.semantic` 全量 | dry-run 解析 + 部分 REST 落库能力 | Round 1 **dry-run OK**；写库进行中 |
| `spec.behavior` | dry-run 校验引用；无 US-B01/B03 API | Blocked（写库） |
| `spec.events` | dry-run 计数；无 US-E* API | Blocked（写库） |
| `spec.governance` | dry-run 计数；REST 部分落地 | 进行中 |
| `spec.dataSources` | dry-run 计数；REST 部分落地 | 进行中 |
| MCP `resolve_intent` / `query_ontology` | 未实现（US-A03） | Blocked |

## 5. 差异记录

| 轮次 | 日期 | 输入 | 平台结果 | 差异类型 | 处理 |
|------|------|------|----------|----------|------|
| 1 | 2026-06-04 | `manufacturing-manifest.yaml` | **dry-run valid=true**；见 §0 | 无阻塞性校验错误 | Round 2：持久化 import + 手工 REST 对照 |
| 1b | 2026-06-04 | 同上 | 未写库 / 无真实 `draftId` | 范围 | P-01/P-02 仍 Blocked |

## 6. Done 标准（US-A01 联调关闭）

- [x] 平台完成至少一轮 manufacturing Manifest **dry-run** smoke（Round 1）
- [ ] 平台完成至少一轮 **持久化 import** smoke
- [ ] P-01 / P-02 有里程碑或明确排期
- [x] F-01、F-02、F-04、F-05 有平台书面反馈（dry-run 范围）
- [ ] F-03 平台书面反馈
- [ ] 差异记录无未归属 **Blocked** 项（P-01/P-02 仍开放）
