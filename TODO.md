# TODO — Ontology Platform

> 最后更新：2026-06-30
> 来源：[P1 短期](./docs/import/TODO.md)、[差距分析](./docs/shared/项目1-项目2对接差距分析.md)、[数据映射](./docs/project1-to-project2-mapping.md)
> 项目1 待办：[`../Ontology/docs/TODO.md`](../Ontology/docs/TODO.md)

---

## 🔴 P0 — 阻塞联调

| # | 任务 | 来源 | 说明 |
|---|------|------|:----:|
| 1 | Import 管道增强 | 差距分析 P0#2 | ✅ 统一到 v2 管道，`Project1JsonToExchangeConverter` + autoPublish |
| 1 | Import 管道增强 | 差距分析 P0#2 | ✅ 统一到 v2 管道，`Project1JsonToExchangeConverter` + autoPublish |
| 2 | 实体角色映射 | 差距分析 P0#3 | ✅ v2 全链路统一 `aggregate_root`/`child_entity` |
| 3 | businessScenarioId 支持 | 差距分析 P0#4 | ✅ Flyway V20 + BusinessScenario 全链路 |
| 4 | 校验器分层 | 差距分析 P0#5 | ✅ 106 条规则，7 插件 (VE/VM/VX/V-LC/V-AS/V-ORG/Manifest) |

## 🟡 P1 — MCP 运行时

| # | 任务 | 来源 | 说明 |
|---|------|------|------|
| 5 | `resolve_intent` 接入 Semantic Layer | 差距分析 P2#12 | ✅ 4-phase: trigger→BusinessTerm synonym→SemanticRelation→name fallback |
| 6 | `query_ontology` 增强 | 差距分析 P2#13 | ✅ EPC coverage 含 uncovered actions/events；路径 /v1→/api/v1 修复 |
| 7 | 组织上下文 | 差距分析 P2#14 | ✅ V21 governance_role 表 + PositionEntry.roleId + OrganizationContextService |
| 8 | 校验 API 暴露 | 差距分析 P2#15 | ✅ POST /api/v2/exchanges/validate — 106 rules, no-persist |

## 🔵 P2 — 文档与测试

| # | 任务 | 来源 | 说明 |
||---|------|------|------|
| 9 | TDD v2 → v3 | 差距分析 P3#17 | ✅ `TDD-本体建模平台-v3.0.md` |
| 10 | PRD/故事地图 US-D → US-P | 差距分析 P3#16 | ✅ `PRD-本体建模平台-v3.0.md` |
| 11 | Docker E2E 验证 | TODO #3 | ✅ GitHub Actions E2E workflow + integration-test profile |
| 12 | 项目1 docs/shared/ 同步 | 审计发现 | ✅ 项目1 已建 `docs/shared/README.md` 跳转 |

## ⚪ P3 — 技术债务

| # | 任务 | 来源 | 说明 |
|---|------|------|------|
| 13 | 规则/接口/指标 10 表代码层 | 数据映射 | validation_rule 等 10 表 | ✅ 全部有 Service + Controller |
| 14 | RelationRepositoryImpl → 数据库 | AGENTS.md | 当前内存存储 | ✅ MyBatis-Plus 实现 |
| 15 | queryObjects 接入实际数据 | AGENTS.md | 当前返回空结果集 | ✅ 分页查询全链路已实现 |

---

## ✅ 已完成速览

| 阶段 | 内容 |
|------|------|
| Phase 0 | 基础 CRUD + 图查询 |
| Phase 1 | Manifest V01-V11 + MCP Server |
| Phase 2 | JobQueue + RateLimiter + Webhook + Agent 编排 + CI |
| Phase 3a | V2 交换契约 + Import API + 校验器 106 rules |
| Phase 3b-d | V12-V14 新域 19 表全链路 + BusinessScenario + GovernanceRole |
| P1 MCP | resolve_intent (4-phase) + query_ontology (EPC coverage) + 组织上下文 + 校验 API |
| Phase 2 | JobQueue + RateLimiter + Webhook + Agent 编排 + CI |
| E2E | 6 场景跨项目导入/导出（`Project1ToProject2E2ETest`） |
| P0 | 差距分析 6 项全部关闭 |
| 测试 | **174** tests, 0 failures · CI ~1m26s |
| 文档 | README / TODO / import TODO 与项目1 双向链接（2026-06-26） |

### 联调状态（2026-06-26）

| 项目 | CI | 测试规模 | 备注 |
|------|:--:|----------|------|
| 项目1 `D:\AI\Ontology` | `ci:check` ✅ | ~1049（760 unit + 259 integration + 30 e2e） | Manifest 编译导出 + golden 验证 |
| 项目2 本仓库 | GitHub Actions ✅ | 174 unit | Import API + V12–V14 + 校验器 106 rules |
| **US-A01 联调** | ✅ | 6 E2E 场景 | Project1ToProject2E2ETest 全链路: import→validate→publish |

详细导入进度见 [`docs/import/TODO.md`](./docs/import/TODO.md)。
