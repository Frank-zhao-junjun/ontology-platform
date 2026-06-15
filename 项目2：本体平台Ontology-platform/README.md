# 项目2：ontology-platform 治理平台

- **代码仓库**：[`../ontology-platform`](../ontology-platform)（GitHub: [ontology-platform](https://github.com/Frank-zhao-junjun/ontology-platform)）
- **技术栈**：Spring Boot DDD + PostgreSQL/AGE + `web-ui`
- **职责**：本体 CRUD、Manifest 导入/校验/发布、版本、治理、MCP
- **不负责**：Next.js 设计台、浏览器本地 Zustand 草稿编辑

## 文档

| 路径 | 说明 |
|------|------|
| [../docs/shared/PRD-本体建模平台-UserStoryMap-v1.2.md](../docs/shared/PRD-本体建模平台-UserStoryMap-v1.2.md) | 故事地图 v1.2.1（MCP 三步；§0 双产品） |
| [../docs/shared/PRD-本体建模平台-v2.0.md](../docs/shared/PRD-本体建模平台-v2.0.md) | 产品 PRD + [TDD](../docs/shared/TDD-本体建模平台-v2.0.md) / [API](../docs/shared/API契约-本体建模平台-v2.0.yaml) |
| [../docs/shared/ontology-manifest-spec.md](../docs/shared/ontology-manifest-spec.md) | Manifest 格式与 V01–V11 校验 |
| [../docs/superpowers/specs/phase1-spec-v1.md](../docs/superpowers/specs/phase1-spec-v1.md) | Phase 1 实施 Spec |
| [../docs/superpowers/specs/phase2-spec-v1.md](../docs/superpowers/specs/phase2-spec-v1.md) | Phase 2 实施 Spec（Final） |
| `prd/` | 平台专属 PRD 片段（可选） |
| `plans/` | 里程碑与 Issue 拆解（可选） |

## 关联 PR

- Web UI 分支：[PR #1](https://github.com/Frank-zhao-junjun/ontology-platform/pull/1)

## 交接

接收项目1 导出的 YAML，按 [docs/shared/ontology-manifest-spec.md](../docs/shared/ontology-manifest-spec.md) 校验后入库并发布；样例见 [docs/shared/examples/manufacturing-manifest.yaml](../docs/shared/examples/manufacturing-manifest.yaml)。Agent 仅消费 **published** Manifest。
