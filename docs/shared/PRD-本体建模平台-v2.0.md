# 本体建模平台 PRD v2.0

> 版本：v2.0
> 状态：Draft
> 日期：2026-06-14
> 基于：PRD v1.0 + Phase 1 实施反馈 + MCP Server 现状

---

## 1. 版本演进

| 版本 | 日期 | 变更 |
|------|------|------|
| v1.0 | 2026-06-09 | 初始 PRD：五层本体建模、MCP Agent 交互、治理 |
| v2.0 | 2026-06-14 | 增量：Phase 1 实施完成后的修正，对齐现有代码与 MCP Server |

---

## 2. 产品定位（不变）

本体建模平台是面向企业 AI Agent 的本体语义层平台。它把企业业务知识拆成三类可治理资产：

- **语义**：业务是什么（领域、场景、对象、属性、关系、状态、数据获取方式）
- **行为**：业务能做什么（行为、前置校验、规则、事务边界、指标定义、权限）
- **事件**：业务发生了什么（领域事件、事件存储、事件路由、因果链）

平台通过 **MCP Server** 向 Agent 暴露本体能力，使 LLM 在理解业务、执行动作、解释结论时必须以本体为语义标准。

---

## 3. 产品架构（v2.0 更新）

```
                         AI Agent (Hermes / Codex / ...)
                              │
                              ▼ MCP Protocol (JSON-RPC 2.0)
                    ┌─────────────────────┐
                    │   MCP Server (:3001) │
                    │  ┌─────────────────┐ │
                    │  │ 工具:           │ │
                    │  │ · resolve_intent│ │
                    │  │ · query_ontology│ │
                    │  │ · traverse_graph│ │
                    │  │ · validate_instr│ │
                    │  │ · execute_action│ │
                    │  └────────┬────────┘ │
                    │  ┌────────┴────────┐ │
                    │  │ REST Client     │ │
                    │  │ (platform-      │ │
                    │  │  client.ts)     │ │
                    │  └────────┬────────┘ │
                    └────────┬──┴──────────┘
                             │ API Key + JWT
                             ▼
              ┌─────────────────────────────┐
              │  Spring Boot API (:8080)    │
              │  Ontology / ObjectType /    │
              │  Relation / GraphTraversal  │
              └──────────────┬──────────────┘
                             │
              ┌──────────────┴──────────────┐
              │  PostgreSQL + Apache AGE    │
              └─────────────────────────────┘
```

## 4. 核心模块（v2.0 更新）

### 4.1 语义层

| 模块 | v2.0 状态 | 说明 |
|------|-----------|------|
| 领域管理 | ✅ MVP 完成 | 本体 CRUD |
| 对象类型管理 | ✅ MVP 完成 | ObjectType CRUD |
| 属性管理 | ✅ MVP 完成 | Property CRUD |
| 关系管理 | ✅ MVP 完成 | Relation CRUD |
| 图遍历查询 | ✅ MVP 完成 | GraphTraversal (AgeQueryExecutor/InMemory 降级) |

### 4.2 行为层

| 模块 | v2.0 状态 | 说明 |
|------|-----------|------|
| Flyway V3 行为表 | ✅ 完成 | action_definition, state_machine, state_transition |
| Action 查询 API | ✅ 完成 | GET /api/v1/ontologies/{id}/actions |
| 状态机校验 | ✅ 完成 | 初始状态/终态/transition 校验 |

### 4.3 事件层

| 模块 | v2.0 状态 | 说明 |
|------|-----------|------|
| Flyway V4 事件表 | ✅ 完成 | domain_event, causality |
| Event 查询 API | ✅ 完成 | GET /api/v1/ontologies/{id}/events |
| 因果链校验 | ✅ 完成 | 无环检测 |

### 4.4 EPC 编排层

| 模块 | v2.0 状态 | 说明 |
|------|-----------|------|
| Flyway V5 EPC 表 | ✅ 完成 | epc_step |
| EPC 查询 API | ✅ 完成 | GET /api/v1/ontologies/{id}/epc |

### 4.5 治理层

| 模块 | v2.0 状态 | 说明 |
|------|-----------|------|
| Flyway V6 治理表 | ✅ 完成 | agent_token, agent_role, role_permission, approval_request |
| Token 签发/吊销 | ✅ 完成 | POST/GET/DELETE /api/v1/governance/tokens |
| 角色权限 CRUD | ✅ 完成 | 角色定义、权限绑定、审批流 |
| RBAC 过滤 | ✅ 完成 | MCP Server 侧 domain 过滤 + riskLevel 门控 |

### 4.6 Manifest

| 模块 | v2.0 状态 | 说明 |
|------|-----------|------|
| Flyway V2 Manifest 表 | ✅ 完成 | manifest_import, manifest_version |
| Validator V01~V11 | ✅ 完成 | 责任链模式，11 个校验规则 |
| Import API | ✅ 完成 | POST /api/v1/manifests/import |
| Preview / Publish / Export | ✅ 完成 | diff 预览、版本快照、YAML/JSON 导出 |

### 4.7 MCP Server

| 模块 | v2.0 状态 | 说明 |
|------|-----------|------|
| Express 骨架 | ✅ 完成 | POST /mcp, GET /health |
| JSON-RPC 2.0 处理器 | ✅ 完成 | tools/list, tools/call, initialize |
| 5 个固定工具 | ✅ 完成 | resolve_intent, query_ontology, traverse_graph, validate_instruction, execute_action |
| 动态工具编译 | ✅ 完成 | 从 Manifest JSON 动态注册工具 |
| 版本化工具 | ✅ 完成 | name_v1, name_v2, sunset 机制 |
| JWT Auth | ✅ 完成 | Bearer → AgentContext |
| RBAC | ✅ 完成 | domain 过滤 + riskLevel 门控 |
| Platform REST Client | ✅ 完成 | actions/events/epc/tokens/approvals |
| Vitest 测试 | ✅ 6/6 | smoke.test.ts |
| **Docker 化** | ❌ 待完成 | |
| **CI pipeline** | ❌ 待验证 | .github/workflows/ci.yml mcp-server gate |

---

## 5. v2.0 增量需求

### 5.1 已完成（从 v1.0 升级到 v2.0 状态）

| 需求 | 对应实现 | 状态 |
|------|---------|------|
| Flyway V2~V6 迁移 | 6 个 migration 文件 | ✅ |
| 11 条 Manifest 校验规则 | V01~V11 责任链 | ✅ |
| MCP Server 完整链路 | 5 工具 + RBAC + Auth | ✅ |
| 行为/事件/EPC 查询 API | 3 个 Controller | ✅ |
| Governance 模块 | Token/Role/Permission/Approval | ✅ |
| UploadTask + ImportTask | Flyway V7 + 全部 DDD 四层 | ✅ |
| 幂等 + TraceId + JobQueue | Phase 2a~2c | ✅ |

### 5.2 待完成

| 需求 | 说明 | 优先级 |
|------|------|--------|
| MCP → Spring Boot E2E 打通 | platform-client.ts 需对应真实的 Spring Boot API | P0 |
| Docker Compose 集成 | mcp-server Dockerfile + docker-compose 编排 | P1 |
| CI 中 mcp-server 测试门 | 确保 npm test 在 CI 中执行 | P1 |

---

## 6. 成功指标（v2.0 更新）

| 指标 | v1.0 目标 | v2.0 实测 | 说明 |
|------|:---------:|:---------:|------|
| 后端测试用例数 | — | 106 单元 + 73 IT | ✅ |
| MCP Server 测试 | — | 6/6 | ✅ |
| API 覆盖率 | — | 8 个 Controller | ✅ |
| Flyway 迁移数 | — | V1~V7 | ✅ |
| Manifest 校验规则 | — | V01~V11 | ✅ |

---

## 7. 暂不做（v2.0 不变）

- 不存储完整业务实例数据
- 不做通用工作流引擎
- 不做 BI 报表
- 不直接训练 LLM
- 不开放 Agent 静默修改正式本体
