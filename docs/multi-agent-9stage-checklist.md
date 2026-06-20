# 生产级多Agent系统：9阶段方法论速查

> 来源：今日头条 · 老猿视角 · 2026-06-01  
> 整理时间：2026-06-20  
> 对照参考：`multi-agent-collaboration-workflow` (Hermes Skill)

---

## 总览：三层九阶

```
┌─────────────────────────────────────────┐
│  第三部分 · 生产级 Crew                 │
│  ⑦ 记忆+持久化+沙盒                    │
│  ⑧ 评估+轨迹检查+CI门禁                │
│  ⑨ 权限+人工检查点                     │
├─────────────────────────────────────────┤
│  第二部分 · 协同层                      │
│  ④ 隔离上下文的子Agent                  │
│  ⑤ 只规划不执行的协调器                │
│  ⑥ 共享任务列表                        │
├─────────────────────────────────────────┤
│  第一部分 · 单Agent根基                 │
│  ① Agent循环（不是聊天）               │
│  ② 精心设计上下文                      │
│  ③ 类型化工具                          │
└─────────────────────────────────────────┘
```

---

## 第一部分 · 单Agent根基

### ① Agent循环

聊天 ≠ Agent。Agent 是一个**循环**：

```
目标 → 决策(模型) → 动作 → 执行 → 观察 → 再决策... → 停止
                                  ↑__________↓
```

**必须包含**：
- 审批闸门 (`if requires_approval: wait_for_human()`)
- 日志挂钩
- 停止条件（目标达成 / max_iterations / 用户中止 / 错误阈值）
- **永远设置 max_iterations**（30–50次）

**与我们 Skill 的关系**：`multi-agent-collaboration-workflow` 的接力模式本质就是这个循环的跨Agent版本——每阶段(Kimi→Claude→Codex→Hermes)是循环的一个Action，输出作为下一个Action的观察。

### ② 精心设计上下文

上下文失败三原因：没拿到 / 太多 / 过时。四关键操作：

| 操作 | 做法 |
|:----:|:-----|
| **写入** | 每一步刻意添加，每行消耗token |
| **选择** | 从记忆或文件检索，不是倾倒 |
| **压缩** | 上下文将满时压缩早期为摘要，保留决策 |
| **隔离** | 子Agent跑在自己上下文，主线程洁净 |

**结构化上下文模板**：
```json
{
  "goal": "...",
  "plan": [...],
  "recent_observations": [...],
  "decisions_log": "memory/decisions.md",
  "subagent_summaries": []
}
```

### ③ 类型化工具

生产事故主因：**工具没有schema，模型靠猜**。

每个工具必须声明：

| 字段 | 说明 | 示例 |
|:----|:-----|:-----|
| `name` | 工具名 | `delete_file` |
| `parameters` | 类型化参数 | `{path: string}` |
| `preconditions` | 前置条件 | 文件不在保护目录 |
| `side_effects` | 副作用 | 永久删除 |
| `requires_approval` | 是否需要审批 | true |
| `blocked_targets` | 禁止目标 | src/auth/ |

> 关键：**执行框架强制执行，模型绕不过去**。

---

## 第二部分 · 协同层

### ④ 带隔离上下文的子Agent

子Agent是专家，自己的上下文/工具集/模型：

```python
spawn_subagent(
    role="research",
    model="claude-haiku-4-5",
    goal="找出src/中所有缺少认证的API端点",
    tools=["grep", "read_file"],
    return_format="summary + file list"
)
```

**`return_format` 比任何提示词都重要**——强制结构化返回。

**与我们 Skill 的对照**：我们接力模式中每个阶段的产出(tasks.md/spec.md/Test)就是 return_format。Hermes 的 `delegate_task` 也隐含了隔离上下文。

### ⑤ 只规划、不执行的协调器

**协调器职责**：规划 → 委派 → 收集。**不写代码、不跑查询、不直接调API**。

```python
def orchestrator_loop(goal):
    plan = make_plan(goal)
    for step in plan:
        result = run_subagent(pick_specialist(step), step)
        if plan_needs_revision(result): plan = revise_plan(plan, result)
    return synthesize(results)
```

**成本模型**：协调器用最强模型（Opus），子Agent用Sonnet/Haiku → **5-10倍任务量**。

**与我们 Skill 的对照**：我们的 接力模式中 Hermes=协调器，只编排不写代码。而Skill本身就是"子Agent的战术手册"。

### ⑥ 共享任务列表

唯一结构化任务状态，三要素：**执行者、依赖关系、状态**。

```json
{
  "tasks": [
    { "id": "t1", "status": "done", "assignee": "subagent_a" },
    { "id": "t2", "status": "in_progress", "assignee": "subagent_b", "depends_on": ["t1"] },
    { "id": "t3", "status": "pending", "depends_on": ["t2"] }
  ]
}
```

**与我们 Skill 的对照**：我们的 `writing-plans` 产出 `tasks.md` 带 category + depends_on，本质上就是共享任务列表。

---

## 第三部分 · 生产级Crew

### ⑦ 记忆、持久化与沙盒

| 机制 | 做法 |
|:----|:------|
| **记忆** | 结构化存储（不是日志）：`memory/decisions.md`、`memory/conventions.md`、`memory/known_failures.md` |
| **持久化** | 每一步执行前写磁盘 → 第47步崩溃不用重跑 |
| **沙盒** | 容器/受限子进程，只能访问显式授权的资源 |

**与我们 Skill 的对照**：Skill 本身就是"持久化+记忆"的结构化形式。踩坑存的 `skill_patch()` = `known_failures.md`。

### ⑧ 评估与轨迹检查

三层度量：

| 层 | 内容 |
|:--:|:------|
| **评估集** | 20-100冻结任务+已知好结果，每次改动后跑 |
| **轨迹检查** | 不仅看结果，还看工具调用顺序是否正确 |
| **CI门禁** | PR自动跑评估集，通过率低于阈值则拦住 |

**评估用例示例**：
```json
{
  "input": "为未保护端点加JWT",
  "expected": { "endpoints_protected": 11, "files_touched": [...] },
  "trajectory_must_include": ["grep", "读取认证模块", "应用中间件", "运行测试"]
}
```

> `trajectory_must_include` 是秘密武器——能抓住通过错误路径得到正确答案的"假通过"。

### ⑨ 权限与人工检查点

权限文件声明三类操作：

| 级别 | 含义 | 示例 |
|:----:|:-----|:-----|
| ✅ 始终允许 | 无需审批 | 读文件、跑测试、建分支、写 memory/skills |
| ⚠️ 需要审批 | 人工确认 | 合并PR、部署、删文件、装依赖、改CI |
| ❌ 永远禁止 | 执行框架封锁 | 强制推main、访问凭证、改权限文件 |

> 最重要的是：**划清"可以放着过夜跑"和"必须盯着"的界限**。

---

## 最常跳过的坑（及对策）

| # | 坑 | 对策 |
|:-:|:---|:-----|
| 1 | 无真正循环 | 显式 `while + max_iterations` |
| 2 | 自由格式上下文 | 结构化上下文 JSON |
| 3 | 无类型工具 | 工具 schema + preconditions + blocked_targets |
| 4 | 子Agent无隔离 | 独立上下文窗口，只收摘要 |
| 5 | 协调器亲自执行 | 规划→委派→收集，三件事分明 |
| 6 | 无共享任务列表 | tasks.md + status + depends_on |
| 7 | 无持久化 | 每步写磁盘 |
| 8 | 无评估 | 评估集 + trajectory 检查 + CI门禁 |
| 9 | 无权限文件 | `permissions.md` + 执行框架读取 |

---

## 与我们现有工作流的对照

| 9阶段 | 我们的 Skill | 差距 |
|:------|:-------------|:-----|
| ① Agent循环 | 接力模式 = 跨Agent循环 | ✅ |
| ② 上下文设计 | delegate_task 隔离、Skill 注入 | ✅ |
| ③ 类型化工具 | ACP bridge 有 schema | ✅ 但可加强 |
| ④ 隔离子Agent | delegate_task + subagent | ✅ |
| ⑤ 协调器不执行 | Hermes=PM不写代码 | ✅ |
| ⑥ 共享任务列表 | tasks.md（writing-plans） | ✅ |
| ⑦ 记忆+持久化+沙盒 | Skill 持久化 | ⚠️ 缺沙盒 |
| ⑧ 评估+轨迹+CI | GitHub Actions + 171 tests | ⚠️ 缺轨迹检查 |
| ⑨ 权限+人工检查 | — | ❌ 未实现 |

---

> **一句话总结**：这篇文章的结构和我们已有的工作流高度互补——我们已有①~⑥，缺⑦的沙盒、⑧的轨迹检查、⑨的权限文件。是后续可选优化方向。
