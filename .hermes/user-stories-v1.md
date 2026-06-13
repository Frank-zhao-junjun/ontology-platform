# ontology-platform User Stories v1.1

> 基于 PRD / US-A01 交接包 / Agent对MCP Server的要求 文档综合评估
> 日期: 2026-06-13
> 修订: v1.1 — 修正 P08 治理模块前提假设，补充依赖声明，新增 Manifest 导出方向

---

## 一、发现层 — Manifest 导入（打通设计台→platform）

### US-P01：一键导入 Manifest

> **前置依赖：** 无（入口故事）
> As a **平台管理员**,
> I want **通过 REST API 上传设计台导出的 OntologyManifest YAML/JSON**
> so that **五层模型的全部内容（语义/行为/事件/治理/EPC）一键落库，无需手动逐条创建**

**验收标准：**
- [ ] `POST /api/v1/manifests/import` 接受 YAML/JSON body
- [ ] 服务端解析 Manifest 全部 5 个 dimension
- [ ] 语义层：写入 ontology / object_type / property_definition / relation_definition 表
- [ ] 行为层：写入行为/状态机表（需新建）
- [ ] 事件层：写入领域事件表（需新建）
- [ ] 治理层：写入角色/权限/沙箱表
- [ ] EPC：写入 EPC 流程表（需新建）
- [ ] 数据源：写入数据源配置表
- [ ] 返回 `{ draftId, importedCounts, warnings }` 结构

### US-P02：导入校验

> **前置依赖：** US-P01（Manifest 结构已定义）
> As a **平台管理员**,
> I want **导入时自动执行 V01~V11 校验，不合法的 Manifest 拒绝导入**
> so that **损坏的 Manifest 不会污染运行时数据**

**验收标准：**
- [ ] 校验 apiVersion 受支持（`ontology.platform/v1`）
- [ ] 校验 metadata.version 为有效 semver
- [ ] 校验至少 1 个 aggregate_root
- [ ] 校验 entity/action/event 引用一致性
- [ ] 校验无明文凭证
- [ ] 校验 id 唯一性
- [ ] 校验失败返回 AC-4 结构：`{ code, elementType, id, field, message }`

### US-P03：导入预览与发布

> **前置依赖：** US-P01（draft 已落库）
> As a **平台管理员**,
> I want **导入后可预览变更 diff，确认后再 publish**
> so that **对生产环境变更保留最终控制权**

**验收标准：**
- [ ] 导入后进入 draft 状态，不直接生效
- [ ] 提供预览接口：POST /api/v1/manifests/{draftId}/preview → 返回变更摘要
- [ ] 提供发布接口：POST /api/v1/manifests/{draftId}/publish → 正式生效
- [ ] 已发布的 Manifest 可追溯历史版本

### US-P03b：Manifest 导出（platform → 运行时）

> **前置依赖：** US-P03（已有已发布的 Manifest 版本）
> As a **平台管理员**,
> I want **将已发布的 Manifest 导出为 YAML/JSON，用于部署到运行时环境**
> so that **完成 platform → runtime 的全链路闭环**

**验收标准：**
- [ ] `GET /api/v1/manifests/{id}/export` 返回完整 Manifest
- [ ] 支持 YAML 和 JSON 格式（`?format=yaml|json`）
- [ ] 导出内容与导入内容结构一致，可互相校验
- [ ] 导出文件名遵循命名规范：`{ontologyName}_{version}.{format}`

---

## 二、动力层+事件层 — 补齐四层架构

### US-P04：行为与状态机

> **前置依赖：** US-P01（行为数据通过 Manifest 导入）
> As an **AI Agent** (via MCP),
> I want **查询某个实体有哪些行为、状态机、前置条件**
> so that **按正确的业务流程操作，不触发无效状态**

**验收标准：**
- [ ] 数据库表支持存储：action 定义、状态机(含初始/终态)、transition、preRules
- [ ] REST API 返回实体的完整行为集
- [ ] 状态机校验：恰 1 个 isInitial、可达性

### US-P05：事件与因果链

> **前置依赖：** US-P01（事件数据通过 Manifest 导入）
> As an **AI Agent** (via MCP),
> I want **查询某个领域事件的定义、触发条件和后续动作**
> so that **理解事件驱动的业务逻辑**

**验收标准：**
- [ ] 数据库表支持存储：domainEvent、eventType、severity、causality
- [ ] REST API 返回事件完整定义
- [ ] 事件名过去式校验（警告级）

### US-P06：EPC 流程

> **前置依赖：** US-P01（EPC 数据通过 Manifest 导入）
> As an **AI Agent** (via MCP),
> I want **查询 EPC 流程的完整步骤和条件分支**
> so that **按预定义编排执行多步骤业务**

**验收标准：**
- [ ] 数据库表支持存储：epc_step(trigger/action/conditions/guards)
- [ ] REST API 按触发事件查询 EPC 链路

---

## 三、MCP Server — 对外暴露给任意 Agent

### US-P07：工具注册与发现（P0）

> **前置依赖：** US-P01（Manifest 导入后才有动态工具签名）
> As an **AI Agent**,
> I want **通过 MCP 协议发现所有可用的工具，包含名称、描述、参数 schema、所属业务域**
> so that **知道当前上下文下我能调用哪些能力**

**验收标准（对齐 Agent 要求文档）：**
- [ ] 实现 MCP `tools/list` 端点
- [ ] 每个工具声明：name / description / inputSchema / outputSchema / domain / riskLevel
- [ ] 按 domain 分组（对应 Ontology 的 bounded context）
- [ ] 工具签名由 Manifest 动态编译（非硬编码）
- [ ] **平台级基础工具**（固定 3 个）：resolve_intent / validate_instruction / traverse_graph
- [ ] **本体级业务工具**（动态）：execute_action / query_ontology 等，随导入的 Manifest 变化

### US-P08：认证与权限隔离（P0）

> **前置依赖：** 新建 governance 模块（token 签发、角色绑定、租户隔离）— 此为基础设施故事，应在 P01 前或并行完成
> As an **AI Agent**,
> I want **使用独立 access token 连接 MCP Server，只能看到自己被授权的工具和数据**
> so that **财务 Agent 不能操作供应链数据，这是企业合规底线**

**验收标准（对齐 Agent 要求文档）：**
- [ ] 连接层：每个 Agent 使用独立 token（从 platform governance 模块签发）
- [ ] 工具层：同 MCP Server，Agent A 只能看到 domain=a 的工具
- [ ] 数据层：返回数据按租户/部门隔离
- [ ] 操作层：写操作需审批流，高风险操作需二次确认
- [ ] governance 模块需新建：包含 token 管理、角色定义、权限绑定、租户隔离四个子模块

### US-P09：结果结构化（P0）

> **前置依赖：** US-P07（MCP 协议已通）
> As an **AI Agent** (via MCP),
> I want **所有工具返回结构化 JSON（含 status/data/metadata），而非自由文本**
> so that **Agent 门户能可靠解析并渲染为卡片/图表/表格**

**验收标准（对齐 Agent 要求文档）：**
- [ ] 返回格式统一：`{ status, data, metadata }`
- [ ] data 字段包含核心业务数据
- [ ] metadata 包含 version/generated_at/trace_id 等元信息
- [ ] 错误时返回结构化错误信息

### US-P10：语义查询（Agent 视角）

> **前置依赖：** US-P07（MCP 协议） + US-P04/P05/P06（行为/事件/EPC 表已建）
> As an **AI Agent** (via MCP),
> I want **通过 MCP 查询实体的完整语义定义（属性/关系/类型）**
> so that **理解业务对象结构，不硬编码字段名**

**验收标准：**
- [ ] resolve_intent 工具：自然语言 → 意图映射
- [ ] query_ontology 工具：查询指定实体的对象类型、属性、关系
- [ ] traverse_graph 工具：查询实体间的图关系链路
- [ ] validate_instruction 工具：校验 Agent 操作是否符合业务规则

### US-P11：行为与约束查询（Agent 视角）

> **前置依赖：** US-P10（语义查询通道已通） + US-P04（行为/状态机已实现）
> As an **AI Agent** (via MCP),
> I want **通过 MCP 查询某实体的行为、状态机、约束、权限、护栏规则**
> so that **不越权操作，不违反业务规则**

**验收标准：**
- [ ] execute_action 工具触发时自动校验 preRules
- [ ] 返回该实体可执行的动作列表及前置条件
- [ ] 返回约束规则和权限白名单

---

## 四、路线图 — P1/P2（本次不做）

| ID | 要求 | 优先级 | 说明 |
|----|------|--------|------|
| US-F01 | 异步执行 | P1 | 长时任务异步提交+轮询/Webhook |
| US-F02 | 幂等重试 | P1 | 写操作携带 idempotency_key |
| US-F03 | 可观测性 | P1 | 调用次数/延迟/成功率/Agent维度统计 |
| US-F04 | 版本兼容 | P2 | 工具名含版本号，旧版保留30天 |
| US-F05 | 限流成本 | P2 | 按Agent/Tool/租户限流 |
