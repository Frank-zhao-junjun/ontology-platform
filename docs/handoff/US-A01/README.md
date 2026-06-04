# US-A01 — manufacturing Manifest 交接包

> **任务**：US-A01 编译本体为 Agent Manifest（制品层 P0）  
> **交接日期**：2026-06-04  
> **规范**：[ontology-manifest-spec.md](../shared/ontology-manifest-spec.md)（`ontology.platform/v1`）  
> **同源副本**：`项目1：生成本体模型/handoff/US-A01/`（设计台工作区）；本目录为 **项目2 同仓权威副本**。

## 交付物（4 件套）

| # | 文件 | 说明 |
|---|------|------|
| 1 | [manufacturing-manifest.yaml](./manufacturing-manifest.yaml) | **平台联调主输入** — 制造域完整 MVP（与 `ontology-platform/docs/shared/examples` 对齐） |
| 2 | [validation-evidence.md](./validation-evidence.md) | 设计台 V01–V11 校验与测试证据 |
| 3 | [import-steps.md](./import-steps.md) | 项目2 导入 / smoke 步骤（含当前 API 能力边界） |
| 4 | [issues-checklist.md](./issues-checklist.md) | 待确认与阻塞问题清单 |

## 附：编译器最小导出（回归用）

| 文件 | 说明 |
|------|------|
| [manufacturing-manifest-compiler-minimal.yaml](./manufacturing-manifest-compiler-minimal.yaml) | 自 `manifest-compile-project.json` 编译，用于 compiler 单测与 Round-1 最小联调 |

## 接收方

- **项目2** `ontology-platform`：import → 校验 → draft → publish  
- 联调分支建议：`web-ui`（截至 2026-06-04 已含 Sprint 1/2 REST + JPA 地基）
