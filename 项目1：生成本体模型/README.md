# 项目1：Ontology 设计台

- **代码仓库**：`e:\00 - AI\Ontology\repo-main`（GitHub: [Ontology](https://github.com/Frank-zhao-junjun/Ontology)）
- **技术栈**：Next.js + React + Zustand persist
- **职责**：persist 建模草稿、五层建模 UI、预览、**导出 OntologyManifest YAML**
- **不负责**：服务端权威存储、Manifest 发布、MCP、企业 RBAC/Agent 沙箱运行时

## 文档

| 路径 | 说明 |
|------|------|
| [../ontology-platform/docs/shared/PRD-本体建模平台-UserStoryMap-v1.2.md](../ontology-platform/docs/shared/PRD-本体建模平台-UserStoryMap-v1.2.md) | 共用故事地图 v1.2.1（附录 F = 设计台 US-D） |
| `prd/` | 设计台专属 PRD 片段（可选） |
| `plans/` | 实施计划（如 YAML 导出、US-D01～D05） |

## 交接

设计台导出的 YAML 由项目2 导入，契约见 [../ontology-platform/docs/shared/ontology-manifest-spec.md](../ontology-platform/docs/shared/ontology-manifest-spec.md)，样例见 [../ontology-platform/docs/shared/examples/manufacturing-manifest.yaml](../ontology-platform/docs/shared/examples/manufacturing-manifest.yaml)。

### US-A01 交接包（2026-06-04）

任务 **US-A01 manufacturing manifest handoff package** 四件套：

| 交付物 | 路径 |
|--------|------|
| Manifest | [handoff/US-A01/manufacturing-manifest.yaml](./handoff/US-A01/manufacturing-manifest.yaml) |
| 校验证据 | [handoff/US-A01/validation-evidence.md](./handoff/US-A01/validation-evidence.md) |
| 导入步骤 | [handoff/US-A01/import-steps.md](./handoff/US-A01/import-steps.md) |
| 问题清单 | [handoff/US-A01/issues-checklist.md](./handoff/US-A01/issues-checklist.md) |

索引：[handoff/US-A01/README.md](./handoff/US-A01/README.md)
