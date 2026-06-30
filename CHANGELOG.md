# Changelog

本体模型服务平台（Ontology Service Platform）变更记录。

---

## [2026-06-30] 联调与 CI 加固

### Added
- **GitHub Actions E2E 流水线**：`.github/workflows/e2e.yml`，main/release 分支 push/PR 自动触发
- **Maven `integration-test` profile**：绑定 `maven-failsafe-plugin`，统一运行 `*IT.java` / `*E2ETest.java`
- **Docker E2E 使用说明**：`TODO.md` P2#11 补充本地 `mvn verify -Pintegration-test` 与 CI 触发方式

### Fixed
- **编译修复**：恢复 `OntologyImportRequest` / `OntologyImportResponse` 被误删的字段（`autoPublish`、`validationMode`、`status`、`totalEntities`、`warnings`）
- **测试路径修复**：21 个 Controller 测试由 `/v1/` 同步为 `/api/v1/`，修复 111 个测试失败
- **MCP 编译修复**：`auto-entity-tools.ts` 去除重复 import/常量，修正含 `*/` 的 JSDoc 注释
- **MCP 测试修复**：按 upload 自动注册的新行为重写 `ontology-lifecycle.test.ts`

### Changed
- 全量测试规模：Maven 174 → **176** tests，MCP 28 tests，合计 **204 tests / 0 failures**
- `TODO.md` 最后更新日期更新为 2026-06-30

---

## [Phase 3] — 2026-06-19 ~ 2026-06-26

### Added
- **V2 交换契约**：`ontology.platform/v2`，`spec.project` = `OntologyProject` 1:1 映射
- **V12 领域模型**（8 表）：departments, positions, business-metrics, orchestrations, process-steps, metadata-templates, business-terms, agent-intents
- **V13 语义层**（6 表）：semantic-relations, intent-slots, agent-policies-semantic, error-recoveries, semantic-field-mappings, entity-lifecycle-snapshots
- **V14 EPC**（5 表）：epc-chains, epc-nodes, epc-edges, epc-model-refs, epc-profiles
- **项目1→项目2 导入 API**：`POST /api/v1/ontologies/import`，支持 JSON 全量导入
- **跨项目 E2E 测试**：6 场景（CROSS-1~6），覆盖导入/发布/导出/拒绝
- **Ontology 模型生命周期**：upload → apply → disable → re-apply 全流程测试
- LifecycleValidator（V-LC-01~15）
- SemanticValidator + EpcVxValidator
- EPC 覆盖 API + 语义层查询 + 生命周期查询
- Markdown 导入支持
- Health 详情端点（构建信息、JVM 版本、测试统计）

### Fixed
- Legacy 启动阻塞修复
- Dev-profile 测试失败修复
- Lombok 与 @Schema 注解冲突

### Changed
- `load` → `upload` 语义重构
- `staging+apply` 模式替换直接注册

---

## [Phase 2] — 2026-06-14 ~ 2026-06-20

### Added
- **Agent 编排 API**：ACP 协议接入 Kimi/Claude/Codex，`POST /api/v1/agents/tasks`
- **JobQueue**：异步任务提交/查询/取消（Redis 支持）
- **RateLimiter**：令牌桶限流
- **Webhook**：Job 完成/失败回调订阅
- **CI 流水线**：GitHub Actions（1m26s），Feishu 通知
- **项目1→2 数据映射**：10 张新表（V9+V10）Flyway SQL + Domain Entity + PO/Mapper/XML + Service/DTO/Controller
- 导入适配器（Excel/JSON/YAML）
- AgentBridgeService + Python 桥接脚本

### Fixed
- DDD 架构违规修复：application 层不再依赖 infrastructure PO/Mapper
- Controller 测试修复（@MockBean、双括号、addFilters）

---

## [Phase 1] — 2026-06-13 ~ 2026-06-14

### Added
- **Manifest 导入/校验/发布/导出**：V01-V11 校验规则责任链
- **MCP Server**：Express + MCP SDK，resolve_intent / query_ontology / traverse_graph / validate_instruction / execute_action
- **Governance 模块**：AgentToken、Role、Permission、Approval
- **Domain 扩展**：Action、StateMachine、Event、EpcStep 实体与仓储
- Flyway V2-V6 迁移
- Phase 0+1 Spec + 用户故事

---

## [Phase 0] — 2026-06-08 ~ 2026-06-13

### Added
- 基础本体 CRUD：Ontology、ObjectType、Property、Relation
- 图遍历查询（Apache AGE）：traverse / paths / subgraph
- 多格式初始化数据加载（JSON/CSV/YAML/Excel）
- Health 端点 + Prometheus 指标
- MCP Server 骨架
- CI 门禁（compile + unit + IT）
- Phase 0 Spec + TDD v2.0 规范

### Changed
- Java 8 → JDK 21 升级
- Spring Boot 2.x → 3.2.5
- MyBatis → MyBatis-Plus
