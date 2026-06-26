# User Stories — 项目1→项目2 本体模型发布管道

## 背景

项目1（Ontology 设计台）支持设计本体模型、导出 JSON。项目2（ontology-platform Spring Boot + MCP Server）需要接收导出的模型，持久化到 DB，并让 Agent 通过 MCP Server 可见可用。

---

### US-01: 一键发布模型到平台

**As a** 本体设计者  
**I want** 在设计台编辑完模型后，点击按钮将当前模型发布到平台  
**So that** 不用手动导出 JSON 再复制粘贴到平台

**Acceptance Criteria:**
```gherkin
Scenario: 从设计台发布模型到平台
  Given 我在项目1的模型编辑页面
  And 当前模型已保存且校验通过
  When 我点击"发布到平台"按钮
  Then 系统将当前模型 JSON 发送到平台 import API
  And 展示导入结果（成功/失败统计）
  And 失败时展示具体错误列表
  And 成功后该模型在 MCP Server 中 Agent 可见

Scenario: 网络异常处理
  Given 平台不可达
  When 我点击"发布到平台"按钮
  Then 提示"无法连接到平台，请重试"
  And 按钮可再次点击

Scenario: 模型未保存
  Given 模型有未保存的修改
  When 我点击"发布到平台"按钮
  Then 提示"请先保存当前模型"
  And 自动跳转到保存流程
```

---

### US-02: 平台接收并持久化导入的模型

**As a** 平台用户  
**I want** 平台能接收项目1导出的模型 JSON 并写入数据库  
**So that** 数据在平台重启后不丢失，可供后续查询

**Acceptance Criteria:**
```gherkin
Scenario: 导入有效模型
  Given 平台运行中
  When 发送项目1导出的 JSON 到 import API
  Then 返回 200 + draftId
  And 数据写入 manifest_import 表
  And raw_content 包含完整的 JSON 内容
  And imported_counts 统计正确（entities/rules 等数量）

Scenario: 导入格式错误
  When 发送不合法 JSON
  Then 返回 400
  And 错误消息提示"JSON 解析失败"

Scenario: 导入已存在的模型
  Given 某模型已导入
  When 再次导入同一 externalId + version
  Then 返回 409 Conflict
  And 错误消息提示"该版本已存在"
```

---

### US-03: 已发布的模型在 MCP Server 中 Agent 可见

**As a** Agent 使用者  
**I want** 发布后的模型自动注册为 MCP tool（search_*/get_*）  
**So that** 可以直接用自然语言查询模型数据

**Acceptance Criteria:**
```gherkin
Scenario: 发布后自动注册
  Given 模型已发布到平台
  When 重启 MCP Server
  Then 已发布的模型自动注册为 search_*/get_* 工具
  And Agent 可调用工具查询实体数据

Scenario: 停用后不可见
  Given 模型处于 disabled 状态
  When 重启 MCP Server
  Then 模型不注册 tool
  And Agent 不可见
```

---

### US-04: 转换项目1格式 → 平台内部格式

**As a** 平台开发者  
**I want** 项目1的导出 JSON 格式能被平台正确解析  
**So that** 不需要平台端了解项目1的内部结构

**Acceptance Criteria:**
```gherkin
Scenario: 格式转换
  Given 项目1导出 JSON 包含 structural/behavioral/rules 等字段
  When 平台接收请求
  Then 将字段映射为平台内部格式并存储
  And 不失精度（所有字段保留在 raw_content 中）

Scenario: 字段缺失
  When 项目1导出缺少某些预期字段
  Then 缺失字段默认为空数组
  And 不影响其他字段的导入
```
