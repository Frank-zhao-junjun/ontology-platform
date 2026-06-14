# 本体建模平台 — 用户故事地图 v1.2

> 版本：v1.2.1
> 状态：Draft
> 日期：2026-06-14
> 适用：Ontology 设计台（Next.js）+ ontology-platform 治理平台（Spring Boot）

---

## §0 双产品定位

```
┌──────────────────────────────────────────────────────┐
│                  本体建模平台 总目标                    │
│  让 AI Agent 通过 MCP 查询完整的本体语义/行为/事件/    │
│  EPC/权限，以本体作为语义标准、规则依据和审计来源      │
└──────────────────────┬───────────────────────────────┘
                       │
          ┌────────────┴────────────┐
          ▼                         ▼
┌──────────────────┐    ┌────────────────────────┐
│  项目1：设计台     │    │  项目2：治理平台        │
│  Next.js +       │    │  Spring Boot + AGE     │
│  Zustand persist │    │  + MCP Server          │
│                  │    │                        │
│  建模草稿 / 导出  │───►│  导入/校验/发布/版本    │───► MCP
│  五层模型 UI     │    │  治理/MCP Server       │
└──────────────────┘    └────────────────────────┘
```

---

## §1 发现层 — Manifest 导入与发布

### US-P01：一键导入 Manifest
> As a **平台管理员**，通过 REST API 上传设计台导出的 YAML/JSON，五层模型全部落库

**依赖：** 无（入口故事）

### US-P02：导入校验
> As a **平台管理员**，导入时自动执行 V01~V11 校验，不合法的 Manifest 拒绝导入

**依赖：** US-P01

### US-P03：导入预览与发布
> As a **平台管理员**，导入后可预览变更 diff，确认后再 publish

**依赖：** US-P01

### US-P03b：Manifest 导出
> As a **平台管理员**，将已发布的 Manifest 导出为 YAML/JSON，完成 platform → runtime 闭环

**依赖：** US-P03

---

## §2 动力层+事件层 — 补齐四层架构

### US-P04：行为与状态机
> As an **AI Agent** (via MCP)，查询某个实体有哪些行为、状态机、前置条件

**依赖：** US-P01（行为数据通过 Manifest 导入）

### US-P05：事件与因果链
> As an **AI Agent** (via MCP)，查询领域事件的定义、触发条件和后续动作

**依赖：** US-P01（事件数据通过 Manifest 导入）

### US-P06：EPC 流程
> As an **AI Agent** (via MCP)，查询 EPC 流程的完整步骤和条件分支

**依赖：** US-P01（EPC 数据通过 Manifest 导入）

---

## §3 MCP Server — 对外暴露给任意 Agent

### MCP 三步路线图

```
Step 1 ── 基础 MCP 骨架 ──→ tools/list + tools/call（JSON-RPC 2.0）
Step 2 ── 固定工具集 ──→ resolve_intent / query_ontology / traverse_graph / validate_instruction
Step 3 ── 动态编译 ──→ Manifest → 动态工具签名 + execute_action + RBAC
```

### US-P07：工具注册与发现（P0）
> As an **AI Agent**，通过 MCP 协议发现所有可用工具

**依赖：** US-P01（Manifest 导入后才有动态工具签名）

### US-P08：认证与权限隔离（P0）
> As an **AI Agent**，使用独立 token 连接 MCP Server，只能看到被授权的工具和数据

**依赖：** Governance 模块（token 签发/角色/权限）

### US-P09：结果结构化（P0）
> As an **AI Agent**，所有工具返回结构化 JSON（status/data/metadata）

**依赖：** US-P07

### US-P10：语义查询（Agent 视角）
> As an **AI Agent**，通过 MCP 查询实体的完整语义定义

**依赖：** US-P07 + US-P04/P05/P06

### US-P11：行为与约束查询（Agent 视角）
> As an **AI Agent**，查询实体的行为/状态机/约束/权限/护栏规则

**依赖：** US-P10 + US-P04

---

## §4 设计台 — 建模 UI

### US-D01：五层模型可视化建模
> As a **建模者**，在画布上拖拽创建聚合根、对象、关系，并配置属性、状态、数据获取方式

**依赖：** 无

### US-D02：行为定义编辑器
> As a **建模者**，为实体定义行为（preRules/postRules/参数 Schema/事件）

**依赖：** US-D01

### US-D03：事件与因果定义
> As a **建模者**，定义领域事件、事件类型、严重级别、因果链

**依赖：** US-D01

### US-D04：EPC 流程编辑器
> As a **建模者**，通过拖拽定义 EPC 步骤（trigger/action/conditions/guards）

**依赖：** US-D01/US-D02/US-D03

### US-D05：Manifest 导出
> As a **建模者**，一键导出 OntologyManifest YAML，供 project2 导入使用

**依赖：** US-D01~D04

---

## §5 依赖全景

```
设计台                       治理平台
 US-D01 ──────────────────┐
  (五层建模)               │
   │                       ├── US-P01 (导入) ──→ US-P02 (校验)
 US-D02 (行为) ───────────┤                          │
 US-D03 (事件) ───────────┤                          ▼
 US-D04 (EPC) ───────────┤                     US-P03 (预览/发布) ──→ US-P03b (导出)
   │                       │
   ▼                       ▼
 US-D05 ──────────────────►│
  (导出 YAML)              │
                           ├── US-P04 (行为查询) ──→ US-P07~P11 (MCP)
                           ├── US-P05 (事件查询) ──→ US-P07~P11
                           ├── US-P06 (EPC查询) ───→ US-P07~P11
                           │
                           └── US-P08 (治理) ──────→ 贯穿所有 MCP
```

---

## §6 P1/P2（本次不做）

| ID | 要求 | 优先级 |
|----|------|--------|
| US-F01 | 异步执行（长时任务提交+轮询/Webhook） | P1 |
| US-F02 | 幂等重试（写操作携带 idempotency_key） | P1 |
| US-F03 | 可观测性（调用次数/延迟/成功率/Agent维度） | P1 |
| US-F04 | 版本兼容（工具名含版本号，旧版保留30天） | P2 |
| US-F05 | 限流成本（按 Agent/Tool/租户限流） | P2 |
