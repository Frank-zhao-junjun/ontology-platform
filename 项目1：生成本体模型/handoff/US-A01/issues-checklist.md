# US-A01 问题清单 — manufacturing Manifest 联调

> 状态图例：**Open** 待确认 · **Blocked** 阻塞联调 · **Resolved** 已关闭

## 1. 阻塞项（平台 / 契约）

| ID | 问题 | 影响 | 建议 | 责任 | 状态 |
|----|------|------|------|------|------|
| P-01 | 平台无 **Manifest 一键 import** API | 无法完成 US-A01 AC-2b 端到端 | 实现 `POST /manifests/import` + V01–V11 | 项目2 | **Blocked** |
| P-02 | 无 `manifests` 表持久化 / publish 流水线 | AC-3 round-trip 无法验收 | 按 TDD §3 编译器 + Flyway | 项目2 | **Blocked** |
| P-03 | `fieldPermissions` 键名：YAML 用 `propertyNameEn`，平台 G02 API 用 `fieldName` | 字段权限导入可能对不齐 | 统一为 `properties[].nameEn`（snake_case） | 双方 | **Open** |
| P-04 | `governance.roles[].permissions` 与平台 `POST /roles/{id}/object-permissions` 结构不一致 | 需映射层 | import 适配器或调整 YAML 扁平化 | 双方 | **Open** |
| P-05 | `agentPolicies` vs `POST /sandboxes` 字段名（`allowedMcpTools` / `allowedTools`） | 沙箱白名单可能漏导 | import 映射 + 文档 | 双方 | **Open** |

## 2. 字段约定（待平台书面确认）

| ID | 确认项 | 设计台结论 | 平台反馈 | 状态 |
|----|--------|------------|----------|------|
| F-01 | DB 列名是否采用 `properties[].nameEn` | **是**（方案 A，snake_case） | 待填 | Open |
| F-02 | `objectTypes[].id` 是否接受 kebab-case（如 `production-order`） | **是**，稳定即可 | 待填 | Open |
| F-03 | `objectTypes[].id` 与 `code` 映射关系 | import 时 `code` 可用 `nameEn` 或 `id` | 待填 | Open |
| F-04 | import 失败错误 JSON 格式 | 期望 `{ code, elementType, id, field, message }` | 待填 | Open |
| F-05 | import 成功最小返回 | 期望 `{ draftId, importedCounts, warnings }` | 待填 | Open |

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
| `spec.semantic` 全量 | 部分（context / agg-root / object-type / relationship） | 进行中 |
| `spec.behavior` | 未实现 US-B01/B03 API | Blocked |
| `spec.events` | 未实现 US-E* API | Blocked |
| `spec.governance` | 部分（role / object-perm / field-perm / sandbox） | 进行中 |
| `spec.dataSources` | 部分（data-source + access-method） | 进行中 |
| MCP `resolve_intent` / `query_ontology` | 未实现（US-A03） | Blocked |

## 5. 差异记录

| 轮次 | 日期 | 输入 | 平台结果 | 差异类型 | 处理 |
|------|------|------|----------|----------|------|
| 1 | 2026-06-04 | `manufacturing-manifest.yaml` | **待平台 smoke** | — | 等待 B 节手工或 import API |

## 6. Done 标准（US-A01 联调关闭）

- [ ] 平台完成至少一轮 manufacturing Manifest import smoke
- [ ] P-01 / P-02 有里程碑或明确排期
- [ ] F-01–F-05 有平台书面反馈
- [ ] 差异记录无未归属 **Blocked** 项
