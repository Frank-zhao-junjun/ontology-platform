# TODO — Ontology Platform

> 最后更新：2026-06-26
> 来源：[P1 短期](./docs/import/TODO.md)、[差距分析](./docs/shared/项目1-项目2对接差距分析.md)、[数据映射](./docs/project1-to-project2-mapping.md)
> 项目1 待办：[`../Ontology/docs/TODO.md`](../Ontology/docs/TODO.md)

---

## 🔴 P0 — 阻塞联调

| # | 任务 | 来源 | 说明 |
|---|------|------|------|
| 1 | Import 管道增强 | 差距分析 P0#2 | 支持 JSON 全量 + Excel parsedData 映射 |
| 2 | 实体角色映射 | 差距分析 P0#3 | `child_entity` ↔ 存储层 `entity` 统一 |
| 3 | businessScenarioId 支持 | 差距分析 P0#4 | 限界上下文下新增场景表或 JSONB 索引 |
| 4 | 校验器分层 | 差距分析 P0#5 | V01-V11 保留 + 插件化 VE/VM/VX/V-LC/V-AS |

## 🟡 P1 — MCP 运行时

| # | 任务 | 来源 | 说明 |
|---|------|------|------|
| 5 | `resolve_intent` 接入 Semantic Layer | 差距分析 P2#12 | Intent → Action 查询，非硬编码 |
| 6 | `query_ontology` 增强 | 差距分析 P2#13 | 覆盖 Lifecycle 聚合 + EPC 覆盖报告 |
| 7 | 组织上下文 | 差距分析 P2#14 | Agent 权限链：Position → Role → Permission |
| 8 | 校验 API 暴露 | 差距分析 P2#15 | 完整 validationReport 端点 |

## 🔵 P2 — 文档与测试

| # | 任务 | 来源 | 说明 |
|---|------|------|------|
| 9 | TDD v2 → v3 | 差距分析 P3#17 | 覆盖新校验规则与 V2 导入 |
| 10 | PRD/故事地图 US-D → US-P | 差距分析 P3#16 | 交接条款更新 |
| 11 | Docker E2E 验证 | TODO #3 | 本机待有 Docker/PostgreSQL 环境 |
| 12 | 项目1 docs/shared/ 同步 | 审计发现 | ✅ 项目1 已建 `docs/shared/README.md` 跳转；权威源仍在本仓库 `docs/shared/` |

## ⚪ P3 — 技术债务

| # | 任务 | 来源 | 说明 |
|---|------|------|------|
| 13 | 规则/接口/指标 10 表代码层 | 数据映射 | validation_rule 等 10 表无 PO/Mapper/Service/Controller |
| 14 | RelationRepositoryImpl → 数据库 | AGENTS.md | 当前内存存储，需替换为 PostgreSQL 实现 |
| 15 | queryObjects 接入实际数据 | AGENTS.md | 当前返回空结果集 |

---

## ✅ 已完成速览

| 阶段 | 内容 |
|------|------|
| Phase 0 | 基础 CRUD + 图查询 |
| Phase 1 | Manifest V01-V11 + MCP Server |
| Phase 2 | JobQueue + RateLimiter + Webhook + Agent 编排 + CI |
| Phase 3a | V2 交换契约 + Import API |
| Phase 3b-d | V12-V14 新域 19 表全链路 |
| E2E | 6 场景跨项目导入/导出（`Project1ToProject2E2ETest`） |
| 测试 | **174** tests, 0 failures · CI ~1m26s |
| 文档 | README / TODO / import TODO 与项目1 双向链接（2026-06-26） |

### 联调状态（2026-06-26）

| 项目 | CI | 测试规模 | 备注 |
|------|:--:|----------|------|
| 项目1 `D:\AI\Ontology` | `ci:check` ✅ | ~1049（760 unit + 259 integration + 30 e2e） | Manifest 编译导出就绪 |
| 项目2 本仓库 | GitHub Actions ✅ | 174 unit | Import API + V12–V14 已上线 |

详细导入进度见 [`docs/import/TODO.md`](./docs/import/TODO.md)。
