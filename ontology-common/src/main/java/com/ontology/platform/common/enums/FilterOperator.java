package com.ontology.platform.common.enums;

/**
 * 图遍历过滤操作符枚举
 * Graph Filter Operator - 白名单限制，禁止自由文本输入
 */
public enum FilterOperator {
    eq,         // 等于
    ne,         // 不等于
    gt,         // 大于
    gte,        // 大于等于
    lt,         // 小于
    lte,        // 小于等于
    in,         // 包含于数组
    notIn,      // 不包含于数组
    contains,   // 字符串包含
    startsWith, // 字符串前缀
    endsWith,   // 字符串后缀
    isNull,     // 为空
    isNotNull,  // 不为空
    between     // 范围（需两个值）
}
