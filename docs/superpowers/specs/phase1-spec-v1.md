# ontology-platform Phase 1 Spec v1.0

> 基于 US v1.1 | 2026-06-13 | Draft

---

## 1. 范围

| 板块 | US | 新增组件 |
|------|-----|---------|
| 发现层 | P01, P02, P03, P03b | ManifestController, Validator, Service |
| 动力层 | P04 | action_definition, state_machine, state_transition |
| 事件层 | P05 | domain_event, causality |
| 编排层 | P06 | epc_step |
| MCP 协议 | P07, P09, P10, P11 | mcp-server/ (独立 Node.js :3001) |
| 治理层 | P08 | agent_token, agent_role, role_permission, approval_request |

### 拓扑

Agent (LLM) -> MCP (Streamable HTTP) -> MCP Server (:3001) -> REST (API Key + 内网) -> Spring Boot (:8080) -> PG + AGE + Redis

---

## 2. 架构决策

| 决策 | 选择 | 理由 |
|------|------|------|
| MCP 传输 | Streamable HTTP | 无状态，替代 SSE |
| 工具签名 | Manifest 动态编译 | 设计台改 -> MCP 自动更新 |
| Agent 认证 | JWT RS256 + RBAC | 每 Agent 独立 token |
| DB 迁移 | Flyway V2~V6 | 已有 V1 基础表 |

---

## 3. 数据模型 (12 张新表)

| 表 | Flyway | US |
|----|--------|-----|
| manifest_import | V2 | P01 |
| manifest_version | V2 | P03 |
| action_definition | V3 | P04 |
| state_machine | V3 | P04 |
| state_transition | V3 | P04 |
| domain_event | V4 | P05 |
| causality | V4 | P05 |
| epc_step | V5 | P06 |
| agent_token | V6 | P08 |
| agent_role | V6 | P08 |
| role_permission | V6 | P08 |
| approval_request | V6 | P08 |

### 3.1 DDL

```sql
-- V2__create_manifest_tables.sql
CREATE TABLE manifest_import (
    id UUID PRIMARY KEY, ontology_id UUID NOT NULL,
    external_id VARCHAR(255) NOT NULL,                     -- 设计台原始 ID (如 manufacturing-ontology)
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

-- V3__create_action_state_machine.sql
CREATE TABLE action_definition (
    id UUID PRIMARY KEY, ontology_id UUID NOT NULL,
    entity_id VARCHAR(255) NOT NULL, name VARCHAR(200) NOT NULL,
    display_name VARCHAR(500), description TEXT,
    action_type VARCHAR(50) NOT NULL, input_schema JSONB DEFAULT '{}',
    output_schema JSONB DEFAULT '{}', pre_rules JSONB DEFAULT '[]',
    post_rules JSONB DEFAULT '[]', domain VARCHAR(200),
    risk_level VARCHAR(20) DEFAULT 'READ', is_async BOOLEAN DEFAULT FALSE,
    timeout_ms INTEGER DEFAULT 30000,
    created_at TIMESTAMPTZ DEFAULT now(), updated_at TIMESTAMPTZ DEFAULT now(),
    deleted BOOLEAN DEFAULT FALSE
);
CREATE TABLE state_machine (
    id UUID PRIMARY KEY, ontology_id UUID NOT NULL,
    entity_id VARCHAR(255) NOT NULL, name VARCHAR(200) NOT NULL,
    initial_state VARCHAR(100) NOT NULL, states JSONB DEFAULT '[]',
    created_at TIMESTAMPTZ DEFAULT now(), updated_at TIMESTAMPTZ DEFAULT now(),
    deleted BOOLEAN DEFAULT FALSE
);
CREATE TABLE state_transition (
    id UUID PRIMARY KEY, state_machine_id UUID NOT NULL REFERENCES state_machine(id),
    from_state VARCHAR(100) NOT NULL, to_state VARCHAR(100) NOT NULL,
    trigger VARCHAR(200) NOT NULL, guard_condition VARCHAR(500),
    created_at TIMESTAMPTZ DEFAULT now()
);

-- V4__create_domain_event.sql
CREATE TABLE domain_event (
    id UUID PRIMARY KEY, ontology_id UUID NOT NULL,
    entity_id VARCHAR(255) NOT NULL, name VARCHAR(200) NOT NULL,
    display_name VARCHAR(500), description TEXT,
    event_type VARCHAR(50) NOT NULL, severity VARCHAR(20) DEFAULT 'INFO',
    payload_schema JSONB DEFAULT '{}', source VARCHAR(200),
    created_at TIMESTAMPTZ DEFAULT now(), updated_at TIMESTAMPTZ DEFAULT now(),
    deleted BOOLEAN DEFAULT FALSE
);
CREATE TABLE causality (
    id UUID PRIMARY KEY, ontology_id UUID NOT NULL,
    cause_event_id UUID NOT NULL REFERENCES domain_event(id),
    effect_event_id UUID NOT NULL REFERENCES domain_event(id),
    description TEXT, delay_ms INTEGER DEFAULT 0, condition VARCHAR(500),
    created_at TIMESTAMPTZ DEFAULT now(),
    CONSTRAINT uq_causality UNIQUE (cause_event_id, effect_event_id)
);

-- V5__create_epc_step.sql
CREATE TABLE epc_step (
    id UUID PRIMARY KEY, ontology_id UUID NOT NULL,
    flow_name VARCHAR(200) NOT NULL, step_order INTEGER NOT NULL,
    trigger_event_id UUID REFERENCES domain_event(id),
    action_id UUID REFERENCES action_definition(id),
    conditions JSONB DEFAULT '[]', guards JSONB DEFAULT '[]',
    timeout_ms INTEGER DEFAULT 60000,
    created_at TIMESTAMPTZ DEFAULT now(), updated_at TIMESTAMPTZ DEFAULT now(),
    CONSTRAINT uq_epc_flow_step UNIQUE (flow_name, step_order)
);

-- V6__create_governance_tables.sql
CREATE TABLE agent_token (
    id UUID PRIMARY KEY, agent_id VARCHAR(200) NOT NULL UNIQUE,
    token_hash VARCHAR(500) NOT NULL, tenant_id VARCHAR(100) NOT NULL,
    display_name VARCHAR(500), status VARCHAR(20) DEFAULT 'ACTIVE',
    issued_at TIMESTAMPTZ DEFAULT now(), expires_at TIMESTAMPTZ NOT NULL,
    last_used_at TIMESTAMPTZ, created_by VARCHAR(100),
    created_at TIMESTAMPTZ DEFAULT now()
);
CREATE TABLE agent_role (
    id UUID PRIMARY KEY, token_id UUID NOT NULL REFERENCES agent_token(id),
    domain VARCHAR(200) NOT NULL, role VARCHAR(50) NOT NULL,
    granted_at TIMESTAMPTZ DEFAULT now(),
    CONSTRAINT uq_token_domain UNIQUE (token_id, domain)
);
CREATE TABLE role_permission (
    id UUID PRIMARY KEY, role_id UUID NOT NULL REFERENCES agent_role(id),
    resource VARCHAR(200) NOT NULL, operations JSONB DEFAULT '[]',
    domain VARCHAR(200) NOT NULL, created_at TIMESTAMPTZ DEFAULT now()
);
CREATE TABLE approval_request (
    id UUID PRIMARY KEY, agent_id VARCHAR(200) NOT NULL,
    action_id UUID REFERENCES action_definition(id),
    requested_op VARCHAR(50) NOT NULL, status VARCHAR(20) DEFAULT 'PENDING',
    reason TEXT, requested_at TIMESTAMPTZ DEFAULT now(),
    resolved_at TIMESTAMPTZ, resolved_by VARCHAR(100)
);
```

---

## 4. REST API 契约

### 统一响应: { code, message, data, meta: { trace_id, version, generated_at } }

### 4.1 Manifest (P01-P03b)

| 方法 | 端点 | 说明 |
|------|------|------|
| POST | /api/v1/manifests/import | 导入 YAML/JSON -> { draftId, externalId, importedCounts, warnings } |
| POST | /api/v1/manifests/{id}/preview | 变更预览 -> { changes, diff } |
| POST | /api/v1/manifests/{id}/publish | 发布 -> { version, publishedAt } |
| GET | /api/v1/manifests/{id}/export | 导出 -> file download |

### 4.2 校验链 (P02) — V01~V11 责任链

V01 apiVersion | V02 semver | V03 >=1 aggregate_root | V04 entity ref
V05 action ref | V06 event ref | V07 no plaintext creds | V08 unique id
V09 single initial state | V10 EPC refs | V11 no causality cycle

### 4.3 Domain 查询 (P04-P06)

| 端点 | 字段 |
|------|------|
| GET /api/v1/ontologies/{id}/actions?entityId= | name, type, preRules, stateMachine, riskLevel |
| GET /api/v1/ontologies/{id}/events?entityId= | name, type, severity, causalities |
| GET /api/v1/ontologies/{id}/epc?flowName= | stepOrder, triggerEvent, action, conditions, guards |

### 4.4 Governance (P08)

| 方法 | 端点 |
|------|------|
| POST | /api/v1/governance/tokens (签发, token 仅返回一次) |
| GET | /api/v1/governance/tokens (admin 列表) |
| DELETE | /api/v1/governance/tokens/{id} (吊销) |
| POST | /api/v1/governance/roles |
| POST | /api/v1/governance/permissions |
| POST | /api/v1/governance/approvals |
| PUT | /api/v1/governance/approvals/{id} |

---

## 5. MCP Server

### 5.1 项目结构

```
mcp-server/src/
  index.ts                  Express + MCP transport
  mcp/server.ts             MCP Server 实例
  mcp/tools/
    registry.ts             Tool registry
    resolve-intent.ts       Fixed tool
    query-ontology.ts       Fixed tool
    traverse-graph.ts       Fixed tool
    validate-instruction.ts Fixed tool
    execute-action.ts       Dynamic tool
  auth/middleware.ts        JWT verify -> AgentContext
  auth/rbac.ts              domain + role -> tool filter
  client/platform-client.ts REST proxy to Spring Boot
  types/index.ts
```

### 5.2 MCP 端点

```
POST /mcp  { jsonrpc:"2.0", method:"tools/list" }
POST /mcp  { jsonrpc:"2.0", method:"tools/call", params:{name,arguments} }
```

### 5.3 工具分类

**固定 (4):** resolve_intent, validate_instruction, traverse_graph, query_ontology
**动态:** {domain}.{actionName} (由 Manifest 编译)

### 5.3b IntentCategory 枚举

```typescript
enum IntentCategory {
  QUERY    = "QUERY",     // 查询类：查订单、查库存
  CREATE   = "CREATE",    // 创建类：新建订单
  UPDATE   = "UPDATE",    // 更新类：修改状态
  DELETE   = "DELETE",    // 删除类：取消订单
  ANALYZE  = "ANALYZE",   // 分析类：趋势、聚合
  NAVIGATE = "NAVIGATE",  // 导航类：跳转到实体
  EXECUTE  = "EXECUTE",   // 执行类：触发流程
  UNKNOWN  = "UNKNOWN"    // 兜底
}
```

resolve_intent 输入 `{ query: string }`，输出 `{ category: IntentCategory, confidence: number, entities: string[], suggestedTool?: string }`

### 5.4 统一返回 (P09)

```typescript
{
  content: [{ type: "text" | "resource", text?: string }],
  structuredContent: {
    status: "success" | "error" | "pending_approval",
    data: unknown,
    metadata: { version, generated_at, trace_id, confidence?, derivation_chain? },
    error?: { code, message, details? }
  }
}
```

### 5.5 Auth 流程

Bearer token -> JWT verify -> agent_role -> role_permission
-> tools/list filtered by domain -> tools/call gated by operations
-> high risk (DELETE|APPROVAL) -> approval_request

---

## 6. 安全基线

| 层 | 措施 |
|----|------|
| Agent->MCP | JWT RS256, 90d expiry, bcrypt hash |
| MCP->Platform | API Key (Header) + 内网绑定 (Phase 2: mTLS) |
| 数据 | tenant_id 注入, PII masking |
| 操作 | approval flow, idempotency keys |
| 日志 | trace_id 全链路 |
| 限流 | Agent 100/min, IP 1000/min |

---

## 7. 实施顺序

| Phase | 内容 |
|-------|------|
| 1a | Flyway V2-V6 + Governance + Manifest import/export |
| 1b | Domain extensions (action, event, epc API) |
| 1c | MCP Server (Express + MCP SDK + tools + auth) |
| 1d | End-to-end integration |

---

## 8. 测试策略

| 层 | 框架 | 目标 |
|----|------|------|
| Domain | JUnit 5 | Business rules |
| Controller | MockMvc | Contract testing |
| Repository | Testcontainers | Real PG |
| MCP tools | Vitest | Tool logic + RBAC |
| MCP<->Platform | Supertest + nock | Integration |

每 US 最低: 2 unit + 1 integration

---

> 本 Spec 覆盖 US v1.1 全部 12 条 P0 故事。
> DDL 可直接用作 Flyway 迁移脚本。
> API 契约可生成 OpenAPI 3.0 YAML。
