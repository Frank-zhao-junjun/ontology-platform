# TDD Filter参数SQL注入风险修复说明

## 修复概述

**修复日期**：2026年  
**安全等级**：🔴 红线（安全红线问题）  
**修复状态**：✅ 已完成

## 问题定位

在就绪评审中发现以下API设计中存在SQL注入风险：

1. **4.8.1 查询对象列表** - filter参数使用自由文本格式
2. **4.8.4 聚合查询** - filter参数同样使用自由文本格式

### 原危险设计

```json
// 4.8.1 原filter格式（危险）
{
  "filter": "field1=value1 AND field2>100 OR field3 LIKE '%关键词%'"
}

// 4.8.4 原filter格式（危险）
{
  "filter": "created_at >= '2026-01-01'"
}
```

上述自由文本格式存在严重SQL注入风险，攻击者可构造恶意输入如：
- `" OR 1=1 --`
- `" UNION SELECT * FROM users --`

---

## 修复方案

### 方案选择：结构化参数格式（推荐）

将所有filter参数从自由文本格式升级为结构化JSON格式，彻底消除SQL注入风险。

### 修复内容

#### 1. 4.8.1 查询对象列表（新增结构化filter）

**修改前**：
```json
{
  "filter": "field1=value1 AND field2>100"
}
```

**修改后**：
```json
{
  "filter": {
    "conditions": [
      { "field": "status", "operator": "in", "value": ["pending", "approved"] },
      { "field": "amount", "operator": "gte", "value": 10000 }
    ],
    "logic": "AND"
  }
}
```

**新增内容**：
- `ObjectFilter` 接口定义
- `FilterCondition` 类型定义
- `FilterOperator` 枚举（14种操作符）
- 结构化请求示例
- 废弃自由文本格式的警告说明

#### 2. 4.8.4 聚合查询（新增结构化filter）

**修改前**：
```json
{
  "filter": "created_at >= '2026-01-01'"
}
```

**修改后**：
```json
{
  "filter": {
    "conditions": [
      { "field": "created_at", "operator": "gte", "value": "2026-01-01T00:00:00Z" },
      { "field": "status", "operator": "ne", "value": "cancelled" }
    ],
    "logic": "AND"
  }
}
```

#### 3. 7.5.4 Filter参数安全校验（新增章节）

新增专门章节，包含完整的filter安全校验实现：

```typescript
class FilterSecurityValidator {
  // ✅ 操作符枚举限制
  private readonly ALLOWED_OPERATORS = new Set([
    'eq', 'ne', 'gt', 'gte', 'lt', 'lte',
    'in', 'notIn', 'contains', 'startsWith', 'endsWith',
    'isNull', 'isNotNull', 'between'
  ]);
  
  // 值长度限制
  private readonly MAX_VALUE_LENGTH = 1000;
  
  // 最大条件数限制
  private readonly MAX_CONDITIONS = 20;
  
  // 字段名格式校验
  // 危险字符检测
  // 类型校验
}
```

**安全措施**：
- [x] 字段白名单校验
- [x] 操作符枚举限制
- [x] 值参数化传递
- [x] 特殊字符检测
- [x] 查询深度限制
- [x] 结果数量限制
- [x] 条件数量限制
- [x] 值长度限制

#### 4. 7.6.1.1 Filter参数化处理（新增章节）

新增参数化处理器，将结构化filter转换为安全查询：

```typescript
class FilterParameterizedBuilder {
  buildWhereClause(filter, params) {
    // 生成参数化WHERE子句
    // 所有用户输入通过params传递
    // 绝对不直接拼接到SQL
  }
}
```

#### 5. 7.5.6 安全事件监控（新增filter监控规则）

```yaml
filter_security:
  patterns:
    - name: "filter_injection_attempt"
      description: "Filter注入攻击尝试"
      condition: "filter_validation_failed = true"
      severity: high
      action: "block_and_log"
      
    - name: "filter_complexity_attack"
      description: "Filter复杂度攻击"
      condition: "filter_conditions > 20"
      severity: medium
      action: "reject"
      
    - name: "invalid_operator"
      description: "非法操作符"
      severity: high
      action: "block_and_log"
      
    - name: "field_whitelist_bypass"
      description: "字段白名单绕过尝试"
      severity: high
      action: "block_and_log"
      
    - name: "value_length_overflow"
      description: "Filter值长度超限"
      severity: medium
      action: "reject"
```

---

## 安全措施清单

| 安全措施 | 实现位置 | 状态 |
|---------|---------|------|
| 字段白名单校验 | 7.5.2 WhitelistService, 7.5.4 FilterSecurityValidator | ✅ |
| 操作符枚举限制 | 7.5.4 ALLOWED_OPERATORS | ✅ |
| 值参数化传递 | 7.6.1.1 FilterParameterizedBuilder | ✅ |
| 特殊字符检测 | 7.5.4 validateScalarValue | ✅ |
| 查询深度限制 | 4.8.3 maxDepth=5 | ✅ |
| 结果数量限制 | 4.8.3 limit=1000, 4.8.1 limit=100 | ✅ |
| 条件数量限制 | 7.5.4 MAX_CONDITIONS=20 | ✅ |
| 值长度限制 | 7.5.4 MAX_VALUE_LENGTH=1000 | ✅ |
| 安全事件监控 | 7.5.6 filter_security | ✅ |

---

## 修改文件清单

| 文件路径 | 修改内容 |
|---------|---------|
| `./产品规划/本体建模平台TDD.md` | 完整修复 |

### 章节变更汇总

| 章节 | 操作 | 说明 |
|-----|------|------|
| 4.8.1 查询对象列表 | 修改 | filter参数升级为结构化格式 |
| 4.8.4 聚合查询 | 修改 | filter参数升级为结构化格式 |
| 7.5.4 Filter参数安全校验 | 新增 | 新增完整安全校验实现 |
| 7.5.5 查询执行安全 | 编号调整 | 原7.5.4 |
| 7.5.6 安全事件监控 | 增强 | 新增filter监控规则 |
| 7.6.1.1 Filter参数化处理 | 新增 | 新增参数化处理器 |

---

## 技术债务消除

本次修复消除了以下技术债务：

1. ~~自由文本filter格式~~
2. ~~直接字符串拼接SQL~~
3. ~~无操作符验证~~
4. ~~无值长度限制~~
5. ~~无filter安全监控~~

---

## 下一步建议

1. **实现FilterSecurityValidator**：开发团队需按7.5.4节实现完整校验
2. **实现FilterParameterizedBuilder**：按7.6.1.1节实现参数化处理
3. **配置白名单数据**：为每个本体配置允许的字段白名单
4. **测试覆盖**：补充filter安全相关的测试用例
5. **监控告警**：部署filter_security监控规则
