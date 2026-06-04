# 共用文档（项目1 + 项目2）

本目录存放**两个产品共享**的规格与故事地图（位于 `ontology-platform` 仓库内，与代码同库版本管理）。

## 权威文档

| 文件 | 说明 |
|------|------|
| [PRD-本体建模平台-UserStoryMap-v1.2.md](./PRD-本体建模平台-UserStoryMap-v1.2.md) | 用户故事地图（**当前执行版 v1.2.1**，含 MCP 三步链路） |
| [archive/PRD-本体建模平台-UserStoryMap-v1.1.md](./archive/PRD-本体建模平台-UserStoryMap-v1.1.md) | v1.1 快照归档 |
| [archive/PRD-本体建模平台-UserStoryMap.md](./archive/PRD-本体建模平台-UserStoryMap.md) | v1.0-draft 归档 |

## v2.0 实施包（平台）

| 文件 | 说明 |
|------|------|
| [PRD-本体建模平台-v2.0.md](./PRD-本体建模平台-v2.0.md) | 产品战略 PRD（8 章） |
| [TDD-本体建模平台-v2.0.md](./TDD-本体建模平台-v2.0.md) | 技术设计（10 章，§5 三通道 / 三步 MCP） |
| [API契约-本体建模平台-v2.0.yaml](./API契约-本体建模平台-v2.0.yaml) | REST + MCP 内部 API |
| [Manifest-Schema-v2.0.json](./Manifest-Schema-v2.0.json) | 平台编译输出 Manifest JSON Schema |
| [Sprint计划-本体建模平台-v2.0.md](./Sprint计划-本体建模平台-v2.0.md) | Sprint 排期 |

## 契约与样例

| 文件 | 说明 |
|------|------|
| [ontology-manifest-spec.md](./ontology-manifest-spec.md) | 设计台↔平台交接（`ontology.platform/v1`） |
| [examples/manufacturing-manifest.yaml](./examples/manufacturing-manifest.yaml) | 制造域样例 |

## MCP 静态工具三件套（MVP）

沙箱 `allowedMcpTools` / `allowedTools` 须包含：

1. `resolve_intent` — 自然语言 → 概念引用（US-A03 Step 1）
2. `query_ontology` — 概念引用 → 完整定义（US-A03 Step 2）
3. `execute_action` — 统一行为执行（US-A05；`action` + `parameters`）

每行为独立 MCP Tool（如 `execute_production_order_release`）为 **P1 / US-A04**，非 MVP 默认。

## 产品边界（速查）

| | 项目1 Ontology 设计台 | 项目2 ontology-platform |
|--|------------------------|-------------------------|
| 代码 | `Ontology` 仓库（设计台） | 本仓库 `ontology-platform` |
| 交付 | 导出 YAML 草稿 | 导入 → 发布 → MCP |
| 工作区说明 | `本体建模/项目1：生成本体模型/` | `本体建模/项目2：本体平台Ontology-platform/`（指向本仓库） |

## Sprint 交付记录

| Sprint | 记录 |
|--------|------|
| Sprint 1 | [sprint-records/Sprint1-交付总结-2026-06-04.md](./sprint-records/Sprint1-交付总结-2026-06-04.md) |

## 修改约定

- 故事地图：只改 **UserStoryMap-v1.2.md**；`UserStoryMap-v1.1.md` 仅为跳转 stub。
- 破坏性 Manifest 交接格式：升 `apiVersion`（spec）；编译产物升 `manifestVersion`（Schema v2.0）。
- MCP 链路变更：同步 USM、PRD §3.6/§4.3、TDD §5、API、Sprint 6、样例 YAML。

