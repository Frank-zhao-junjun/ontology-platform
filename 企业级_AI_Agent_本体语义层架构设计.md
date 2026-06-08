# 企业级 AI Agent 本体语义层架构设计与实现

> 基于 MCP 协议的大模型与本体互动工程实践

---

## 目录

1. [架构背景与核心问题](#1-架构背景与核心问题)
2. [核心概念定义](#2-核心概念定义)
3. [LLM 与本体的互动本质](#3-llm-与本体的互动本质)
4. [互动场景全景图](#4-互动场景全景图)
5. [MCP Server 作为互动桥梁](#5-mcp-server-作为互动桥梁)
6. [MCP Tool Schema 设计](#6-mcp-tool-schema-设计)
7. [LLM 调用 MCP Tool 的四层控制机制](#7-llm-调用-mcp-tool-的四层控制机制)
8. [意图路由：如何决定调用哪个 Tool](#8-意图路由如何决定调用哪个-tool)
9. [参数提取流水线](#9-参数提取流水线)
10. [追问策略与动态选项生成](#10-追问策略与动态选项生成)
11. [完整交互示例](#11-完整交互示例)
12. [企业级部署建议](#12-企业级部署建议)

---

## 1. 架构背景与核心问题

### 1.1 为什么需要本体语义层？

在企业级 AI Agent 系统中，大语言模型（LLM）面临以下固有局限：

| LLM 擅长 | LLM 不擅长 |
|---------|-----------|
| 理解模糊自然语言意图 | 精确记忆大量结构化领域知识 |
| 跨领域联想与常识推理 | 保证推理一致性与可复现性 |
| 自然语言生成与解释 | 处理复杂业务规则与约束 |
| 处理非结构化信息 | 精确数值计算与逻辑推导 |

**本体（Ontology）** 作为结构化外脑，恰好补齐这些短板：

- **概念定义**：精确描述领域概念（如供应商风险包含哪些维度）
- **关系建模**：显式表达概念间的继承、关联、约束关系
- **规则引擎**：执行业务规则，保证推理一致性
- **可解释性**：提供推理路径追溯，满足审计合规要求

### 1.2 核心架构问题

用户输入（自然语言）
    |
    v
LLM（通用大脑）  <- 理解意图、生成表达
    |
    | 如何互动？
    v
本体（专业外脑）  <- 精确推理、规则执行

核心问题：
1. LLM 何时需要调用本体？
2. 调用本体的哪个能力？
3. 如何传递参数？
4. 如何确保 LLM 一定会调用？
5. 调用失败或参数缺失时如何处理？

---

## 2. 核心概念定义

| 术语 | 定义 | 类比 |
|------|------|------|
| **LLM** | 大语言模型，负责自然语言理解、生成、常识推理 | 会思考但记忆不准的通用大脑 |
| **本体（Ontology）** | 形式化的领域知识表示，包含概念、关系、规则 | 记忆精确但不善变通的专家 |
| **Agent** | 基于 LLM 的自主任务执行体，可调用外部工具 | 学生/医生（需要查资料做决策） |
| **MCP（Model Context Protocol）** | AI 模型与外部工具通信的标准协议 | AI 世界的 HTTP/API 标准 |
| **MCP Server** | 托管 MCP Tool 的服务端 | 微服务容器 |
| **MCP Tool** | MCP Server 暴露的具体能力接口 | API 端点 |
| **MCP Tool Schema** | Tool 的接口定义（参数、返回值、描述） | OpenAPI/Swagger 文档 |
| **意图路由** | 根据用户输入决定调用哪个 Tool 的机制 | 路由器/调度器 |
| **参数提取** | 从自然语言中抽取结构化参数的过程 | 表单自动填充 |
| **追问策略** | 参数缺失时的交互补全机制 | 智能表单验证 |

---

## 3. LLM 与本体的互动本质

### 3.1 不是查字典，而是共同思考

| 互动模式 | 说明 | 适用场景 |
|---------|------|---------|
| **静态查询** | LLM 查一次本体，拿到定义直接回答 | 简单术语解释 |
| **动态推理** | LLM 与本体多轮交互，任务推进中不断协作 | 复杂决策、风险评估 |
| **双向演化** | Agent 执行结果反哺本体更新 | 持续运营的知识库 |

### 3.2 分工原则

LLM 负责（模糊到结构化）：
- 理解用户自然语言意图
- 生成初步计划草案
- 解释业务数据的含义
- 将结构化结论转化为自然语言
- 总结案例经验

本体负责（结构化到精确推理）：
- 精确查询概念定义与关系
- 验证计划完整性与约束
- 执行规则引擎推理
- 提供推理路径与依据
- 结构化存储与规则优化

### 3.3 互动流程（以供应商风险评估为例）

Phase 1: 意图理解
用户：帮我看看供应商 ABC 的风险
    |
    v
LLM 思考：用户关心供应商风险，但风险很模糊，需要结构化
    |
    v
LLM -> 本体: ontology_intent_parse(query="供应商 ABC 风险")
    |
    v
本体返回：核心概念=SupplierRisk，维度=[财务,履约,合规,ESG]
    |
    v
LLM 理解：需要聚合多维度做风险推理

Phase 2: 任务规划
LLM 生成初步计划 -> 本体验证 -> 修正计划

Phase 3: 数据获取
LLM 执行查询 -> 拿到原始数据 -> 问本体这数字什么意思

Phase 4: 综合推理
LLM 提交事实 -> 本体规则引擎计算 -> LLM 解释结论

Phase 5: 结果沉淀
LLM 总结案例 -> 本体存储并发现规则盲区

---

## 4. 互动场景全景图

### 4.1 十一类核心互动场景

| # | 场景 | LLM 角色 | 本体角色 | 触发时机 |
|---|------|---------|---------|---------|
| 1 | **概念查询** | 提问者 | 知识库 | 遇到不确定的专业概念 |
| 2 | **任务规划验证** | 生成者 | 审核者 | 生成多步计划后 |
| 3 | **语义标准查询** | 理解者 | 解释者 | 拿到业务数据需要判断 |
| 4 | **规则推理** | 提供事实 | 精确计算 | 多维度事实需要综合判断 |
| 5 | **存储反馈** | 经验提炼者 | 结构化存储 | 任务完成有验证结果 |
| 6 | **冲突裁决** | 检测冲突 | 权威裁定 | 多规则/多 Agent 结论矛盾 |
| 7 | **概念演化** | 发现新概念 | 扩展定义 | 现有概念无法覆盖新场景 |
| 8 | **路径回溯** | 请求解释 | 返回推导链 | 用户质疑结论依据 |
| 9 | **上下文继承** | 使用指代 | 维护实体绑定 | 多轮对话中的这个该 |
| 10 | **跨本体映射** | 发现术语差异 | 提供对齐关系 | 不同系统术语不一致 |
| 11 | **假设推演** | 提出假设 | 执行反事实推理 | 用户问如果...会怎样 |

### 4.2 场景详细说明

#### 场景 1：概念查询（ontology_intent_parse）

**目的**：将自然语言意图映射到本体概念图谱。

**输入**：用户原始查询字符串。

**输出**：
- 核心概念（如 SupplierRisk）
- 关联概念列表
- 涉及的实体实例（如供应商 ABC）
- 建议的查询维度

**LLM 触发时机**：收到任何业务相关查询的第一步。

#### 场景 2：任务规划验证（ontology_plan_validate）

**目的**：检查 LLM 生成的执行计划是否完整、合规。

**输入**：初步执行计划（步骤列表）。

**输出**：
- 遗漏步骤
- 顺序调整建议
- 约束条件提醒
- 依赖关系修正

**LLM 触发时机**：生成多步骤计划后，执行前。

#### 场景 3：语义标准查询（ontology_semantic_check）

**目的**：解释业务数据在本体中的含义和判断标准。

**输入**：原始数据值 + 概念名。

**输出**：
- 字段业务定义
- 阈值标准（如制造业 debt_ratio > 0.7 为高风险）
- 关联检查项
- 数据质量状态（如是否过期）

**LLM 触发时机**：拿到原始数据后，做业务判断前。

#### 场景 4：规则推理（ontology_reason_execute）

**目的**：基于事实执行精确的逻辑/数学推理。

**输入**：结构化事实数据。

**输出**：
- 综合结论（如风险等级）
- 置信度
- 触发因素权重
- 建议行动

**LLM 触发时机**：多维度数据收集完毕后。

#### 场景 5：存储反馈（ontology_learn_case）

**目的**：记录验证案例，更新规则命中率，发现盲区。

**输入**：案例摘要 + 预测结果 + 实际结果。

**输出**：
- 规则命中率统计
- 新发现的规则盲区
- 本体扩展建议

**LLM 触发时机**：任务完成，有验证反馈时。

#### 场景 6：冲突裁决（ontology_conflict_resolve）

**目的**：解决多规则或多 Agent 结论冲突。

**输入**：冲突项列表（来源、结论、置信度）。

**输出**：
- 最终裁决结论
- 裁决路径（为什么选这个）
- 被否决的异议及原因

**LLM 触发时机**：检测到结论矛盾时。

#### 场景 7：概念演化（ontology_concept_evolve）

**目的**：发现现有概念无法覆盖新场景，触发本体扩展。

**输入**：新场景描述 + 现有概念匹配失败记录。

**输出**：
- 建议新增概念
- 与现有概念的关系建议
- 审批流程触发

**LLM 触发时机**：反复遇到无法归类的业务场景。

#### 场景 8：路径回溯（ontology_explain_trace）

**目的**：追溯推理结论的推导路径。

**输入**：结论 + 目标实体。

**输出**：
- 完整推理路径（概念->关系->规则->结论）
- 每步的依据
- 置信度传递

**LLM 触发时机**：用户质疑为什么是这个结论。

#### 场景 9：上下文继承（ontology_context_bind）

**目的**：多轮对话中保持概念实例的状态一致性。

**输入**：当前查询 + 会话历史。

**输出**：
- 指代消解结果
- 当前活跃的实体绑定
- 缺失的上下文提示

**LLM 触发时机**：出现该供应商这个合同等指代时。

#### 场景 10：跨本体映射（ontology_cross_map）

**目的**：不同部门/系统本体概念不一致时对齐。

**输入**：源概念 + 目标系统。

**输出**：
- 概念等价关系
- 属性映射
- 关系映射
- 冲突标记与转换函数

**LLM 触发时机**：多系统集成时发现术语差异。

#### 场景 11：假设推演（ontology_what_if）

**目的**：基于当前本体模拟反事实场景。

**输入**：假设条件 + 基准状态。

**输出**：
- 推演结论
- 关键敏感因子
- 置信度变化

**LLM 触发时机**：用户问如果...会怎样。

---

## 5. MCP Server 作为互动桥梁

### 5.1 为什么需要 MCP Server？

LLM 不直接查询图数据库（Neo4j）或 SQL，而是通过 MCP Server 中转：

| 优势 | 说明 |
|------|------|
| **协议标准化** | Agent 只需理解 MCP，无需适配 Cypher/SPARQL/SQL |
| **安全隔离** | 查询权限、敏感字段脱敏在 MCP 层统一控制 |
| **抽象封装** | 将复杂图查询封装为高阶 Tool（如 query_ontology） |
| **多源聚合** | 一个 Tool 内部串联图库 + SQL + 向量检索 |
| **可观测性** | 统一日志、审计、监控 |

### 5.2 推荐架构

AI Agent 层（Claude/GPT/自研 Agent）
    | MCP 协议 (stdio/SSE)
    v
MCP Server 网关层（你构建）
    |-- 暴露语义查询 Tools
    |-- 封装权限、审计、限流
    |-- 多数据源路由（图库/SQL/向量库）
    |
    |---> 图数据库（本体/知识图谱）
    |---> SQL（事务性业务数据）
    |---> 向量数据库（语义检索/相似度）

---

## 6. MCP Tool Schema 设计

### 6.1 Schema 是什么？

MCP Tool Schema 是 MCP Server 对外暴露能力的接口说明书，包含：

- **name**：Tool 名称
- **description**：功能描述（LLM 靠这个决定是否调用）
- **parameters**：参数定义（类型、必填/选填、描述）
- **returns**：返回值结构

### 6.2 十一类场景的 Tool Schema 示例

#### Tool 1: ontology_intent_parse

```json
{
  "name": "ontology_intent_parse",
  "description": "解析用户自然语言意图，识别涉及的本体概念（如供应商、合同、风险等），返回概念图谱和查询计划。所有涉及供应商、合同、财务、合规的问题必须先调用此工具。",
  "parameters": {
    "type": "object",
    "required": ["query"],
    "properties": {
      "query": {
        "type": "string",
        "description": "用户的原始自然语言查询"
      },
      "context": {
        "type": "object",
        "description": "会话上下文，包含已提及的实体",
        "properties": {
          "mentioned_entities": {
            "type": "array",
            "items": {"type": "string"}
          }
        }
      }
    }
  },
  "returns": {
    "type": "object",
    "properties": {
      "core_concept": {"type": "string", "description": "核心概念名，如 SupplierRisk"},
      "related_concepts": {"type": "array", "items": {"type": "string"}},
      "involved_entities": {"type": "array", "items": {"type": "object"}},
      "suggested_dimensions": {"type": "array", "items": {"type": "string"}},
      "query_plan_draft": {"type": "array", "items": {"type": "string"}}
    }
  }
}
```

#### Tool 2: ontology_plan_validate

```json
{
  "name": "ontology_plan_validate",
  "description": "验证任务执行计划是否完整、是否符合本体约束，检查遗漏步骤和顺序问题。",
  "parameters": {
    "type": "object",
    "required": ["plan", "target_concept"],
    "properties": {
      "plan": {
        "type": "array",
        "items": {
          "type": "object",
          "properties": {
            "step": {"type": "integer"},
            "action": {"type": "string"},
            "source": {"type": "string", "enum": ["sql", "graph", "api"]},
            "query": {"type": "string"}
          }
        }
      },
      "target_concept": {"type": "string", "description": "目标概念名"}
    }
  },
  "returns": {
    "type": "object",
    "properties": {
      "valid": {"type": "boolean"},
      "missing_steps": {"type": "array", "items": {"type": "string"}},
      "order_adjustments": {"type": "array", "items": {"type": "string"}},
      "constraint_violations": {"type": "array", "items": {"type": "string"}},
      "corrected_plan": {"type": "array"}
    }
  }
}
```

#### Tool 3: ontology_semantic_check

```json
{
  "name": "ontology_semantic_check",
  "description": "解释业务数据在本体中的含义、阈值标准和质量规则。",
  "parameters": {
    "type": "object",
    "required": ["data", "concept"],
    "properties": {
      "data": {"type": "object", "description": "原始数据键值对"},
      "concept": {"type": "string", "description": "数据所属概念名"},
      "industry": {"type": "string", "description": "行业上下文（影响阈值）", "default": "manufacturing"}
    }
  },
  "returns": {
    "type": "object",
    "properties": {
      "field_definitions": {"type": "object"},
      "thresholds": {"type": "object"},
      "data_quality": {"type": "object"},
      "suggested_checks": {"type": "array", "items": {"type": "string"}}
    }
  }
}
```

#### Tool 4: ontology_reason_execute

```json
{
  "name": "ontology_reason_execute",
  "description": "基于事实数据执行本体规则推理，得出综合结论（如风险等级判定）。",
  "parameters": {
    "type": "object",
    "required": ["facts"],
    "properties": {
      "facts": {"type": "object", "description": "结构化事实数据"},
      "rule_set": {"type": "string", "default": "default"},
      "reasoning_type": {"type": "string", "enum": ["risk_synthesis", "compliance_check", "cost_optimization"]}
    }
  },
  "returns": {
    "type": "object",
    "properties": {
      "conclusion": {"type": "string"},
      "confidence": {"type": "number"},
      "trigger_factors": {"type": "array"},
      "suggested_actions": {"type": "array"},
      "rule_trace": {"type": "array"}
    }
  }
}
```

#### Tool 5: ontology_learn_case

```json
{
  "name": "ontology_learn_case",
  "description": "记录验证案例，更新规则命中率，发现本体盲区。",
  "parameters": {
    "type": "object",
    "required": ["case_summary", "predicted", "actual"],
    "properties": {
      "case_summary": {"type": "string"},
      "predicted": {"type": "string"},
      "actual": {"type": "string"},
      "rule_triggered": {"type": "string"},
      "suggestion": {"type": "string"}
    }
  },
  "returns": {
    "type": "object",
    "properties": {
      "stored": {"type": "boolean"},
      "rule_hit_rate": {"type": "number"},
      "discovered_gaps": {"type": "array"},
      "update_suggestions": {"type": "array"}
    }
  }
}
```

#### Tool 6: ontology_conflict_resolve

```json
{
  "name": "ontology_conflict_resolve",
  "description": "当多条规则或多个Agent结论冲突时，基于本体中的权威链定义进行裁决。客户相关Agent具有最高裁决权。",
  "parameters": {
    "type": "object",
    "required": ["conflicts", "task_context"],
    "properties": {
      "conflicts": {
        "type": "array",
        "items": {
          "type": "object",
          "properties": {
            "source": {"type": "string", "enum": ["rule_engine", "case_base", "agent_finance", "agent_supply", "agent_customer"]},
            "conclusion": {"type": "string"},
            "confidence": {"type": "number"},
            "rule_id": {"type": "string"}
          }
        }
      },
      "task_context": {
        "type": "object",
        "properties": {
          "domain": {"type": "string", "enum": ["supplier_risk", "contract_review", "compliance_check"]},
          "priority_source": {"type": "string"}
        }
      }
    }
  },
  "returns": {
    "type": "object",
    "properties": {
      "final_decision": {"type": "string"},
      "arbitration_path": {"type": "array"},
      "dissenting_views": {"type": "array"}
    }
  }
}
```

#### Tool 7-11: 其他工具（简述）

| Tool | 核心参数 | 核心返回 |
|------|---------|---------|
| ontology_concept_evolve | new_scenario, failed_matches | suggested_concept, relation_proposal |
| ontology_explain_trace | conclusion, target_entity | reasoning_path, step_details |
| ontology_context_bind | current_query, session_id | resolved_entities, missing_bindings |
| ontology_cross_map | source_concept, target_system | mapping_rules, conflicts |
| ontology_what_if | assumptions, baseline | projected_result, sensitive_factors |

---

## 7. LLM 调用 MCP Tool 的四层控制机制

### 7.1 核心问题：LLM 不一定会调用 Tool

LLM 调用 Tool 是概率性行为，可能：
- 自信幻觉：觉得这题我会，直接编造答案
- 工具描述模糊：不知道何时该调用
- 成本规避：减少调用以节省延迟

### 7.2 四层控制架构

用户输入
    |
    v
第一层：Prompt 控制  <- 软性引导，让 LLM 倾向于调用
    |
    v
第二层：API 强制  <- tool_choice="required"，硬性阻断
    |
    v
第三层：架构硬控制  <- 代码状态机，LLM 只生成参数
    |
    v
第四层：校验兜底  <- 事后审计，发现编造则拒绝
    |
    v
通过 -> 输出
未通过 -> 重试/人工/降级

### 7.3 第一层：Prompt 控制

核心策略：让 LLM 先思考工具，再思考答案。

系统提示要求：
1. 收到输入后，先在 thinking 标签内完成思考
2. 完成思考后，必须输出 tool_call 标签
3. 只有在收到 tool_result 后，才能输出 answer
4. 工具返回错误时，必须输出 error，禁止编造

### 7.4 第二层：API 强制

方式 A：强制调用任意工具
- tool_choice="required"

方式 B：强制调用特定工具
- tool_choice={"type": "function", "function": {"name": "ontology_intent_parse"}}

### 7.5 第三层：架构硬控制（核心）

process() 是状态机，LLM 无法跳过阶段：

1. 意图解析（代码强制调用，LLM 只负责生成参数）
2. 规划验证（代码决定调用，LLM 生成参数）
3. 数据查询（代码按规划调用，LLM 不参与）
4. 规则推理（代码强制调用，LLM 生成参数）
5. 生成回答（LLM 必须有结论才能生成）

关键设计：LLM 的角色被限制为参数生成器和最终表达器。

### 7.6 第四层：校验兜底

验证回答中的每个事实声明是否在工具结果中有依据。
如果发现无来源的声明，拒绝回答并要求重新调用工具。

---

## 8. 意图路由：如何决定调用哪个 Tool

### 8.1 三层混合路由

用户输入
    |
    v
第一层：规则快速匹配  <- 代码硬逻辑，毫秒级（关键词/正则）
    | 未匹配
    v
第二层：语义相似度  <- 意图向量 vs Tool向量
    | 低相似/多候选
    v
第三层：LLM 裁决  <- 描述 + 候选Tool列表
    |
    v
明确 -> 路由到Tool
模糊 -> 拒绝/追问

### 8.2 Tool 注册表（带意图标签）

每个 Tool 注册时包含：
- name: Tool 名称
- description: 给 LLM 看的详细描述
- intent_tags: 意图关键词标签
- intent_patterns: 正则模式
- required_entities: 必须提取的实体
- handler: 实际执行函数
- embedding: 向量嵌入（预计算）

### 8.3 路由引擎实现

1. 第一层：规则匹配
   - 关键词匹配
   - 正则匹配

2. 第二层：语义匹配
   - 计算用户输入与 Tool 描述的向量相似度
   - 超过阈值则匹配

3. 第三层：LLM 裁决
   - 取语义相似度 top 3 作为候选
   - 让 LLM 从候选中选择最匹配的
   - 限制输出范围，避免幻觉

---

## 9. 参数提取流水线

### 9.1 四层提取架构

用户输入
    |
    v
L1: 直接映射  <- 代码硬提取，零延迟（简单参数）
    | 不匹配
    v
L2: 实体识别  <- 正则/NER，毫秒级（命名实体）
    | 不匹配
    v
L3: 上下文补全  <- 会话状态查询（指代消解）
    | 仍缺失
    v
L4: LLM 结构化  <- 通用兜底，有延迟（复杂条件）
    |
    v
参数校验（JSON Schema）
    |
    v
通过 -> 执行Tool
失败 -> 追问/拒绝

### 9.2 各层实现要点

**L1 直接映射**：单参数工具直接传递用户输入。

**L2 实体识别**：正则 + NER 提取供应商名、时间范围、指标、比较条件等。

**L3 上下文补全**：会话状态维护已提及实体，消解该供应商这个等指代。

**L4 LLM 结构化**：处理复杂嵌套条件（如近6个月延迟率>20%且环保违规过，或者财务评分<<60）。

### 9.3 参数校验

1. 检查必填项
2. JSON Schema 校验
3. 业务规则校验（如 debt_ratio 必须在 0-1 之间）
4. 生成追问建议

---

## 10. 追问策略与动态选项生成

### 10.1 核心原则

| 原则 | 说明 |
|------|------|
| 精准定位 | 只问缺失的参数，不问已提供的 |
| 选项引导 | 给选择题，不给开放题 |
| 上下文继承 | 追问时保留已确认信息 |
| 渐进披露 | 一次只问 1-2 个缺失项 |
| 退出机制 | 用户可跳过或终止 |

### 10.2 追问类型

| 缺失类型 | 追问方式 |
|---------|---------|
| 必填缺失 | 直接追问 + 示例 |
| 类型错误 | 纠正 + 格式示例 |
| 范围超限 | 提示边界 |
| 歧义多解 | 选项确认 |
| 条件冲突 | 优先级确认 |
| 权限不足 | 降级或拒绝 |

### 10.3 动态选项生成（关键）

选项不是硬编码的，而是从本体动态查询的：

查询本体获取概念的合法维度：
MATCH (c:Concept {name: $concept})-[:HAS_DIMENSION|HAS_AGGREGATION]->(d)
RETURN d.name as option

为什么必须动态生成？
- 新增风险维度：本体更新即可，选项自动扩展
- 不同业务域：根据概念上下文动态拉取
- 权限隔离：查询时过滤用户可见维度
- 多语言：本体存储多语言标签

### 10.4 追问会话管理

ClarificationSession 维护：
- session_id
- original_input
- target_tool
- pending_questions
- answered_params
- attempt_count（最多3轮）
- status（active / resolved / abandoned）

---

## 11. 完整交互示例

### 场景：供应商风险评估（全流程）

用户输入：帮我看看供应商 ABC 的风险

Phase 1: 意图解析
系统：【架构硬控制】强制调用 ontology_intent_parse
      参数：{query: "帮我看看供应商 ABC 的风险"}

本体返回：
  - 核心概念: SupplierRisk
  - 关联概念: 财务健康度、履约准时率、合规记录、ESG评分
  - 涉及实体: 供应商ABC (ID: 8823)
  - 建议维度: [财务, 履约, 合规]

LLM 理解：需要聚合多维度做风险推理

Phase 2: 规划验证
LLM 生成初步计划 -> 本体验证 -> 修正计划

Phase 3: 数据获取
系统执行查询 -> 拿到原始数据 -> 问本体这数字什么意思

Phase 4: 综合推理
LLM 提交事实 -> 本体规则引擎计算 -> LLM 解释结论

Phase 5: 结果沉淀
LLM 总结案例 -> 本体存储并发现规则盲区

---

## 12. 企业级部署建议

### 12.1 技术选型

| 层级 | 推荐技术 |
|------|---------|
| 语义核心层 | Neo4j / Amazon Neptune / 阿里云图数据库 |
| 事务数据层 | PostgreSQL / MySQL |
| 向量检索层 | Milvus / Pinecone |
| MCP Server | Python FastMCP / TypeScript SDK |
| 规则引擎 | Drools / 自研 |
| 会话状态 | Redis |

### 12.2 关键设计原则

| 原则 | 实现 |
|------|------|
| LLM 最小化决策 | 是否调用工具由代码控制，LLM 只生成参数 |
| 本体唯一真相源 | 概念定义、合法取值、规则全部来自本体 |
| 四层防御 | Prompt + API + 架构 + 校验，逼近确定性 |
| 可审计追溯 | 每步调用记录工具名、参数、结果、推理路径 |
| 渐进演化 | 案例反馈自动更新规则命中率，人工审核本体扩展 |

### 12.3 针对多 Agent 架构的特别建议

| Agent 类型 | 控制层组合 |
|-----------|-----------|
| 普通查询 Agent | Prompt + API |
| 业务决策 Agent | API + 架构 + 校验 |
| 冲突裁决 Agent | 架构为主 |

核心原则：越接近客户视角裁决的 Agent，越减少 LLM 自由度，增加代码硬控制。

---

## 总结

企业级 AI Agent 本体语义层的核心设计：

- LLM 是会思考但记忆不准的通用大脑
- 本体是记忆精确但不善变通的专业外脑
- 两者通过 MCP 协议持续对话
- LLM 负责理解模糊意图、生成草案、自然语言交互
- 本体负责精确查询、规则校验、结构化推理
- MCP Server 作为标准化桥梁解耦两者
- 四层控制机制确保 LLM 一定会调用工具
- 动态选项生成确保追问内容来自本体而非硬编码
