对 MCP Server 提出以下要求：
1. 工具注册与发现（Tool Registry）
核心要求：MCP Server 必须提供标准化的工具描述能力。

每个工具必须声明：
- name: 唯一标识（如 "finance.query_budget"）
- description: 自然语言描述（Agent LLM 用来判断何时调用）
- inputSchema: JSON Schema 格式的参数定义
- outputSchema: 返回结构的定义
- domain: 所属业务域（财务/供应链/法务等）
- riskLevel: 风险等级（读/写/删除/审批）
为什么重要：当前门户有 9 大业务板块，每个 Agent 需要快速发现 "我能用哪些工具"，而不是遍历全部工具列表。按 domain 分组可以和门户的板块结构一一映射。
2. 认证与权限隔离（Auth & RBAC）
核心要求：MCP Server 必须支持 Agent 粒度的权限控制。
层级	要求
连接层	每个 Agent 使用独立的 access token 连接 MCP Server，不能共享凭证
工具层	同一个 MCP Server，财务 Agent 只能调用 finance.* 工具，不能调用 hr.*
数据层	工具返回的数据按租户 / 部门隔离，Agent A 查不到 Agent B 的数据
操作层	写操作需要审批流，高风险操作（删除 / 支付）需要二次确认机制
为什么重要：企业场景下，财务 Agent 不应该能操作供应链系统的库存数据，这是合规底线。
3. 异步与长时任务（Async Execution）
核心要求：MCP Server 必须支持异步执行模式。

企业 Agent 的典型场景：
- 生成财务报表 → 可能需要 30s-5min
- 跑供应链优化模型 → 可能需要几分钟
- 批量合同审查 → 可能需要十几分钟
MCP Server 必须提供：
1. 同步调用：适合秒级返回的查询
2. 异步提交 + 轮询/Webhook 回调：适合长时任务
3. 任务状态查询接口：pending / running / completed / failed
4. 任务取消接口
为什么重要：如果所有调用都是同步阻塞的，前端 Agent 门户会频繁超时，用户体验极差。
4. 结果结构化（Structured Output）
核心要求：工具返回的结果必须是结构化的，不能是自由文本。
json

// 好 ✅
{
  "status": "success",
  "data": {
    "budget_used": 850000,
    "budget_total": 1000000,
    "usage_rate": 0.85
  },
  "metadata": {
    "currency": "CNY",
    "period": "2026-Q1",
    "generated_at": "2026-05-09T14:00:00Z"
  }
}
// 差 ❌
{
  "result": "本季度预算使用率为85%，已使用85万，总预算100万"
}
为什么重要：Agent 门户需要将结果渲染为卡片、图表、表格等 UI 组件，自由文本无法被可靠解析和展示。
5. 幂等与重试（Idempotency）
核心要求：所有写操作的工具必须支持幂等。

要求：
- 每个写操作请求携带唯一的 idempotency_key
- 相同 key 的重复请求不会重复执行
- MCP Server 返回是否为重复执行（is_duplicate: true/false）
场景：
- 网络超时导致 Agent 重试 → 不能创建两笔采购订单
- 用户重复点击 → 不能提交两次审批
6. 可观测性（Observability）
核心要求：MCP Server 必须暴露足够的运行指标。

必须提供的监控维度：
1. 工具调用次数 / 成功率 / 失败率 / P99延迟
2. 按 Agent 维度的调用量统计
3. 按 domain 维度的调用量统计
4. 错误分类：超时 / 权限拒绝 / 参数错误 / 内部错误
5. Token/配额消耗量（如果调用第三方 API）
日志要求：
- 每次调用记录：agent_id, tool_name, params(脱敏), status, duration
- 支持按 trace_id 追踪跨 Agent 的调用链路
为什么重要：Agent 跑起来后，你需要知道哪个 Agent 调用了哪个工具、成功还是失败、耗时多少，否则线上出问题无法排查。
7. 版本与兼容（Versioning）
核心要求：工具接口必须支持版本管理。

要求：
- 工具名包含版本号：finance.query_budget.v1
- 新版本发布时，旧版本至少保留 30 天
- 变更日志（changelog）自动生成
- Agent 声明依赖的工具版本范围
场景：
- 财务系统升级，返回字段从 amount 改为 amount_cents
- 如果没有版本控制，所有 Agent 同时报错
8. 限流与成本（Rate Limiting & Cost）
核心要求：MCP Server 必须内置限流机制。

维度：
- 按 Agent 限流：每个 Agent 每分钟最多 N 次调用
- 按 Tool 限流：高成本工具（如 LLM 调用）单独限流
- 按租户限流：企业级配额管理
- 降级策略：超过限流后返回 429 + retry_after，不是直接拒绝
优先级排序
优先级	要求	原因
P0	工具注册与发现	没有 it，Agent 不知道能调什么
P0	认证与权限隔离	企业合规底线
P0	结果结构化	前端无法渲染自由文本
P1	异步执行	避免 Agent 门户超时
P1	幂等与重试	避免重复操作引发业务事故
P1	可观测性	线上运维必需
P2	版本与兼容	长期维护必需
P2	限流与成本	规模化后必需
一句话总结：MCP Server 的核心价值是 "让 Agent 安全、可靠、高效地调用企业能力"。先保证 P0（发现、权限、结构化），再补齐 P1（异步、幂等、观测），最后优化 P2（版本、限流）。