# 本体建模平台 — 技术设计文档 (TDD) v2.0

**版本**：V2.0  
**日期**：2026-06-04  
**关联文档**：[PRD v2.0](./PRD-本体建模平台-v2.0.md) | [User Story Map v1.2.1](./PRD-本体建模平台-UserStoryMap-v1.2.md)（32 Stories）

---

## 目录

1. [架构总览](#1-架构总览)
2. [数据模型设计](#2-数据模型设计)
3. [Manifest 编译器设计](#3-manifest-编译器设计)
4. [MCP Server 设计](#4-mcp-server-设计)
5. [三通道防御实现方案](#5-三通道防御实现方案)
6. [事件引擎设计](#6-事件引擎设计)
7. [REST API 设计](#7-rest-api-设计)
8. [技术选型终稿](#8-技术选型终稿)
9. [部署架构](#9-部署架构)
10. [性能与可扩展性](#10-性能与可扩展性)

---

## 1. 架构总览

### 1.1 系统边界

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          本体建模平台 系统边界                             │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │                    MCP Server（独立进程）                          │   │
│  │  Port :8090  |  tech: Spring Boot + McpSDK                        │   │
│  │  • MCP 协议实现（JSON-RPC over SSE/STDIO）                        │   │
│  │  • 加载 Manifest → 动态注册 MCP Tools                             │   │
│  │  • 三通道防御：白名单校验 + 本体查询注入 + 输出校验                  │   │
│  └────────────────────────────┬─────────────────────────────────────┘   │
│                               │ REST API (内部)                          │
│  ┌────────────────────────────▼─────────────────────────────────────┐   │
│  │                    本体建模平台（核心服务）                         │   │
│  │  Port :8080  |  tech: Spring Boot 3.2 + DDD 五模块                │   │
│  │                                                                     │   │
│  │  ┌───────────┐ ┌───────────┐ ┌───────────┐ ┌───────────┐        │   │
│  │  │ ontology  │ │ ontology  │ │ ontology  │ │ ontology  │        │   │
│  │  │ -api      │ │ -app      │ │ -domain   │ │ -infra    │        │   │
│  │  │ REST API  │ │ Services  │ │ Entities  │ │ Repos     │        │   │
│  │  └───────────┘ └───────────┘ └───────────┘ └───────────┘        │   │
│  │                                                                     │   │
│  │  核心引擎：                                                         │   │
│  │  ┌──────────────┐ ┌──────────────┐ ┌──────────────────┐           │   │
│  │  │ Manifest     │ │ Validation   │ │ Event           │           │   │
│  │  │ Compiler     │ │ Engine       │ │ Engine           │           │   │
│  │  └──────────────┘ └──────────────┘ └──────────────────┘           │   │
│  └────────────────────────────┬─────────────────────────────────────┘   │
│                               │                                         │
│  ┌────────────────────────────▼─────────────────────────────────────┐   │
│  │                        数据层                                      │   │
│  │  ┌────────────────┐  ┌────────────┐  ┌──────────────────────┐    │   │
│  │  │ PostgreSQL 15  │  │ Redis 7    │  │ Object Storage       │    │   │
│  │  │ (模型元数据     │  │ (缓存+     │  │ (Manifest 版本文件    │    │   │
│  │  │  + 事件存储)    │  │  会话)     │  │  + 审批快照)         │    │   │
│  │  └────────────────┘  └────────────┘  └──────────────────────┘    │   │
│  └──────────────────────────────────────────────────────────────────┘   │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

### 1.2 模块职责映射

| Maven 模块 | DDD 层 | 核心职责 | 关键类（示例） |
|-----------|--------|---------|-------------|
| `ontology-api` | 接口层 | REST Controller、DTO、OpenAPI 注解 | `BoundedContextController`, `BehaviorController` |
| `ontology-application` | 应用层 | 用例编排、事务管理、Manifest 编译 | `ManifestCompiler`, `ValidationService`, `WorkflowService` |
| `ontology-domain` | 领域层 | 实体、值对象、聚合根、领域服务接口 | `BoundedContext`, `AggregateRoot`, `Behavior`, `DomainEvent` |
| `ontology-infrastructure` | 基础设施层 | Repository 实现、MCP Client、事件总线 | `BoundedContextRepositoryImpl`, `McpToolRegistry` |
| `ontology-common` | 共享内核 | 工具类、异常、枚举、常量 | `Result<T>`, `BusinessException`, `OntologyLayer` |

### 1.3 核心设计原则

| 原则 | 实现方式 |
|------|---------|
| **元模型固定，用户模型灵活** | 平台 schema 固定（表结构），用户定义的属性/规则/事件 Payload 存 JSONB |
| **建模期与运行期分离** | E03/E04 建模期定义（存 DB）；E02 运行期配置（环境变量 + DB） |
| **渐进式事件架构** | MVP: 进程内 Spring Events → 生产: Kafka / PostgreSQL LISTEN/NOTIFY |
| **Manifest 是唯一交付物** | 所有建模操作最终产物 = 版本化 JSON Manifest；MCP Server 只读 Manifest |
| **默认拒绝，显式授权** | 沙箱零权限起步；权限变更即时生效（Redis 缓存失效） |

---

## 2. 数据模型设计

### 2.1 设计策略

> **核心决策**：使用 PostgreSQL 关系表 + JSONB 混合模型。平台自身的元模型（限界上下文、聚合根、行为等概念）用固定表结构；用户定义的动态内容（属性列表、校验规则表达式、事件 Payload 字段等）用 JSONB 列存储，兼顾查询灵活性和 Schema 演进。

### 2.2 语义层表结构

#### 2.2.1 bounded_contexts（限界上下文）

```sql
CREATE TABLE bounded_contexts (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(100) NOT NULL,          -- "生产制造"
    code            VARCHAR(50) NOT NULL UNIQUE,     -- "manufacturing"
    description     TEXT,
    domain_tag      VARCHAR(50),                     -- "manufacturing" | "quality" | "equipment" | "supply_chain"
    ontology_id     UUID NOT NULL UNIQUE,            -- 1:1 绑定本体
    workflow_state  VARCHAR(20) NOT NULL DEFAULT 'DRAFT',  -- DRAFT | IN_REVIEW | PUBLISHED
    created_by      VARCHAR(100),
    created_at      TIMESTAMP DEFAULT NOW(),
    updated_at      TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_bc_domain_tag ON bounded_contexts(domain_tag);
CREATE INDEX idx_bc_workflow_state ON bounded_contexts(workflow_state);
```

#### 2.2.2 business_scenarios（业务场景）

```sql
CREATE TABLE business_scenarios (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    context_id      UUID NOT NULL REFERENCES bounded_contexts(id) ON DELETE CASCADE,
    name            VARCHAR(100) NOT NULL,           -- "面向库存生产(MTS)"
    code            VARCHAR(50) NOT NULL,
    description     TEXT,
    is_active       BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT NOW(),
    UNIQUE(context_id, code)
);
```

#### 2.2.3 aggregate_roots（聚合根）

```sql
CREATE TABLE aggregate_roots (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    context_id      UUID NOT NULL REFERENCES bounded_contexts(id) ON DELETE CASCADE,
    name            VARCHAR(100) NOT NULL,           -- "生产订单"
    code            VARCHAR(50) NOT NULL,             -- "ProductionOrder"
    description     TEXT,
    is_active       BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT NOW(),
    UNIQUE(context_id, code)
);
```

#### 2.2.4 object_types（对象类型）

```sql
CREATE TABLE object_types (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    context_id      UUID NOT NULL REFERENCES bounded_contexts(id) ON DELETE CASCADE,
    aggregate_root_id UUID REFERENCES aggregate_roots(id) ON DELETE SET NULL,
    parent_object_id UUID REFERENCES object_types(id),  -- 继承父对象
    name            VARCHAR(100) NOT NULL,              -- "生产订单头"
    code            VARCHAR(50) NOT NULL,
    object_kind     VARCHAR(20) NOT NULL DEFAULT 'ENTITY',  -- ENTITY | VALUE_OBJECT | INTERFACE_ABSTRACT
    description     TEXT,
    -- 动态属性列表（JSONB）
    attributes      JSONB NOT NULL DEFAULT '[]',
    -- attributes 结构示例:
    -- [{
    --   "name": "order_id", "label": "生产订单号", "type": "STRING",
    --   "required": true, "primaryKey": true,
    --   "enumValues": null, "defaultValue": null,
    --   "sensitive": false, "sortOrder": 1
    -- }]
    is_active       BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT NOW(),
    UNIQUE(context_id, code)
);

CREATE INDEX idx_ot_aggregate ON object_types(aggregate_root_id);
CREATE INDEX idx_ot_attributes_gin ON object_types USING GIN (attributes);
```

#### 2.2.5 relationships（关系定义）

```sql
CREATE TABLE relationships (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    context_id      UUID NOT NULL REFERENCES bounded_contexts(id) ON DELETE CASCADE,
    name            VARCHAR(100) NOT NULL,              -- "包含"
    code            VARCHAR(50) NOT NULL,
    source_object_id UUID NOT NULL REFERENCES object_types(id),
    target_object_id UUID NOT NULL REFERENCES object_types(id),
    cardinality     VARCHAR(10) NOT NULL DEFAULT '1:N', -- 1:1 | 1:N | N:1 | N:M
    relation_kind   VARCHAR(20) NOT NULL DEFAULT 'REFERENCE', -- COMPOSITION | AGGREGATION | REFERENCE | DEPENDENCY
    is_cross_context BOOLEAN DEFAULT FALSE,              -- 是否跨上下文
    target_context_id UUID REFERENCES bounded_contexts(id),
    created_at      TIMESTAMP DEFAULT NOW(),
    CONSTRAINT chk_cardinality CHECK (cardinality IN ('1:1','1:N','N:1','N:M')),
    CONSTRAINT chk_relation_kind CHECK (relation_kind IN ('COMPOSITION','AGGREGATION','REFERENCE','DEPENDENCY'))
);
```

#### 2.2.6 value_objects（值对象 — 全局复用）

```sql
CREATE TABLE value_objects (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(100) NOT NULL,              -- "金额"
    code            VARCHAR(50) NOT NULL UNIQUE,         -- "Money"
    description     TEXT,
    -- 值对象的内部属性组合（JSONB）
    inner_attributes JSONB NOT NULL DEFAULT '[]',
    -- [{"name":"amount","type":"DECIMAL"}, {"name":"currency","type":"STRING"}]
    created_at      TIMESTAMP DEFAULT NOW()
);
```

#### 2.2.7 state_machines + states + state_transitions（状态机）

```sql
CREATE TABLE state_machines (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    object_type_id  UUID NOT NULL REFERENCES object_types(id) ON DELETE CASCADE,
    name            VARCHAR(100) NOT NULL,
    created_at      TIMESTAMP DEFAULT NOW(),
    UNIQUE(object_type_id)
);

CREATE TABLE states (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    machine_id      UUID NOT NULL REFERENCES state_machines(id) ON DELETE CASCADE,
    name            VARCHAR(50) NOT NULL,               -- "已下达"
    code            VARCHAR(30) NOT NULL,
    is_initial      BOOLEAN DEFAULT FALSE,
    is_terminal     BOOLEAN DEFAULT FALSE,
    sort_order      INT DEFAULT 0,
    UNIQUE(machine_id, code)
);

CREATE TABLE state_transitions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    machine_id      UUID NOT NULL REFERENCES state_machines(id) ON DELETE CASCADE,
    from_state_id   UUID NOT NULL REFERENCES states(id),
    to_state_id     UUID NOT NULL REFERENCES states(id),
    driving_behavior_id UUID,                           -- 驱动该转换的行为（见 behaviors 表）
    event_id        UUID,                               -- 转换后自动发布的领域事件
    precondition    JSONB,                              -- 前置条件表达式
    created_at      TIMESTAMP DEFAULT NOW(),
    UNIQUE(machine_id, from_state_id, to_state_id)
);
```

#### 2.2.8 data_sources + data_access_methods（数据源）

```sql
CREATE TABLE data_sources (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(100) NOT NULL,
    code            VARCHAR(50) NOT NULL UNIQUE,
    source_type     VARCHAR(20) NOT NULL,               -- SQL | API | MCP
    connection_config JSONB NOT NULL DEFAULT '{}',
    -- SQL:  {"jdbcUrl":"...", "username":"...", "credentialRef":"vault:sap-db"}
    -- API:  {"baseUrl":"...", "authType":"OAUTH2", "credentialRef":"vault:sap-api"}
    -- MCP:  {"serverName":"sap-odata-server", "toolName":"get_sales_order"}
    credential_ref  VARCHAR(200),                       -- 凭证引用（不存明文！指向 KMS/Vault）
    is_active       BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT NOW()
);

CREATE TABLE data_access_methods (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    object_type_id  UUID NOT NULL REFERENCES object_types(id) ON DELETE CASCADE,
    data_source_id  UUID NOT NULL REFERENCES data_sources(id),
    method_type     VARCHAR(20) NOT NULL,               -- SQL_QUERY | API_CALL | MCP_TOOL
    -- 获取方式的详细配置（JSONB）
    access_config   JSONB NOT NULL DEFAULT '{}',
    -- SQL:  {"query":"SELECT * FROM orders WHERE id = :orderId", "paramMapping": {...}}
    -- API:  {"endpoint":"/A_SalesOrder('{orderId}')", "method":"GET", "responseMapping":"$.d.TotalNetAmount"}
    -- MCP:  {"toolName":"get_sales_order", "paramSchema": {...}}
    cache_ttl_sec   INT DEFAULT 300,                    -- 消费端建议缓存时间
    created_at      TIMESTAMP DEFAULT NOW(),
    UNIQUE(object_type_id, data_source_id, method_type)
);
```

### 2.3 行为层表结构

#### 2.3.1 behaviors（行为定义）

```sql
CREATE TABLE behaviors (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    context_id      UUID NOT NULL REFERENCES bounded_contexts(id) ON DELETE CASCADE,
    aggregate_root_id UUID NOT NULL REFERENCES aggregate_roots(id),
    name            VARCHAR(100) NOT NULL,              -- "生产订单下达"
    code            VARCHAR(50) NOT NULL,                -- "release_production_order"
    description     TEXT,
    invocation_mode VARCHAR(20) NOT NULL DEFAULT 'BOTH', -- DIRECT | EVENT_DRIVEN | BOTH
    -- 输入参数（JSONB）
    input_params    JSONB NOT NULL DEFAULT '[]',
    -- [{"name":"orderId","type":"STRING","required":true,"description":"生产订单ID"}]
    -- 声明式事件发布列表（JSONB）
    published_events JSONB NOT NULL DEFAULT '[]',
    -- [{"eventCode":"production_order.released","eventName":"生产订单已下达"}]
    is_active       BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT NOW(),
    UNIQUE(context_id, code)
);

CREATE INDEX idx_behaviors_aggregate ON behaviors(aggregate_root_id);
```

#### 2.3.2 validation_rules + behavior_rule_bindings（校验规则）

```sql
CREATE TABLE validation_rules (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    context_id      UUID NOT NULL REFERENCES bounded_contexts(id) ON DELETE CASCADE,
    name            VARCHAR(100) NOT NULL,              -- "物料齐套校验"
    code            VARCHAR(50) NOT NULL,
    rule_type       VARCHAR(20) NOT NULL DEFAULT 'PRE_CHECK', -- PRE_CHECK | POST_CHECK | INVARIANT
    description     TEXT,                               -- 自然语言描述
    -- 结构化规则表达式（JSONB）
    rule_expression JSONB NOT NULL DEFAULT '{}',
    -- {
    --   "operator": "ANY",
    --   "conditions": [{
    --     "left": "material.stock_qty",
    --     "op": "<",
    --     "right": "order.required_qty",
    --     "aggregation": null
    --   }]
    -- }
    violation_message_template TEXT,                    -- "物料 {{materialCode}} 库存不足，缺口 {{gap}} 件"
    version         INT NOT NULL DEFAULT 1,
    is_active       BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT NOW(),
    UNIQUE(context_id, code)
);

CREATE TABLE behavior_rule_bindings (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    behavior_id     UUID NOT NULL REFERENCES behaviors(id) ON DELETE CASCADE,
    rule_id         UUID NOT NULL REFERENCES validation_rules(id) ON DELETE CASCADE,
    execution_order INT NOT NULL DEFAULT 0,              -- 规则执行顺序
    UNIQUE(behavior_id, rule_id)
);
```

#### 2.3.3 metrics（指标定义）

```sql
CREATE TABLE metrics (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    context_id      UUID NOT NULL REFERENCES bounded_contexts(id) ON DELETE CASCADE,
    name            VARCHAR(100) NOT NULL,              -- "准时完工率"
    code            VARCHAR(50) NOT NULL,
    description     TEXT,
    formula         TEXT NOT NULL,                      -- "COUNT(按时完工)/COUNT(总完工) * 100"
    -- 数据来源事件（JSONB）
    event_source    JSONB NOT NULL DEFAULT '{}',
    -- {"eventCode":"production_order.closed", "timestampField":"closed_at", "conditionField":"planned_completion_date"}
    aggregation_period VARCHAR(20) DEFAULT 'MONTHLY',   -- DAILY | WEEKLY | MONTHLY | QUARTERLY
    -- 聚合维度（JSONB）
    dimensions      JSONB DEFAULT '[]',
    -- [{"name":"product_line","path":"order.product_line"}, {"name":"workshop","path":"order.workshop"}]
    created_at      TIMESTAMP DEFAULT NOW(),
    UNIQUE(context_id, code)
);
```

#### 2.3.4 transaction_boundaries + side_effects

```sql
CREATE TABLE transaction_boundaries (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    behavior_id     UUID NOT NULL REFERENCES behaviors(id) ON DELETE CASCADE,
    -- 事务内操作列表（JSONB）
    operations      JSONB NOT NULL DEFAULT '[]',
    -- [{"objectCode":"ProductionOrder","field":"status","newValue":"RELEASED"},
    --  {"objectCode":"Material","field":"reserved_qty","delta":"-{{qty}}"},
    --  {"objectCode":"Operation","field":"status","newValue":"PENDING"}]
    created_at      TIMESTAMP DEFAULT NOW(),
    UNIQUE(behavior_id)
);

CREATE TABLE side_effects (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    behavior_id     UUID NOT NULL REFERENCES behaviors(id) ON DELETE CASCADE,
    effect_type     VARCHAR(20) NOT NULL,               -- NOTIFICATION | SYNC | LOG | WEBHOOK
    target          VARCHAR(200) NOT NULL,               -- 目标：人/系统/队列/URL
    content_template TEXT,                               -- 内容模板
    is_async        BOOLEAN DEFAULT TRUE,                -- 是否异步执行
    retry_policy    JSONB DEFAULT '{"maxRetries":3,"backoff":"exponential"}',
    created_at      TIMESTAMP DEFAULT NOW()
);
```

### 2.4 事件层表结构

#### 2.4.1 domain_events（领域事件）

```sql
CREATE TABLE domain_events (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    context_id      UUID NOT NULL REFERENCES bounded_contexts(id) ON DELETE CASCADE,
    name            VARCHAR(100) NOT NULL,              -- "生产订单已下达"
    code            VARCHAR(80) NOT NULL,                -- "production_order.released"
    event_kind      VARCHAR(20) NOT NULL DEFAULT 'DOMAIN',  -- DOMAIN | INTEGRATION
    triggering_behavior_id UUID REFERENCES behaviors(id),
    -- Payload Schema（JSONB）
    payload_schema  JSONB NOT NULL DEFAULT '[]',
    -- [{"name":"event_id","type":"STRING","required":true},
    --  {"name":"order_id","type":"STRING","required":true},
    --  {"name":"triggered_by","type":"STRING","required":true}]
    is_active       BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT NOW(),
    UNIQUE(context_id, code)
);
```

#### 2.4.2 event_routes（事件路由）

```sql
CREATE TABLE event_routes (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id        UUID NOT NULL REFERENCES domain_events(id) ON DELETE CASCADE,
    target_type     VARCHAR(20) NOT NULL,               -- CONTEXT | EXTERNAL_SYSTEM
    target_context_id UUID REFERENCES bounded_contexts(id),
    target_system   VARCHAR(100),                       -- "MES" | "ERP"
    route_condition JSONB,                              -- 路由过滤条件
    -- {"scenarioCode": "MTO"}  仅当业务场景=MTO时路由
    is_active       BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT NOW()
);
```

#### 2.4.3 event_handlers（事件处理器矩阵）

```sql
CREATE TABLE event_handlers (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id        UUID NOT NULL REFERENCES domain_events(id) ON DELETE CASCADE,
    scenario_id     UUID REFERENCES business_scenarios(id),
    -- 前置状态条件（NULL = 任意状态）
    required_state_id UUID REFERENCES states(id),
    -- 处理行为（引用行为库）
    handler_behavior_id UUID NOT NULL REFERENCES behaviors(id),
    description     TEXT,
    priority        INT DEFAULT 0,                     -- 匹配优先级（数值越大越优先）
    is_active       BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT NOW(),
    -- 一个(事件,场景,状态)组合最多一个处理器
    UNIQUE(event_id, scenario_id, required_state_id)
);
```

#### 2.4.4 event_store_configs（事件存储配置）

```sql
CREATE TABLE event_store_configs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    context_id      UUID NOT NULL REFERENCES bounded_contexts(id) ON DELETE CASCADE,
    store_type      VARCHAR(20) NOT NULL DEFAULT 'IN_PROCESS', -- IN_PROCESS | KAFKA | POSTGRES | REDIS_STREAM
    connection_config JSONB NOT NULL DEFAULT '{}',
    -- KAFKA:  {"bootstrapServers":"...", "topic":"ontology-events-{{context}}"}
    -- POSTGRES: {"schema":"event_store", "tablePrefix":"es_"}
    is_active       BOOLEAN DEFAULT FALSE,
    created_at      TIMESTAMP DEFAULT NOW(),
    UNIQUE(context_id)
);
```

### 2.5 治理层表结构

#### 2.5.1 roles + permissions（角色与权限）

```sql
CREATE TABLE roles (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    context_id      UUID REFERENCES bounded_contexts(id), -- NULL = 全局角色
    name            VARCHAR(50) NOT NULL,                 -- "生产计划员"
    code            VARCHAR(30) NOT NULL,
    description     TEXT,
    is_global       BOOLEAN DEFAULT FALSE,
    created_at      TIMESTAMP DEFAULT NOW(),
    UNIQUE(context_id, code)
);

CREATE TABLE role_permissions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role_id         UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    object_type_id  UUID NOT NULL REFERENCES object_types(id) ON DELETE CASCADE,
    perm_read       BOOLEAN DEFAULT FALSE,
    perm_write      BOOLEAN DEFAULT FALSE,
    perm_delete     BOOLEAN DEFAULT FALSE,
    perm_execute    BOOLEAN DEFAULT FALSE,               -- 行为执行权限
    UNIQUE(role_id, object_type_id)
);
```

#### 2.5.2 field_permissions（字段级权限）

```sql
CREATE TABLE field_permissions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role_id         UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    object_type_id  UUID NOT NULL REFERENCES object_types(id) ON DELETE CASCADE,
    field_name      VARCHAR(100) NOT NULL,               -- 匹配 attributes JSONB 中的 name
    is_visible      BOOLEAN DEFAULT TRUE,                -- 对无权限角色完全隐藏
    is_editable     BOOLEAN DEFAULT FALSE,
    UNIQUE(role_id, object_type_id, field_name)
);
```

#### 2.5.3 agent_sandboxes（AI Agent 沙箱）

```sql
CREATE TABLE agent_sandboxes (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(100) NOT NULL,
    manifest_version_id UUID,                            -- 绑定的 Manifest 版本
    agent_role_id   UUID REFERENCES roles(id),
    -- 白名单：允许的 MCP 工具（JSONB）
    allowed_tools   JSONB NOT NULL DEFAULT '[]',
    -- 白名单：允许的聚合根（JSONB）
    allowed_aggregate_roots JSONB NOT NULL DEFAULT '[]',
    -- 白名单：允许的行为（JSONB）
    allowed_behaviors JSONB NOT NULL DEFAULT '[]',
    max_ops_per_second INT DEFAULT 10,                   -- 频率限制
    is_active       BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT NOW()
);
```

#### 2.5.4 workflow_states + review_comments（建模工作流）

```sql
CREATE TABLE workflow_state_log (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    context_id      UUID NOT NULL REFERENCES bounded_contexts(id) ON DELETE CASCADE,
    from_state      VARCHAR(20) NOT NULL,                -- DRAFT | IN_REVIEW | PUBLISHED
    to_state        VARCHAR(20) NOT NULL,
    operated_by     VARCHAR(100) NOT NULL,
    operated_at     TIMESTAMP DEFAULT NOW(),
    comment         TEXT
);

CREATE TABLE review_comments (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    context_id      UUID NOT NULL REFERENCES bounded_contexts(id) ON DELETE CASCADE,
    -- 批注可挂载到任意建模元素
    target_type     VARCHAR(30) NOT NULL,                -- AGGREGATE_ROOT | OBJECT_TYPE | BEHAVIOR | EVENT | RULE
    target_id       UUID NOT NULL,
    reviewer        VARCHAR(100) NOT NULL,
    resolution      VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING | APPROVED | NEEDS_CHANGE | REJECTED
    content         TEXT NOT NULL,
    created_at      TIMESTAMP DEFAULT NOW(),
    resolved_at     TIMESTAMP
);

CREATE INDEX idx_rc_context ON review_comments(context_id);
CREATE INDEX idx_rc_target ON review_comments(target_type, target_id);
```

### 2.6 制品层表结构

```sql
CREATE TABLE manifests (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    context_id      UUID NOT NULL REFERENCES bounded_contexts(id),
    version         VARCHAR(20) NOT NULL,                -- 语义化版本 "1.0.0"
    semver_major    INT NOT NULL,
    semver_minor    INT NOT NULL,
    semver_patch    INT NOT NULL,
    -- Manifest 完整 JSON（七段元数据）
    manifest_json   JSONB NOT NULL,
    -- 编译摘要（JSONB）
    summary         JSONB NOT NULL DEFAULT '{}',
    -- {"aggregateRoots":4,"behaviors":6,"events":6,"roles":4,"validated":true}
    compiled_by     VARCHAR(100),
    compiled_at     TIMESTAMP DEFAULT NOW(),
    change_log      TEXT,                                -- 版本变更说明
    UNIQUE(context_id, version)
);

CREATE INDEX idx_manifests_context ON manifests(context_id);
CREATE INDEX idx_manifests_version ON manifests(context_id, semver_major, semver_minor, semver_patch DESC);
```

### 2.7 实体关系总图（核心表）

```
bounded_contexts ──1:N── business_scenarios
       │
       ├──1:N── aggregate_roots ──1:N── object_types ──1:1── state_machines ──1:N── states
       │                                                │                        └──1:N── state_transitions
       │                                                ├──1:N── relationships (source)
       │                                                ├──1:N── data_access_methods ──N:1── data_sources
       │                                                └──1:N── role_permissions ──N:1── roles
       │
       ├──1:N── behaviors ──1:N── behavior_rule_bindings ──N:1── validation_rules
       │        ├──1:1── transaction_boundaries
       │        ├──1:N── side_effects
       │        └──1:N── domain_events (triggering_behavior_id)
       │
       ├──1:N── domain_events ──1:N── event_routes
       │        └──1:N── event_handlers ──N:1── behaviors (handler_behavior_id)
       │                                      ──N:1── business_scenarios
       │
       ├──1:N── metrics
       ├──1:1── event_store_configs
       ├──1:N── review_comments
       ├──1:N── workflow_state_log
       └──1:1── manifests (latest)
```

---

## 3. Manifest 编译器设计

### 3.1 编译流程

```
┌─────────────────────────────────────────────────────────────────┐
│                    Manifest Compiler Pipeline                     │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  Step 1: 加载模型元数据                                           │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │ SELECT * FROM bounded_contexts WHERE id = :contextId      │   │
│  │   + aggregate_roots + object_types + relationships        │   │
│  │   + behaviors + validation_rules + domain_events          │   │
│  │   + event_handlers + roles + role_permissions             │   │
│  │   + data_sources + data_access_methods                    │   │
│  │   + agent_sandboxes                                       │   │
│  └──────────────────────────────────────────────────────────┘   │
│                           │                                       │
│                           ▼                                       │
│  Step 2: 校验完整性                                               │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │ • 聚合根 ≥ 1 个？                                          │   │
│  │ • 每个行为都关联了聚合根？                                  │   │
│  │ • 组合关系都在同一聚合根内？                                │   │
│  │ • 事件处理器引用的行为存在？                                │   │
│  │ • 状态转换引用的行为/事件存在？                             │   │
│  │ → 失败：返回结构化错误列表（定位到具体元素）                │   │
│  └──────────────────────────────────────────────────────────┘   │
│                           │                                       │
│                           ▼                                       │
│  Step 3: 编译为 Manifest JSON                                    │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │ ManifestCompiler.java                                      │   │
│  │                                                             │   │
│  │ Manifest manifest = Manifest.builder()                      │   │
│  │   .ontologyId(context.getOntologyId())                     │   │
│  │   .version(autoIncrement(contextId))                       │   │
│  │   // 七段元数据                                            │   │
│  │   .boundedContext(buildContextSection(context))            │   │
│  │   .entities(buildEntitySection(objectTypes, attrs))        │   │
│  │   .behaviors(buildBehaviorSection(behaviors, rules))       │   │
│  │   .events(buildEventSection(events, handlers, routes))     │   │
│  │   .permissions(buildPermissionSection(roles, perms))       │   │
│  │   .sandbox(buildSandboxSection(sandboxes))                 │   │
│  │   .dataSources(buildDataSourceSection(sources, methods))   │   │
│  │   .build();                                                │   │
│  └──────────────────────────────────────────────────────────┘   │
│                           │                                       │
│                           ▼                                       │
│  Step 4: 持久化 + 通知                                           │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │ • INSERT INTO manifests (context_id, version, manifest_json)│   │
│  │ • 上传到 Object Storage（S3/MinIO）作为不可变快照          │   │
│  │ • 发布 ManifestPublished 事件 → MCP Server 感知新版本      │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                   │
└─────────────────────────────────────────────────────────────────┘
```

### 3.2 Manifest JSON 结构（七段元数据）

```json
{
  "manifestVersion": "2.0",
  "ontology": {
    "id": "uuid",
    "name": "生产制造",
    "version": "1.0.0",
    "compiledAt": "2026-06-04T10:30:00Z"
  },
  "boundedContext": {
    "name": "生产制造",
    "code": "manufacturing",
    "domainTag": "manufacturing",
    "scenarios": [
      {"code": "MTS", "name": "面向库存生产"},
      {"code": "MTO", "name": "面向订单生产"}
    ]
  },
  "entities": [
    {
      "code": "ProductionOrder",
      "name": "生产订单",
      "isAggregateRoot": true,
      "objectKind": "ENTITY",
      "states": ["CREATED","RELEASED","EXECUTING","REPORTED","PRODUCED","CLOSED"],
      "attributes": [
        {"name":"orderId","type":"STRING","required":true,"primaryKey":true},
        {"name":"plannedQty","type":"MONEY","required":true,"sensitive":false}
      ],
      "children": [
        {
          "code": "Operation",
          "name": "工序",
          "objectKind": "ENTITY",
          "attributes": [...]
        }
      ]
    }
  ],
  "relations": [
    {
      "code": "contains_op",
      "name": "包含",
      "source": "ProductionOrder",
      "target": "Operation",
      "cardinality": "1:N",
      "kind": "COMPOSITION",
      "isCrossContext": false
    }
  ],
  "behaviors": [
    {
      "code": "release_production_order",
      "name": "生产订单下达",
      "aggregateRoot": "ProductionOrder",
      "invocationMode": "BOTH",
      "inputParams": [
        {"name":"orderId","type":"STRING","required":true}
      ],
      "validationRules": [
        {
          "code": "material_availability_check",
          "name": "物料齐套校验",
          "type": "PRE_CHECK",
          "expression": {...},
          "violationMessageTemplate": "物料 {{materialCode}} 库存不足"
        }
      ],
      "publishedEvents": ["production_order.released"],
      "transactionBoundary": {
        "operations": [
          {"object":"ProductionOrder","field":"status","newValue":"RELEASED"},
          {"object":"Material","field":"reservedQty","delta":"-{{qty}}"}
        ]
      },
      "sideEffects": [
        {"type":"NOTIFICATION","target":"workshop_supervisor","async":true}
      ]
    }
  ],
  "events": [
    {
      "code": "production_order.released",
      "name": "生产订单已下达",
      "kind": "DOMAIN",
      "payloadSchema": [
        {"name":"eventId","type":"STRING"},
        {"name":"orderId","type":"STRING"},
        {"name":"triggeredBy","type":"STRING"}
      ],
      "routes": [
        {"targetType":"CONTEXT","targetContext":"material_mgmt"},
        {"targetType":"EXTERNAL_SYSTEM","targetSystem":"MES"}
      ],
      "handlers": [
        {
          "scenario": "MTS",
          "requiredState": "RELEASED",
          "handlerBehavior": "auto_replenishment",
          "priority": 10
        },
        {
          "scenario": "MTO",
          "requiredState": "RELEASED",
          "handlerBehavior": "per_order_picking",
          "priority": 10
        }
      ]
    }
  ],
  "permissions": {
    "roles": [
      {
        "code": "production_planner",
        "name": "生产计划员",
        "objectPermissions": [
          {"objectType":"ProductionOrder","read":true,"write":true,"execute":true}
        ],
        "fieldPermissions": [
          {"objectType":"ProductionOrder","field":"costPrice","visible":false}
        ]
      }
    ]
  },
  "sandbox": {
    "allowedTools": ["resolve_intent","query_ontology","execute_action"],
    "allowedAggregateRoots": ["ProductionOrder","Material","BOM","Routing"],
    "allowedBehaviors": [
      {"code":"release_production_order","maxCallsPerMinute":10}
    ],
    "defaultDenyAll": true
  },
  "dataSources": [
    {
      "objectType": "ProductionOrder",
      "methods": [
        {
          "type": "API_CALL",
          "sourceName": "SAP S/4HANA",
          "endpoint": "GET /A_SalesOrder('{orderId}')",
          "authType": "OAUTH2",
          "credentialRef": "vault:sap-s4-prod"
        }
      ]
    }
  ]
}
```

### 3.3 版本策略

```
版本号规则：MAJOR.MINOR.PATCH

MAJOR++ : 删除聚合根/行为/事件/属性（破坏性变更）
MINOR++ : 新增聚合根/行为/事件/属性（向后兼容）
PATCH++ : 修改描述/校验规则表达式（逻辑变更但 Schema 不变）

自动检测：编译器对比上一版本 Manifest JSON，diff 判定变更类型
```

---

## 4. MCP Server 设计

### 4.1 架构

```
┌─────────────────────────────────────────────────────────────────┐
│                MCP Server 内部架构                                │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │                  MCP Transport Layer                       │   │
│  │  • JSON-RPC 2.0 over SSE (HTTP)                           │   │
│  │  • Session management (session_id -> Manifest binding)    │   │
│  │  • Heartbeat / keepalive                                   │   │
│  └────────────────────────┬─────────────────────────────────┘   │
│                           │                                       │
│  ┌────────────────────────▼─────────────────────────────────┐   │
│  │                  Tool Registry                             │   │
│  │  ┌─────────────────────┐  ┌──────────────────────────┐   │   │
│  │  │ Static Tools (3)    │  │ Dynamic Tools (from       │   │   │
│  │  │ • query_ontology    │  │ Manifest)                 │   │   │
│  │  │ • resolve_intent    │  │ • execute_action("...")   │   │   │
│  │  │ • validate_instruction│ │ • get_object_detail      │   │   │
│  │  └─────────────────────┘  └──────────────────────────┘   │   │
│  └────────────────────────┬─────────────────────────────────┘   │
│                           │                                       │
│  ┌────────────────────────▼─────────────────────────────────┐   │
│  │                  Manifest Loader                           │   │
│  │  • 启动时加载最新 Manifest（或指定版本）                    │   │
│  │  • 监听 ManifestPublished 事件 → 热加载新版本              │   │
│  │  • 版本锁定：Agent 可绑定特定 Manifest 版本                │   │
│  └────────────────────────┬─────────────────────────────────┘   │
│                           │                                       │
│  ┌────────────────────────▼─────────────────────────────────┐   │
│  │                  Defense Layer（三通道防御）                │   │
│  │  Channel 1: Tool Whitelist Enforcement                    │   │
│  │  Channel 2: Ontology Context Injection                    │   │
│  │  Channel 3: Output Validation (Schema + Rules + Perms)    │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                   │
└─────────────────────────────────────────────────────────────────┘
```

### 4.2 MCP 工具注册流程

```java
// 伪代码：MCP Server 启动时的工具注册
@Component
public class McpToolRegistry {

    private final ManifestLoader manifestLoader;

    @PostConstruct
    public void registerTools() {
        Manifest manifest = manifestLoader.loadLatest("manufacturing");

        // 1. 注册平台级固定工具（3 个）
        registerStaticTool("query_ontology", new QueryOntologyTool());
        registerStaticTool("resolve_intent", new ResolveIntentTool());
        registerStaticTool("validate_instruction", new ValidateInstructionTool());

        // 2. 从 Manifest 动态生成本体级工具
        for (Behavior behavior : manifest.getBehaviors()) {
            if (behavior.getInvocationMode() != "DIRECT") {
                McpTool tool = McpTool.builder()
                    .name("execute_action")
                    .description("执行行为：" + behavior.getName())
                    .inputSchema(buildJsonSchema(behavior.getInputParams()))
                    .handler(new ExecuteActionHandler(behavior, manifest))
                    .build();
                registerDynamicTool("execute_action", tool);
            }
        }

        // 3. 按聚合根注册查询工具
        for (Entity entity : manifest.getEntities()) {
            if (entity.isAggregateRoot()) {
                McpTool tool = McpTool.builder()
                    .name("get_object_detail")
                    .description("查询" + entity.getName() + "详情")
                    .inputSchema(buildQuerySchema(entity))
                    .handler(new GetObjectDetailHandler(entity, manifest))
                    .build();
                registerDynamicTool("get_object_detail", tool);
            }
        }
    }
}
```

### 4.3 MCP 会话 → Manifest 绑定

```
Client (Dify/Coze)                    MCP Server
       │                                  │
       │── initialize ──────────────────→│
       │   {ontologyContext:"manufacturing"}│
       │                                  │── 加载 manufacturing Manifest v1.2.0
       │←─ tools/list ───────────────────│
       │   [query_ontology,               │
       │    resolve_intent,               │
       │    execute_action,  ← 6 behaviors│
       │    get_object_detail] ← 4 aggRoots│
       │                                  │
       │── tools/call ──────────────────→│
       │   {name:"execute_action",        │
       │    args:{action:"release_        │
       │    production_order",            │
       │    orderId:"PO-001"}}            │
       │                                  │── 通道一：白名单校验 ✓
       │                                  │── 通道三：Schema + Rule + Permission ✓
       │                                  │── 执行 + 返回结果
       │←─ {success:true, data:{...}} ───│
```

---

## 5. 三通道防御实现方案

### 5.1 整体调用链

> **核心设计决策**：`resolve_intent` 作为独立 MCP 工具暴露给 Agent，而非隐藏在 `query_ontology` 内部。原因是 LLM 需要看到中间映射结果来做 sanity check——LLM 的强项是验证和纠错，不是凭空猜测本体概念空间。两步暴露让 LLM 能在 Step 2 纠正 Step 1 的映射错误。

```
Agent (LLM)         MCP Server          本体平台 Core         校验引擎
    │                   │                    │                   │
    │①用户:"把SO-123    │                    │                   │
    │  改成已发货"       │                    │                   │
    │──resolve_intent─→│                    │                   │
    │  query:"改订单为   │                    │                   │
    │  已发货"          │②语义路由：模糊→精确  │                   │
    │                   │  搜索本体概念空间    │                   │
    │                   │──────────────────→│                   │
    │                   │←── 映射结果 ──────│                   │
    │←── {context:      │                    │                   │
    │     "manufacturing"│                   │                   │
    │     aggregate:     │                    │                   │
    │     "SalesOrder",  │                    │                   │
    │     behavior:      │                    │                   │
    │     "changeStatus",│                    │                   │
    │     targetState:   │                    │                   │
    │     "DELIVERED"}   │                    │                   │
    │                   │                    │                   │
    │③LLM sanity check: │                    │                   │
    │  "SalesOrder 没错, │                    │                   │
    │   DELIVERED 合法"  │                    │                   │
    │──query_ontology─→│                    │                   │
    │  context:         │                    │                   │
    │  "manufacturing", │                    │                   │
    │  concept:         │④通道二：查询概念定义 │                   │
    │  "SalesOrder"     │──────────────────→│                   │
    │                   │←── 概念片段 ──────│                   │
    │←── 属性+状态机    │                    │                   │
    │    +校验规则摘要   │                    │                   │
    │                   │                    │                   │
    │⑤LLM生成Tool调用   │                    │                   │
    │──execute_action─→│                    │                   │
    │  action:          │⑥通道一：白名单校验  │                   │
    │  "changeStatus",  │  tool ∈ allowed?   │                   │
    │  target:"SO-123", │⑦通道三：输出校验    │                   │
    │  params:{         │──────────────────────────────────────→│
    │   state:          │                    │  ⑧Schema校验     │
    │   "DELIVERED"}    │                    │  ⑨规则校验       │
    │                   │                    │  ⑩权限校验       │
    │                   │                    │  ⑪聚合根入口校验  │
    │                   │←── 校验通过 ──────────────────────────│
    │                   │⑫执行行为+发布事件   │                   │
    │                   │──────────────────→│                   │
    │←── 执行结果 ──────│←── 结果 ──────────│                   │
```

**三步链路总结**：
| 步骤 | MCP 工具 | 做什么 | LLM 的角色 |
|------|---------|--------|-----------|
| Step 1 | `resolve_intent` | 模糊自然语言 → 精确概念引用（context + aggregate + behavior + targetState） | 验证映射结果，纠正错误映射 |
| Step 2 | `query_ontology` | 精确概念引用 → 完整语义定义（属性 + 状态机 + 校验规则 + 权限） | 将结构化定义注入后续推理 |
| Step 3 | `execute_action` | 执行行为，经三通道防御拦截 | 生成精确的 Tool 调用参数 |

### 5.2 通道一：硬约束（MCP 工具白名单）

```java
// 在 MCP Server 的 Tool Invocation 拦截器中
@Component
public class WhitelistInterceptor implements McpToolInterceptor {

    @Override
    public ValidationResult beforeInvoke(McpSession session, ToolInvocation invocation) {
        Manifest manifest = session.getManifest();

        // 1. 检查工具是否在白名单中
        if (!manifest.getSandbox().getAllowedTools().contains(invocation.getToolName())) {
            auditLog.deny(session, invocation, "TOOL_NOT_IN_WHITELIST");
            return ValidationResult.deny(403, "工具未授权: " + invocation.getToolName());
        }

        // 2. 对于 execute_action，检查行为是否在白名单中
        if ("execute_action".equals(invocation.getToolName())) {
            String actionCode = invocation.getArgs().get("action").asText();
            boolean allowed = manifest.getSandbox().getAllowedBehaviors().stream()
                .anyMatch(b -> b.getCode().equals(actionCode));
            if (!allowed) {
                return ValidationResult.deny(403, "行为未授权: " + actionCode);
            }

            // 3. 频率限制
            RateLimitResult rl = rateLimiter.check(session.getId(), actionCode,
                manifest.getSandbox().getMaxOpsPerSecond());
            if (!rl.allowed()) {
                return ValidationResult.deny(429, "频率超限，请稍后重试");
            }
        }

        return ValidationResult.allow();
    }
}
```

### 5.3 通道二：软约束（意图解析 + 本体查询注入）

通道二由**两个 MCP 工具协同完成**，缺一不可：

**Step 1 — `resolve_intent`：模糊自然语言 → 精确概念引用**

```java
@Component
public class ResolveIntentTool implements McpTool {

    @Override
    public ToolResult execute(Map<String, Object> args, McpSession session) {
        String query = (String) args.get("query");
        Manifest manifest = session.getManifest();

        // 在本体的概念空间中搜索最佳匹配
        // 匹配维度：限界上下文名、聚合根名、对象名、行为名、状态名、属性标签
        IntentResolution resolution = intentResolver.resolve(query, manifest);

        // 返回精确的概念引用（不是自然语言，是结构化的本体坐标）
        return ToolResult.success(IntentResolution.builder()
            .boundedContext(resolution.getContextCode())     // "manufacturing"
            .aggregate(resolution.getAggregateCode())        // "SalesOrder"
            .behavior(resolution.getBehaviorCode())          // "changeStatus"
            .targetState(resolution.getTargetStateCode())   // "DELIVERED"
            .confidence(resolution.getConfidence())          // 0.92
            .alternatives(resolution.getAlternatives())      // 备选映射（置信度较低）
            .build());
    }
}
```

> **为什么 `resolve_intent` 是独立的 MCP 工具而非内部能力？**
> LLM 看到 `resolve_intent` 返回的结构化映射结果后，能做 sanity check——"SalesOrder 是对的，DELIVERED 是合法状态"。如果映射错了，LLM 能在此纠正（比如从 `alternatives` 中选择正确的）。如果把它藏在 `query_ontology` 内部，LLM 看不到中间结果，纠错能力就浪费了。

**Step 2 — `query_ontology`：精确概念引用 → 完整语义定义**

```java
// Agent 拿到 resolve_intent 的精确结果后，调用 query_ontology 获取完整定义
// 注意：此时参数是精确的（context + concept），不是模糊的自然语言

@Component
public class QueryOntologyTool implements McpTool {

    @Override
    public ToolResult execute(Map<String, Object> args, McpSession session) {
        String context = (String) args.get("context");    // 来自 resolve_intent
        String concept = (String) args.get("concept");     // 来自 resolve_intent
        Manifest manifest = session.getManifest();

        ConceptFragment fragment = ConceptFragment.builder()
            .entity(findEntity(manifest, concept))
            .allowedBehaviors(findBehaviorsForEntity(manifest, concept))
            .stateMachine(findStateMachine(manifest, concept))
            .relatedConcepts(findRelations(manifest, concept))
            .validationRulesSummary(findRulesForEntity(manifest, concept))
            .build();

        return ToolResult.success(fragment.toContextString());
    }
}
```

### 5.4 通道三：校验层（四重校验）

```java
@Service
public class ValidationEngine {

    // 校验入口——在行为执行前被 MCP Server 调用
    public ValidationResult validate(Manifest manifest,
                                      String behaviorCode,
                                      Map<String, Object> inputParams,
                                      String roleCode) {

        Behavior behavior = manifest.findBehavior(behaviorCode);
        if (behavior == null) {
            return fail("BEHAVIOR_NOT_FOUND", "行为不存在: " + behaviorCode);
        }

        // ───── 校验 1：Schema 白名单 ─────
        ValidationResult schemaCheck = validateSchema(manifest, behavior, inputParams);
        if (!schemaCheck.passed()) return schemaCheck;

        // ───── 校验 2：行为前置条件（状态机 + 校验规则）─────
        ValidationResult preconditionCheck = validatePreconditions(manifest, behavior, inputParams);
        if (!preconditionCheck.passed()) return preconditionCheck;

        // ───── 校验 3：权限矩阵 ─────
        ValidationResult permissionCheck = validatePermissions(manifest, behavior, roleCode);
        if (!permissionCheck.passed()) return permissionCheck;

        // ───── 校验 4：聚合根入口校验 ─────
        ValidationResult aggregateCheck = validateAggregateRootEntry(manifest, behavior);
        if (!aggregateCheck.passed()) return aggregateCheck;

        return ValidationResult.pass();
    }

    // 校验 1 详细：Schema 白名单
    private ValidationResult validateSchema(Manifest m, Behavior b, Map<String, Object> params) {
        // 检查输入参数的类型是否匹配 Manifest 中定义的 Schema
        for (BehaviorParam expected : b.getInputParams()) {
            Object actual = params.get(expected.getName());
            if (expected.isRequired() && actual == null) {
                return fail("MISSING_REQUIRED_PARAM",
                    "缺少必填参数: " + expected.getName());
            }
            if (actual != null && !typeMatches(expected.getType(), actual)) {
                return fail("TYPE_MISMATCH",
                    "参数类型错误: " + expected.getName() + " 期望 " + expected.getType());
            }
        }
        // 检查是否有未定义的参数（防止 Agent 幻觉出额外字段）
        for (String key : params.keySet()) {
            if (b.getInputParams().stream().noneMatch(p -> p.getName().equals(key))) {
                return fail("UNKNOWN_PARAM",
                    "未定义的参数: " + key + "（该参数不在本体定义中，可能是 AI 幻觉）");
            }
        }
        return pass();
    }

    // 校验 2 详细：前置条件
    private ValidationResult validatePreconditions(Manifest m, Behavior b,
                                                    Map<String, Object> params) {
        List<FailedItem> failures = new ArrayList<>();

        for (ValidationRule rule : b.getValidationRules()) {
            RuleResult result = ruleEngine.evaluate(rule, params);
            if (!result.passed()) {
                failures.add(new FailedItem(
                    rule.getCode(),
                    result.getFailedItems(),
                    interpolate(rule.getViolationMessageTemplate(), result.getContext())
                ));
            }
        }

        if (!failures.isEmpty()) {
            return fail("RULE_VIOLATION", failures);
        }
        return pass();
    }
}
```

---

## 6. 事件引擎设计

### 6.1 渐进式架构

```
MVP (进程内)                    生产环境 (消息队列)
─────────────────────────────────────────────────

Spring ApplicationEvents    →   Kafka / RabbitMQ
同步分发（同一事务）          异步分发（最终一致性）
无持久化                      事件存储（PostgreSQL Event Store）
无重试                         死信队列 + 重试策略

升级路径：
1. MVP: @EventListener 处理领域事件
2. V1.5: 引入 PostgreSQL Event Store（事件表 + LISTEN/NOTIFY）
3. V2.0: 切换到 Kafka（通过配置切换 EventBus 实现）
```

### 6.2 MVP 事件流实现

```java
// 领域事件基类
public abstract class DomainEvent {
    private final String eventId = UUID.randomUUID().toString();
    private final String eventCode;
    private final Instant occurredAt = Instant.now();
    private final String aggregateRootId;
    private final String triggeredBy;
}

// 行为执行服务（聚合根内）
@Service
@Transactional
public class BehaviorExecutionService {

    private final ApplicationEventPublisher eventPublisher;

    public ExecutionResult execute(String behaviorCode, Map<String, Object> params) {
        // 1. 执行行为核心逻辑（事务边界内）
        Behavior behavior = loadBehavior(behaviorCode);
        TransactionResult txResult = executeInTransaction(behavior, params);

        // 2. 发布领域事件（同一事务内，Spring Events 同步）
        for (String eventCode : behavior.getPublishedEvents()) {
            DomainEvent event = buildEvent(eventCode, txResult);
            eventPublisher.publishEvent(event);
        }
        // @Transactional 确保：行为执行 + 事件发布 = 原子操作

        // 3. 处理副作用（异步，事务提交后）
        for (SideEffect effect : behavior.getSideEffects()) {
            if (effect.isAsync()) {
                sideEffectExecutor.submitAsync(effect, txResult);
            }
        }

        return ExecutionResult.success(txResult);
    }
}

// 事件处理器（进程内监听）
@Component
public class EventHandlerDispatcher {

    @EventListener
    @Async  // 异步处理，不阻塞主事务
    public void onDomainEvent(DomainEvent event) {
        // 从 Manifest 中查找匹配的事件处理器
        List<EventHandler> handlers = eventHandlerMatcher.match(
            event.getEventCode(),
            currentScenario(),       // 从上下文获取当前业务场景
            currentObjectState()     // 从事件 Payload 中获取对象状态
        );

        for (EventHandler handler : handlers) {
            // 处理器引用的行为 = 事件驱动模式
            behaviorExecutionService.execute(
                handler.getHandlerBehaviorCode(),
                buildHandlerParams(event)
            );
        }
    }
}
```

### 6.3 事件处理器匹配算法

```java
@Component
public class EventHandlerMatcher {

    /**
     * 匹配优先级：
     * 1. 精确匹配：(场景, 状态) 完全符合 → 最高优先级
     * 2. 场景匹配：(场景符合, 状态=任意) → 中优先级
     * 3. 状态匹配：(场景=任意, 状态符合) → 低优先级
     * 4. 无匹配 → 忽略该事件
     */
    public List<EventHandler> match(String eventCode, String scenarioCode, String stateCode) {
        Manifest manifest = getCurrentManifest();

        return manifest.getEventHandlers(eventCode).stream()
            .filter(h -> matches(h, scenarioCode, stateCode))
            .sorted(Comparator.comparingInt(this::matchScore).reversed())
            .collect(Collectors.toList());
    }

    private int matchScore(EventHandler h, String scenario, String state) {
        if (Objects.equals(h.getScenario(), scenario) &&
            Objects.equals(h.getRequiredState(), state)) return 100;  // 精确匹配
        if (Objects.equals(h.getScenario(), scenario) &&
            h.getRequiredState() == null) return 50;                   // 场景匹配
        if (h.getScenario() == null &&
            Objects.equals(h.getRequiredState(), state)) return 25;    // 状态匹配
        return 0;                                                       // 不匹配
    }
}
```

---

## 7. REST API 设计

### 7.1 API 设计原则

- **资源导向**：URL 表达资源层级 `/contexts/{id}/aggregate-roots/{id}/behaviors`
- **统一响应格式**：`{ "code": 0, "message": "success", "data": {...} }`
- **分页标准**：`?page=0&size=20&sort=createdAt,desc`
- **版本控制**：URL 前缀 `/api/v1/`

### 7.2 核心 API 端点

```
语义层:
POST   /api/v1/contexts                         创建限界上下文 (S01)
GET    /api/v1/contexts/{id}                    查询上下文详情
POST   /api/v1/contexts/{id}/scenarios          创建业务场景 (S02)
POST   /api/v1/contexts/{id}/aggregate-roots    创建聚合根 (S03)
POST   /api/v1/contexts/{id}/object-types       创建对象类型 (S04)
POST   /api/v1/contexts/{id}/relationships      创建关系 (S07)
POST   /api/v1/contexts/{id}/data-sources       配置数据源 (S08)
POST   /api/v1/contexts/{id}/state-machines     配置状态机 (S06)
GET    /api/v1/value-objects                    查询值对象 (S05)
POST   /api/v1/cross-context-relationships      创建跨上下文关系 (S09)
GET    /api/v1/contexts/{id}/lineage            查询数据血缘 (S10)

行为层:
POST   /api/v1/contexts/{id}/behaviors          定义行为 (B01)
POST   /api/v1/behaviors/{id}/params            配置行为参数
GET    /api/v1/param-templates                  查询参数模板 (B02)
POST   /api/v1/contexts/{id}/validation-rules   创建校验规则 (B03)
POST   /api/v1/behaviors/{id}/rules             关联规则到行为
POST   /api/v1/contexts/{id}/metrics            创建指标 (B05)
POST   /api/v1/behaviors/{id}/transaction       配置事务边界 (B06)
POST   /api/v1/behaviors/{id}/side-effects      配置副作用 (B07)

事件层:
POST   /api/v1/contexts/{id}/events             定义领域事件 (E01)
POST   /api/v1/events/{id}/routes               配置事件路由 (E03)
POST   /api/v1/events/{id}/handlers             配置事件处理器 (E04)
POST   /api/v1/events/{id}/integration          创建集成事件 (E05)
PUT    /api/v1/contexts/{id}/event-store         配置事件存储 (E02)

治理层:
POST   /api/v1/roles                            创建角色 (G01)
POST   /api/v1/roles/{id}/permissions           配置对象级权限
POST   /api/v1/roles/{id}/field-permissions     配置字段级权限 (G02)
POST   /api/v1/roles/{id}/conditional-rules     配置条件权限 (G03)
POST   /api/v1/sandboxes                        配置 AI 沙箱 (G04)
POST   /api/v1/contexts/{id}/submit-review      提交审核 (G05)
POST   /api/v1/contexts/{id}/approve            批准发布 (G05)
POST   /api/v1/contexts/{id}/review-comments    提交审核批注 (G05)

制品层:
POST   /api/v1/contexts/{id}/compile            编译 Manifest (A01)
GET    /api/v1/manifests/{id}/diff?base=v1.0    版本对比 (A02)

MCP 专用（MCP Server 内部调用，不对外暴露）:
POST   /internal/mcp/query-ontology             查询本体概念 (A03)
POST   /internal/mcp/validate-instruction       校验 Agent 指令 (A05)
POST   /internal/mcp/execute-action             执行行为 (A05)
```

### 7.3 统一响应格式

```json
// 成功
{
  "code": 0,
  "message": "success",
  "data": { ... },
  "timestamp": "2026-06-04T10:30:00Z"
}

// 校验失败（通道三）
{
  "code": 40001,
  "message": "RULE_VIOLATION",
  "data": {
    "rule": "material_availability_check",
    "failedItems": [
      {"materialCode": "MAT-001", "requiredQty": 200, "stockQty": 150, "gap": 50}
    ],
    "userMessage": "物料 MAT-001 库存不足，需求 200 件，当前库存 150 件，缺口 50 件"
  },
  "timestamp": "2026-06-04T10:30:00Z"
}

// 编译错误（Manifest 编译器）
{
  "code": 50001,
  "message": "COMPILE_ERROR",
  "data": {
    "errors": [
      {"elementType": "BEHAVIOR", "elementName": "生产订单下达", "field": "aggregateRootId", "message": "关联聚合根不存在"}
    ]
  }
}
```

---

## 8. 技术选型终稿

| 类别 | 选型 | 版本 | 说明 |
|------|------|------|------|
| **语言** | Java | 17 LTS | 企业级生态成熟，团队熟悉 |
| **框架** | Spring Boot | 3.2.x | DDD 五模块结构天然适配 |
| **ORM** | MyBatis-Plus | 3.5.x | JSONB 查询灵活，比 JPA 更适合半结构化数据 |
| **数据库** | PostgreSQL | 15+ | JSONB 存储动态属性/规则/事件 Payload；支持 GIN 索引 |
| **图查询** | PostgreSQL Recursive CTE | — | 关系遍历用 CTE 而非 Neo4j（降低运维复杂度） |
| **缓存** | Redis | 7.x | Manifest 缓存、权限缓存、会话状态 |
| **搜索** | PostgreSQL Full-Text Search | — | MVP 够用；V2.0 可加 Elasticsearch |
| **事件（MVP）** | Spring ApplicationEvents | — | 进程内同步事件，零依赖 |
| **事件（生产）** | Kafka | 3.x | 异步事件总线 + 事件溯源 |
| **对象存储** | MinIO (私有化) / S3 (云) | — | Manifest 版本文件 + 审批快照 |
| **API 文档** | SpringDoc OpenAPI | 2.5.x | 自动生成 Swagger UI |
| **数据库迁移** | Flyway | 10.x | 版本化数据库变更 |
| **容器化** | Docker + Compose | — | 本地开发 + 轻量部署 |
| **CI/CD** | GitHub Actions | — | 自动构建 + 测试 |
| **凭证管理** | HashiCorp Vault (生产) | — | 数据源凭证加密存储 |

### 8.1 技术决策记录（ADR）

| ADR | 决策 | 替代方案 | 理由 |
|-----|------|---------|------|
| ADR-01 | JSONB 存动态属性 | 单独的 attribute 表（EAV 模型） | JSONB 查询性能更好，Schema 变更无需迁移 |
| ADR-02 | PostgreSQL 代替 Neo4j | Neo4j / Apache AGE | 运维简单（单一数据库），CTE 覆盖关系遍历需求；AGE 可作为 V2.0 升级选项 |
| ADR-03 | MCP Server 独立进程 | 嵌入本体平台 | 解耦部署、独立扩缩容、协议层隔离 |
| ADR-04 | MVP 进程内事件 | 直接上 Kafka | 降低 MVP 复杂度；接口抽象（EventBus）允许后期无痛切换 |

---

## 9. 部署架构

### 9.1 MVP 部署拓扑（Docker Compose）

```
┌──────────────────────────────────────────────────────────────┐
│                    Docker Compose (单机)                       │
├──────────────────────────────────────────────────────────────┤
│                                                                │
│  ┌─────────────────┐  ┌─────────────────┐                    │
│  │ ontology-platform│  │  mcp-server     │                    │
│  │ (Spring Boot)   │  │ (Spring Boot)   │                    │
│  │ Port: 8080      │  │ Port: 8090      │                    │
│  └────────┬────────┘  └────────┬────────┘                    │
│           │                    │                               │
│  ┌────────▼────────────────────▼────────┐                    │
│  │           PostgreSQL 15              │                    │
│  │           Port: 5432                 │                    │
│  └──────────────────────────────────────┘                    │
│           │                                                    │
│  ┌────────▼────────┐  ┌─────────────────┐                    │
│  │    Redis 7      │  │   MinIO         │                    │
│  │    Port: 6379   │  │   Port: 9000    │                    │
│  └─────────────────┘  └─────────────────┘                    │
│                                                                │
└──────────────────────────────────────────────────────────────┘
```

### 9.2 生产部署拓扑（Kubernetes）

```
┌──────────────────────────────────────────────────────────────┐
│                     Kubernetes Cluster                         │
├──────────────────────────────────────────────────────────────┤
│                                                                │
│  ┌──────────────────────────────┐                             │
│  │  ontology-platform (3 pods) │  HPA: CPU > 70%             │
│  │  Port: 8080                  │                             │
│  └──────────────────────────────┘                             │
│                                                                │
│  ┌──────────────────────────────┐                             │
│  │  mcp-server (3 pods)         │  HPA: CPU > 60%             │
│  │  Port: 8090                  │                             │
│  └──────────────────────────────┘                             │
│                                                                │
│  ┌──────────────────────────────────────────────┐            │
│  │  PostgreSQL 15 (HA: Patroni + etcd)           │            │
│  │  主库 + 2 只读副本                             │            │
│  └──────────────────────────────────────────────┘            │
│                                                                │
│  ┌────────────┐ ┌────────────┐ ┌──────────────┐             │
│  │ Redis      │ │ MinIO/S3   │ │ Kafka        │             │
│  │ Cluster    │ │ (Manifest) │ │ (Event Bus)  │             │
│  └────────────┘ └────────────┘ └──────────────┘             │
│                                                                │
│  ┌──────────────────────────────────────────────┐            │
│  │  Vault (凭证加密)  |  Prometheus + Grafana   │            │
│  └──────────────────────────────────────────────┘            │
│                                                                │
└──────────────────────────────────────────────────────────────┘
```

---

## 10. 性能与可扩展性

### 10.1 性能目标

| 操作 | P50 | P95 | P99 |
|------|-----|-----|-----|
| 查询本体概念（query_ontology） | <50ms | <100ms | <200ms |
| Manifest 编译（4 聚合根 + 6 行为） | <500ms | <1s | <2s |
| 校验引擎（4 条规则） | <10ms | <20ms | <50ms |
| 行为执行（不含下游调用） | <50ms | <100ms | <200ms |
| 权限检查（缓存命中） | <5ms | <10ms | <15ms |

### 10.2 缓存策略

| 缓存对象 | 存储 | TTL | 失效策略 |
|---------|------|-----|---------|
| Manifest JSON | Redis | 永久（版本不可变） | 新版本发布时写入 |
| 权限矩阵 | Redis | 5 分钟 | 权限变更时主动失效 |
| 校验规则表达式 | 本地 Caffeine | 10 分钟 | 规则版本更新时失效 |
| 数据源连接元数据 | Redis | 30 分钟 | 数据源配置变更时失效 |

### 10.3 扩展点

| 扩展点 | 当前实现 | 可替换为 |
|--------|---------|---------|
| EventBus 接口 | Spring Events | Kafka / RabbitMQ / Pulsar |
| 图遍历引擎 | PostgreSQL CTE | Apache AGE / Neo4j |
| 规则引擎 | 自研 JSONB 表达式 | Drools / Easy Rules |
| 凭证管理 | 环境变量 | HashiCorp Vault / AWS Secrets Manager |
| 搜索 | PostgreSQL FTS | Elasticsearch |
| 通知（副作用） | 同步调用 | AWS SNS / 钉钉 / 飞书 / 企业微信 |

---

> **文档状态**：v2.0 | 基于 PRD v2.0 + USM v1.2（32 Stories）
>
> **下一步**：
> 1. API 契约文档（OpenAPI 3.0 Spec）
> 2. Manifest JSON Schema 正式定义（JSON Schema 标准）
> 3. 数据库迁移脚本（Flyway V1__init.sql）
> 4. Sprint 拆分
