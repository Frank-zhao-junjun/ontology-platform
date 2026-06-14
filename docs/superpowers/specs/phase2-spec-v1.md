# ontology-platform Phase 2 Spec v1.0

> 基于 US v1.1 路线图 P1/P2 | 2026-06-14 | Draft
> 前置: Phase 1 v1.2 全部完成 (17 MyBatis-Plus Repos)

---

## 1. 范围

| 板块 | US | 新增组件 |
|------|-----|---------|
| 异步执行 | F01 | JobQueue, JobPoller, WebhookDispatcher |
| 幂等重试 | F02 | IdempotencyFilter, IdempotencyRecord |
| 可观测性 | F03 | Micrometer + Prometheus metrics, TraceId |
| 版本兼容 | F04 | ToolVersionRegistry, DeprecationNotice |
| 限流 | F05 | RateLimiter (Agent/Tool/Tenant) |
| 传输安全 | -- | mTLS (MCP to Platform) |

### 拓扑 (Phase 2)

```
Agent (LLM) --JWT+mTLS-- MCP Server (:3001) --mTLS+APIKey-- Spring Boot (:8080)
    |                         |                           |
    |                   RateLimiter              IdempotencyFilter
    |                   (per Agent)                   |
    |                                         JobQueue (Redis List)
    |                                              |
    +---- Webhook <---- WebhookDispatcher <---------+
                                                |
                                        PG + AGE + Redis
```

---

## 2. 架构决策

| 决策 | 选择 | 理由 |
|------|------|------|
| 任务队列 | Redis List (BRPOPLPUSH) | 已有 Redis 依赖，无额外 MQ |
| 幂等存储 | PG + Redis 缓存 | 持久化 + 热 key 加速 |
| 指标采集 | Micrometer + Prometheus | Spring Boot 3.x 原生集成 |
| 全链路追踪 | SLF4J MDC trace_id | Phase 1 已有 trace_id 字段 |
| 限流算法 | Token Bucket (Redis Lua) | 分布式精准，支持突发 |
| Webhook | HTTP POST + 重试 3 次 | 无外部 MQ 依赖 |
| mTLS | 自签 CA 证书链 | 内网环境，无需公共 CA |
| 工具版本 | {toolName}_v{major} 命名 | 主版本号隔离，旧版保留 30 天 |

---

## 3. 数据模型 (4 张新表, V8 Flyway)

| 表 | US | 用途 |
|----|-----|------|
| idempotency_record | F02 | 幂等请求记录，24h TTL |
| job_record | F01 | 异步任务状态追踪 |
| webhook_subscription | F01 | Webhook 回调注册 |
| rate_limit_config | F05 | 限流规则配置 |

### 3.1 DDL

```sql
-- V8__create_phase2_tables.sql

CREATE TABLE idempotency_record (
    idempotency_key VARCHAR(128) PRIMARY KEY,
    tenant_id VARCHAR(100) NOT NULL,
    agent_id VARCHAR(200),
    http_method VARCHAR(10) NOT NULL,
    request_path VARCHAR(500) NOT NULL,
    response_status INTEGER,
    response_body JSONB,
    created_at TIMESTAMPTZ DEFAULT now(),
    expires_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_idempotency_expires ON idempotency_record(expires_at);

CREATE TABLE job_record (
    id UUID PRIMARY KEY,
    job_type VARCHAR(100) NOT NULL,
    tenant_id VARCHAR(100) NOT NULL,
    agent_id VARCHAR(200),
    idempotency_key VARCHAR(128) REFERENCES idempotency_record(idempotency_key),
    status VARCHAR(20) DEFAULT 'QUEUED',
    payload JSONB NOT NULL,
    result JSONB,
    error_message TEXT,
    retry_count INTEGER DEFAULT 0,
    max_retries INTEGER DEFAULT 3,
    next_retry_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT now(),
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    CONSTRAINT ck_job_status CHECK (status IN ('QUEUED','RUNNING','COMPLETED','FAILED','CANCELLED'))
);
CREATE INDEX idx_job_status ON job_record(status, created_at);
CREATE INDEX idx_job_tenant ON job_record(tenant_id, created_at);

CREATE TABLE webhook_subscription (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(100) NOT NULL,
    agent_id VARCHAR(200),
    callback_url VARCHAR(1000) NOT NULL,
    event_types JSONB NOT NULL DEFAULT '["job.completed","job.failed"]',
    secret VARCHAR(256) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE rate_limit_config (
    id UUID PRIMARY KEY,
    scope_type VARCHAR(20) NOT NULL,
    scope_value VARCHAR(200) NOT NULL,
    window_seconds INTEGER DEFAULT 60,
    max_requests INTEGER DEFAULT 100,
    burst_size INTEGER DEFAULT 20,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),
    CONSTRAINT uq_rate_limit UNIQUE (scope_type, scope_value)
);
```

---

## 4. REST API 契约

### 4.1 异步任务 (F01)

| 方法 | 端点 | 说明 |
|------|------|------|
| POST | /api/v1/jobs | 提交异步任务 |
| GET | /api/v1/jobs/{id} | 查询任务状态 |
| GET | /api/v1/jobs?status=RUNNING | 按状态列表 |
| DELETE | /api/v1/jobs/{id} | 取消任务 |

**POST /api/v1/jobs 请求体**:
```json
{
  "jobType": "import.execute",
  "payload": {
    "uploadId": "...",
    "ontologyId": "...",
    "objectTypeName": "Equipment"
  },
  "idempotencyKey": "ik_20260614_001"
}
```

**POST /api/v1/jobs 响应**:
```json
{
  "code": 0,
  "data": {
    "jobId": "uuid",
    "jobType": "import.execute",
    "status": "QUEUED",
    "createdAt": "2026-06-14T10:00:00Z"
  }
}
```

**GET /api/v1/jobs/{id} 响应**:
```json
{
  "code": 0,
  "data": {
    "jobId": "uuid",
    "jobType": "import.execute",
    "status": "COMPLETED",
    "progress": { "processedRows": 5000, "totalRows": 5000, "percent": 100 },
    "result": { "successRows": 4950, "failedRows": 50 },
    "retryCount": 0,
    "createdAt": "...",
    "startedAt": "...",
    "completedAt": "..."
  }
}
```

### 4.2 Webhook (F01)

| 方法 | 端点 | 说明 |
|------|------|------|
| POST | /api/v1/webhooks | 注册 Webhook |
| GET | /api/v1/webhooks | 列出订阅 |
| DELETE | /api/v1/webhooks/{id} | 删除订阅 |

**Webhook 回调 Payload** (POST 到 callback_url):
```json
{
  "event": "job.completed",
  "jobId": "uuid",
  "jobType": "import.execute",
  "tenantId": "default",
  "status": "COMPLETED",
  "result": { "successRows": 4950 },
  "timestamp": "2026-06-14T10:01:00Z",
  "retryCount": 0
}
```

**签名**: HTTP Header `X-Webhook-Signature: HMAC-SHA256(secret, body)`

### 4.3 幂等性 (F02)

所有写操作自动支持幂等（可选 Header）：

```
// 首次请求
POST /api/v1/ontologies/{id}/relations
Idempotency-Key: ik_20260614_001
→ 201 Created (执行业务逻辑, 缓存响应)

// 重复请求
POST /api/v1/ontologies/{id}/relations
Idempotency-Key: ik_20260614_001
→ 200 OK (返回缓存响应, 不重复执行)
```

| 场景 | HTTP | 说明 |
|------|------|------|
| 首次执行成功 | 201 | 正常业务响应 |
| 重复请求 (结果在) | 200 | 返回缓存响应 |
| 首次执行中 | 409 | Conflict, 请稍后重试 |
| 无 Idempotency-Key | 正常 | 不启用幂等, 每次都执行 |

**有效期**: 24 小时后过期可复用

### 4.4 限流 (F05)

| 级别 | 窗口 | 默认限制 | 响应 Header |
|------|------|---------|------------|
| Agent | 60s | 100 req | X-RateLimit-Remaining |
| Tool | 60s | 300 req | X-RateLimit-Reset |
| Tenant | 60s | 1000 req | Retry-After (429 时) |

**429 响应**:
```json
{
  "code": 429,
  "message": "Rate limit exceeded. Retry after 12 seconds.",
  "meta": { "retryAfter": 12 }
}
```

### 4.5 可观测性 (F03)

| 端点 | 说明 |
|------|------|
| GET /actuator/prometheus | Prometheus 抓取 (已有配置) |
| GET /actuator/metrics | 指标查询 |
| GET /actuator/health | 健康检查 |

**核心指标**:

| 指标 | 类型 | 标签 |
|------|------|------|
| mcp_tools_calls_total | Counter | tool, agent_id, status |
| mcp_tools_calls_duration | Histogram | tool, agent_id |
| http_requests_total | Counter | method, path, status, tenant |
| job_execution_total | Counter | job_type, status |
| job_queue_size | Gauge | job_type |
| rate_limit_exceeded_total | Counter | scope_type, scope_value |
| idempotency_hit_total | Counter | -- |

**TraceId 传播链**:
```
MCP Server (generate UUID v7)
  -> Platform Client (forward X-Trace-Id header)
    -> Spring Boot Filter (MDC.put("trace_id"))
      -> Service / Repository (SLF4J MDC)
        -> SQL (MyBatis plugin comment)
```

---

## 5. 实施顺序

| Phase | 内容 | 依赖 |
|-------|------|------|
| 2a | 幂等性 (F02) + TraceId 传播 | 无，独立组件 |
| 2b | 异步任务 (F01) + Webhook + 限流 (F05) | Redis |
| 2c | Metrics 完善 (F03) + 工具版本化 (F04) + mTLS | 2a+2b |

### Phase 2a: 幂等性 + 全链路追踪

**Task 2a-1: IdempotencyFilter**
- Spring HandlerInterceptor 拦截所有 POST/PUT/DELETE
- 提取 Idempotency-Key header (不存在则跳过)
- Redis 缓存 + PG idempotency_record 表双层存储
- 首次执行: 先写入 PENDING 标记, 执行业务, 写入响应
- 重复请求: 检查 PENDING -> 409, 检查 COMPLETED -> 200

**Task 2a-2: IdempotencyRecord PO/Mapper/Converter/Impl**
- PO: IdempotencyRecordPO, 表 idempotency_record
- Mapper: IdempotencyRecordPOMapper (BaseMapper CRUD + 过期清理)
- 定时任务: @Scheduled 每小时清理 expires_at < now() 的记录

**Task 2a-3: TraceId 全链路传播**
- MCP Server: UUID v7 生成 trace_id, 注入 response header
- Platform Client: 转发 X-Trace-Id / X-Request-Id header
- Spring Boot: OncePerRequestFilter -> MDC.put("trace_id", ...)
- MyBatis: Interceptor 插件, 注入 SQL comment

### Phase 2b: 异步任务 + Webhook + 限流

**Task 2b-1: Job Queue Service (Redis)**
- JobQueueService: enqueue(jobType, payload), dequeue(), ack(jobId)
- Redis LPUSH 入队, BRPOPLPUSH 可靠消费 (pending list)
- 失败回退: 从 pending list RPOPLPUSH 回主队列 (最多 3 次)

**Task 2b-2: Job Worker**
- 消费 Redis 队列, dispatch 到对应 JobHandler
- JobHandler 接口: execute(JobRecord) -> JobResult
- 内置 Handler: ImportJob, ExportJob, CleanupJob
- JobRecord PO/Mapper/Converter/Impl: 持久化任务状态

**Task 2b-3: Webhook Dispatcher**
- 任务状态变更 -> WebhookDispatcher.dispatch(job)
- 查 webhook_subscription 表 -> POST callback_url
- HMAC-SHA256 签名 (X-Webhook-Signature header)
- 失败重试: 3 次, 1s/5s/25s 间隔

**Task 2b-4: Rate Limiter**
- Redis Lua 脚本实现 Token Bucket
- 三级 key: ratelimit:agent:{id}, ratelimit:tool:{name}, ratelimit:tenant:{id}
- 配置从 rate_limit_config 表加载, @Scheduled 30s 刷新
- 响应: 429 + Retry-After + X-RateLimit-* headers

### Phase 2c: Metrics + 版本化 + mTLS

**Task 2c-1: Micrometer Metrics**
- 注册自定义 MeterRegistry bean
- Counter: mcp_tools_calls_total, job_execution_total, etc.
- @Timed 注解在关键 MCP tool handler 上
- Grafana Dashboard JSON (metrics/dashboard.json)

**Task 2c-2: Tool Versioning**
- Tool Registry: name 从 {domain}.{actionName} 改为 {domain}.{actionName}_v1
- 旧版工具: deprecated=true, sunsetAt=now+30d
- tools/list: 返回 version 和 deprecated 字段

**Task 2c-3: mTLS**
- 自签 CA + Server/Client 证书生成脚本 (scripts/gen-certs.sh)
- MCP Server: https.createServer({ cert, key, ca, requestCert: true })
- Spring Boot: server.ssl.* 配置 + client-auth: need
- 兼容: mTLS 可选, 未配置时回退纯 API Key 模式

---

## 6. 测试策略

| 层 | 框架 | 场景 |
|----|------|------|
| IdempotencyFilter | JUnit 5 + MockMvc | 首次/重复/进行中/过期 4 场景 |
| JobQueue | Testcontainers Redis | 入队/出队/重试/死信 |
| RateLimiter | JUnit 5 + Embedded Redis | Token Bucket 算法正确性 |
| WebhookDispatcher | JUnit 5 + WireMock | 回调发送 + 签名 + 重试 |
| Metrics | JUnit 5 | Meter 注册 + 标签验证 |
| mTLS | Integration Test | 证书握手 + 双向认证 |
| TraceId | JUnit 5 + MockMvc | Filter 注入 + MDC 传播 |

每 US 最低: 2 unit + 1 integration

---

## 7. 验证清单

### 7.1 Phase 2a 冒烟
- [ ] 同一 Idempotency-Key 两次 POST -> 第二次返回 200 + 缓存结果
- [ ] Idempotency-Key 首次正在执行 -> 第三次返回 409 Conflict
- [ ] MCP Server -> Platform 全链路 trace_id 一致
- [ ] SQL 日志可追溯到 trace_id

### 7.2 Phase 2b 冒烟
- [ ] 提交异步导入任务 -> GET /jobs/{id} 最终 COMPLETED
- [ ] Webhook 回调到达 -> HMAC 签名验证通过
- [ ] 任务失败 -> 自动重试 3 次 -> 最终 FAILED + Webhook 通知
- [ ] Agent 限流 100/min -> 第 101 次返回 429 + Retry-After

### 7.3 Phase 2c 冒烟
- [ ] /actuator/prometheus 返回自定义指标
- [ ] Grafana Dashboard 展示 MCP 调用曲线
- [ ] tools/list 返回带版本号的工具名
- [ ] 旧版工具 30 天后从 tools/list 移除
- [ ] mTLS 握手成功, 无证书连接被拒绝

### 7.4 CI 门禁
- [ ] mvn test 全部通过 (含 Phase 2 测试)
- [ ] npm test (vitest) 全部通过
- [ ] Redis Testcontainer 集成测试通过
- [ ] TypeScript 编译零错误

---

## 8. 兼容性说明

| 项 | 说明 |
|----|------|
| Phase 1 API | 全部向后兼容，Idempotency-Key header 可选 |
| Phase 1 表 | 不变，仅新增 V8 四张表 |
| Redis | 新增依赖 (已有配置, 补齐 JobQueue/限流用途) |
| 现有 @Async | ImportServiceImpl.executeImportAsync 迁移到 JobQueue, 旧方法 @Deprecated |
| MCP Server | 工具名加 _v1 后缀, 可选启用版本化 |
| mTLS | 可选启用, 未配置时回退纯 API Key 模式 |
| 17 MyBatis-Plus Repos | 不变, Phase 1 已完成全部迁移 |

---

> 本 Spec 覆盖 US v1.1 路线图全部 P1/P2 项目 (F01~F05 + mTLS)。
> Phase 2a (幂等+TraceId) 可独立启动，不阻塞 Phase 1 测试收尾工作。
> V8 DDL 可直接用作 Flyway 迁移脚本。
> Phase 2 完成后系统达到 **v3.0** (生产就绪)。
