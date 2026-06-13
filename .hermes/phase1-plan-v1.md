# ontology-platform Phase 1 Implementation Plan

> **For Hermes:** Use subagent-driven-development skill to implement this plan task-by-task.
> 基于 Spec v1.1 | 2026-06-13

**Goal:** 补齐 ontology-platform 四层架构（动力层/事件层/编排层/治理层）+ Manifest 导入导出 + MCP Server，让任意 AI Agent 可通过 MCP 查询完整的本体语义/行为/事件/EPC/权限。

**Architecture:** Spring Boot DDD (Java 21) 提供 REST API + Flyway 迁移 + Node.js MCP Server (Express + MCP SDK) 作为协议适配层。MCP→Platform 通过 API Key + 内网绑定通信。

**Tech Stack:**
- Backend: Spring Boot 3.2, Java 21, MyBatis-Plus, Flyway, PostgreSQL + AGE
- MCP: Node.js, Express 5, @modelcontextprotocol/sdk, Zod, jsonwebtoken
- Test: JUnit 5, MockMvc, Testcontainers (backend); Vitest, Supertest (MCP)

**实施顺序:** 1a → 1b → 1c → 1d

---

## Phase 1a: Flyway + Governance + Manifest

### Task 1a-1: Flyway V2 — manifest 表

**Objective:** 创建 `manifest_import` 和 `manifest_version` 两张表

**Files:**
- Create: `db/migrations/V2__create_manifest_tables.sql`

**Code:**

```sql
CREATE TABLE manifest_import (
    id UUID PRIMARY KEY, ontology_id UUID NOT NULL,
    external_id VARCHAR(255) NOT NULL,
    tenant_id VARCHAR(100) DEFAULT 'default', status VARCHAR(20) DEFAULT 'DRAFT',
    api_version VARCHAR(50) NOT NULL, manifest_version VARCHAR(50) NOT NULL,
    source_format VARCHAR(10) NOT NULL, raw_content JSONB NOT NULL,
    imported_counts JSONB DEFAULT '{}', validation_errors JSONB DEFAULT '[]',
    created_by VARCHAR(100), created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(), published_at TIMESTAMPTZ,
    CONSTRAINT uq_external_version UNIQUE (external_id, manifest_version)
);
CREATE TABLE manifest_version (
    id UUID PRIMARY KEY, ontology_id UUID NOT NULL,
    import_id UUID REFERENCES manifest_import(id),
    version VARCHAR(50) NOT NULL, manifest_json JSONB NOT NULL,
    change_summary JSONB DEFAULT '{}', created_at TIMESTAMPTZ DEFAULT now()
);
```

**Verify:** 
```bash
mvn flyway:migrate -pl ontology-infrastructure
# 确认 V2 成功 applied
```

---

### Task 1a-2: Flyway V3 — 行为/状态机表

**Objective:** 创建 `action_definition`, `state_machine`, `state_transition` 三张表

**Files:**
- Create: `db/migrations/V3__create_action_state_machine.sql`

**(DDL 见 Spec §3.1 — V3 完整 DDL)**

---

### Task 1a-3: Flyway V4 — 事件表

**Objective:** 创建 `domain_event`, `causality` 两张表

**Files:**
- Create: `db/migrations/V4__create_domain_event.sql`

**(DDL 见 Spec §3.1 — V4 完整 DDL)**

---

### Task 1a-4: Flyway V5 — EPC 表

**Objective:** 创建 `epc_step` 表

**Files:**
- Create: `db/migrations/V5__create_epc_step.sql`

**(DDL 见 Spec §3.1 — V5 完整 DDL)**

---

### Task 1a-5: Flyway V6 — 治理表

**Objective:** 创建 `agent_token`, `agent_role`, `role_permission`, `approval_request` 四张表

**Files:**
- Create: `db/migrations/V6__create_governance_tables.sql`

**(DDL 见 Spec §3.1 — V6 完整 DDL)**

---

### Task 1a-6: Governance — Token 签发与吊销

**Objective:** 实现 Agent Token 的 CRUD API（签发/列表/吊销）

**Files:**
- Create: `ontology-api/src/main/java/com/ontology/platform/api/controller/GovernanceController.java`
- Create: `ontology-application/src/main/java/com/ontology/platform/application/service/GovernanceService.java`
- Create: `ontology-application/src/main/java/com/ontology/platform/application/service/impl/GovernanceServiceImpl.java`
- Create: `ontology-domain/src/main/java/com/ontology/platform/domain/entity/AgentToken.java`
- Create: `ontology-domain/src/main/java/com/ontology/platform/domain/repository/AgentTokenRepository.java`
- Create: `ontology-infrastructure/src/main/java/com/ontology/platform/infrastructure/repository/AgentTokenRepositoryImpl.java`
- Create: DTOs: `CreateTokenRequest.java`, `TokenResponse.java`

**API:**
| POST   | /api/v1/governance/tokens       | 签发 (token 仅返回一次) |
| GET    | /api/v1/governance/tokens       | admin 列表 |
| DELETE | /api/v1/governance/tokens/{id}  | 吊销 |

**安全:** token 签发时用 bcrypt 存 hash，返回明文仅一次。JWT RS256 签发逻辑在本任务中完成。

---

### Task 1a-7: Governance — Role & Permission

**Objective:** 实现角色/权限/审批的 CRUD

**Files:**
- Create: `ontology-domain/src/main/java/.../entity/AgentRole.java`
- Create: `ontology-domain/src/main/java/.../entity/RolePermission.java`
- Create: `ontology-domain/src/main/java/.../entity/ApprovalRequest.java`
- Create: 对应 Repository / DTO

**API:**
| POST | /api/v1/governance/roles             | 创建角色 |
| POST | /api/v1/governance/permissions       | 绑定权限 |
| POST | /api/v1/governance/approvals         | 提交审批 |
| PUT  | /api/v1/governance/approvals/{id}    | 审批通过/拒绝 |
| GET  | /api/v1/governance/approvals/{id}    | 查询审批状态（Agent 轮询） |

**审批流闭环:**
- Agent 提交高 riskLevel 操作 → 服务端创建 approval_request (PENDING)
- 返回 `{ status: "pending_approval", approvalId }` 给 Agent
- Agent 轮询 `GET /approvals/{id}` 或等待 webhook 通知（webhook URL 从 Agent 注册信息获取）
- 管理员审批后 → 操作继续执行或拒绝

---

### Task 1a-8: Manifest — Validator (V01~V11)

**Objective:** 实现责任链模式的 Manifest 校验器，覆盖 V01~V11

**Files:**
- Create: `ontology-application/src/main/java/.../service/manifest/ManifestValidator.java`
- Create: `ontology-application/src/main/java/.../service/manifest/ValidationRule.java` (接口)
- Create: 11 个规则实现: `VxxRule.java` 在 `manifest/rules/` 下

**规则链:** V01 apiVersion → V02 semver → V03 ≥1 aggregate_root → V04 entity ref → V05 action ref → V06 event ref → V07 no plaintext creds → V08 unique id → V09 single initial state → V10 EPC refs → V11 no causality cycle

**返回:** `ManifestValidationResult { valid: boolean, errors: ValidationError[], warnings: ValidationWarning[] }`
每个 ValidationError: `{ code: string, elementType: string, id: string, field: string, message: string }`

**测试:** 为每个规则至少写 1 个 positive + 1 个 negative 测试用例

---

### Task 1a-9: Manifest — Import API

**Objective:** 实现 `POST /api/v1/manifests/import`

**Files:**
- Create: `ontology-api/src/main/java/.../controller/ManifestController.java`
- Create: `ontology-application/src/main/java/.../service/manifest/ManifestService.java`
- Create: `ontology-application/src/main/java/.../service/manifest/impl/ManifestServiceImpl.java`
- Create: DTOs: `ImportManifestRequest.java`, `ImportManifestResponse.java`

**流程:**
1. 接受 YAML/JSON body
2. 解析为统一 Java 对象 (ManifestDocument)
3. 调用 Validator 执行 V01~V11
4. 校验失败 → 返回 `{ code, message, data: { errors, warnings } }`，状态不变
5. 校验通过 → 写入 manifest_import (status=DRAFT) + 各 dimension 表
6. 返回 `{ draftId, externalId, importedCounts, warnings }`

**前提:** Task 1a-2~1a-5 的 DDL 已 applied，1b 的 Mapper/Repository 已创建（即使 1b 任务尚未执行完整逻辑，导入时仅需 INSERT 能力）。建议 1a-9 与 1b-1~1b-3 的 Repository 层并行准备。

**注意:** YAML 解析需要 SnakeYAML 或 Jackson YAML module（检查 pom.xml 是否已有）

---

### Task 1a-10: Manifest — Preview / Publish / Export

**Objective:** 实现预览/发布/导出三个端点

**Files:** (修改) ManifestController.java, ManifestServiceImpl.java

**Preview** `POST /api/v1/manifests/{id}/preview`
- 比较当前 draft 与上一个已发布版本的差异
- 返回 `{ changes: [...], diff: {...} }`

**Publish** `POST /api/v1/manifests/{id}/publish`
- status DRAFT → PUBLISHED
- 写入 manifest_version (快照)
- 更新 ontology 的当前版本引用
- 返回 `{ version, publishedAt }`

**Export** `GET /api/v1/manifests/{id}/export?format=yaml|json`
- 从 manifest_version 或当前已发布版本读取
- 反序列化为 Manifest 结构，重新导出 YAML/JSON
- 文件下载

---

## Phase 1b: Domain Extensions

> **共同要求:** 所有查询 API 必须按 `tenant_id` 隔离。tenant_id 从请求上下文注入（参见 RequestContextFilter），Repository 查询强制 `WHERE tenant_id = ?`。

### Task 1b-1: Action/Behavior Query API

**Objective:** 实现 `GET /api/v1/ontologies/{id}/actions`

**Files:**
- Create: `ontology-api/src/main/java/.../controller/BehaviorController.java`
- Create: `ontology-application/src/main/java/.../service/manifest/DomainQueryService.java` (或在 ManifestService 中扩充)
- Create: `ontology-domain/src/main/java/.../entity/ActionDefinition.java`
- Create: `ontology-domain/src/main/java/.../entity/StateMachine.java`
- Create: `ontology-domain/src/main/java/.../entity/StateTransition.java`
- Create: 对应 Repository + MyBatis mapper

**端点:** `GET /api/v1/ontologies/{id}/actions?entityId=`
**返回:** action name, type, preRules, stateMachine (含 transitions), riskLevel

---

### Task 1b-2: Event Query API

**Objective:** 实现 `GET /api/v1/ontologies/{id}/events`

**Files:**
- Create: `ontology-domain/.../entity/DomainEvent.java`
- Create: `ontology-domain/.../entity/Causality.java`
- Create: 对应 Repository + Controller + Service 方法

**端点:** `GET /api/v1/ontologies/{id}/events?entityId=`
**返回:** event name, type, severity, causalities (cause→effect 链)

---

### Task 1b-3: EPC Query API

**Objective:** 实现 `GET /api/v1/ontologies/{id}/epc`

**Files:**
- Create: `ontology-domain/.../entity/EpcStep.java`
- Create: 对应 Repository + Controller + Service 方法

**端点:** `GET /api/v1/ontologies/{id}/epc?flowName=`
**返回:** stepOrder, triggerEvent, action, conditions, guards

---

## Phase 1c: MCP Server

### Task 1c-1: MCP Server 骨架

**Objective:** 搭建 Express + MCP SDK 骨架，实现 `POST /mcp` 端点

**Files:**
- Modify: `mcp-server/package.json` (加 type: "module", ts 依赖等)
- Create: `mcp-server/tsconfig.json`
- Create: `mcp-server/src/index.ts` — Express app + MCP transport
- Create: `mcp-server/src/mcp/server.ts` — MCP Server 实例
- Create: `mcp-server/src/types/index.ts` — 类型定义 (IntentCategory, ToolSchema, 等)

**package.json 需加:** typescript, tsx (dev), @types/express, @types/jsonwebtoken, @types/cors, vitest (dev), supertest (dev)

**架构:** Express 监听 `:3001`，`POST /mcp` 路由接收 JSON-RPC 2.0 请求，转发给 MCP SDK 的 server 实例处理 `tools/list` 和 `tools/call`。

---

### Task 1c-2: Tool Registry — 动态编译

**Objective:** 实现工具注册中心，支持从 Manifest 动态编译工具签名

**Files:**
- Create: `mcp-server/src/mcp/tools/registry.ts`

**功能:**
- `registerFixedTools()` — 注册 4 个固定工具 (resolve_intent, validate_instruction, traverse_graph, query_ontology)
- `loadManifest(manifestJson)` — 从 Manifest JSON 动态编译 `{domain}.{actionName}` 工具
- `listTools(agentDomain?)` — 返回可用工具列表（按 domain 过滤）
- `getTool(name)` — 获取单个工具定义

**每个工具格式:**
```typescript
{
  name: string,          // "{domain}.{actionName}" 或固定名
  description: string,
  inputSchema: { type: "object", properties, required },
  outputSchema: { type: "object", properties },
  domain: string,
  riskLevel: "READ" | "WRITE" | "DELETE" | "APPROVAL"
}
```

---

### Task 1c-3: 固定工具实现

**Objective:** 实现 4 个固定工具的逻辑

**Files:**
- Create: `mcp-server/src/mcp/tools/resolve-intent.ts`
- Create: `mcp-server/src/mcp/tools/query-ontology.ts`
- Create: `mcp-server/src/mcp/tools/traverse-graph.ts`
- Create: `mcp-server/src/mcp/tools/validate-instruction.ts`

**resolve_intent:** 输入 `{ query }` → 输出 `{ category: IntentCategory, confidence, entities, suggestedTool }`
**query_ontology:** 调用 platform REST `GET /api/v1/ontologies/{id}/actions?entityId=` 等
**traverse_graph:** 调用 platform REST 的图遍历接口
**validate_instruction:** 校验 Agent 操作是否合规（前置条件/权限）

---

### Task 1c-4: 动态工具执行器

**Objective:** 实现 Manifest 动态编译的工具的 `execute_action` 分发器

**Files:**
- Create: `mcp-server/src/mcp/tools/execute-action.ts`

**功能:**
- `execute_action` 接收 `{ actionName, entityId, params }` 
- 调用 platform REST `GET /api/v1/ontologies/{id}/actions` 获取 action 定义
- 校验 preRules
- 根据 riskLevel 判断是否需要审批
- 触发相应 REST 调用（或返回到 Agent 继续交互）

---

### Task 1c-5: Auth 中间件

**Objective:** 实现 JWT 验证 + AgentContext 提取

**Files:**
- Create: `mcp-server/src/auth/middleware.ts`
- Create: `mcp-server/src/auth/rbac.ts`

**middleware.ts:** 提取 Bearer token → JWT verify (RS256) → 解析出 `AgentContext { agentId, domains, tenantId, roles }`
**rbac.ts:** 根据 `AgentContext.domain` 过滤工具列表；根据 `role_permission.operations` 门控工具调用

**Auth 流程 (Spec §5.5):**
```
Bearer token → JWT verify → agent_role → role_permission
→ tools/list filtered by domain
→ tools/call gated by operations
→ high risk (DELETE|APPROVAL) → approval_request
```

---

### Task 1c-6: Platform REST Client

**Objective:** 实现 MCP Server → Spring Boot 的 HTTP 代理客户端

**Files:**
- Create: `mcp-server/src/client/platform-client.ts`

**功能:**
- 封装所有对 Spring Boot REST API 的调用
- 携带 API Key header
- 统一错误处理
- 超时控制

**方法示例:**
```typescript
class PlatformClient {
  async queryActions(ontologyId: string, entityId: string): Promise<ActionDefinition[]>
  async queryEvents(ontologyId: string, entityId: string): Promise<EventDefinition[]>
  async queryEpc(ontologyId: string, flowName: string): Promise<EpcStep[]>
  async traverseGraph(params: TraverseParams): Promise<GraphResult>
  // ...
}
```

---

## Phase 1d: E2E Integration

### Task 1d-1: E2E Smoke Test

**Objective:** 编写端到端 smoke 测试，验证完整链路

**Files:**
- Create: `mcp-server/tests/e2e/smoke.test.ts`

**测试场景:**
1. Spring Boot 启动 (Testcontainers PG)
2. Flyway 执行 V2~V6
3. 导入 manufacturing-manifest.yaml
4. 校验通过 → draft 创建
5. preview → publish
6. export → 与导入一致
7. MCP Server 启动 → tools/list 返回动态工具
8. tools/call resolve_intent → 返回 IntentCategory

---

## 依赖关系图

```
1a-1~1a-5 (Flyway DDL) — 无依赖，可并行
        │
        ▼
1a-6 (Token) ──→ 1a-7 (Role/Perm) ──→ 1a-8 (Validator)
                                            │
                                            ▼
                                       1a-9 (Import) ──→ 1a-10 (Preview/Publish/Export)
                                            │
                                            ▼
                         1b-1~1b-3 (Action/Event/EPC Query) — 可并行
                                            │
                                            ▼
                         1c-2 (Registry) ←── 1c-1 (MCP Skeleton)
                          │                      │
                          ├── 1c-3 (Fixed tools)  ├── 1c-5 (Auth)
                          └── 1c-4 (Dynamic exec) └── 1c-6 (Platform Client)
                                            │
                                            ▼
                                       1d-1 (E2E Smoke)
```

---

## 验证清单

- [ ] `mvn test` 全部通过
- [ ] Flyway V2~V6 全部 applied
- [ ] 导入 manufacturing-manifest.yaml (handoff 包) 校验通过
- [ ] preview → publish → export round-trip 一致
- [ ] MCP `tools/list` 返回正确工具列表
- [ ] `tools/call resolve_intent` 返回 IntentCategory
- [ ] R ABC 过滤：不同 token 只能看到 domain 内工具
- [ ] 高风险操作触发 approval
