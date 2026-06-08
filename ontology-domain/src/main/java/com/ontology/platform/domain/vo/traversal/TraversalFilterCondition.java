package com.ontology.platform.domain.vo.traversal;

import com.ontology.platform.common.enums.FilterOperator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 图遍历过滤条件值对象
 * Traversal Filter Condition Value Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TraversalFilterCondition {
    
    /**
     * 属性字段名（必须通过白名单验证）
     */
    private String field;
    
    /**
     * 操作符（枚举限制）
     */
    private FilterOperator operator;
    
    /**
     * 比较值（类型校验）
     */
    private Object value;
    
    /**
     * 参数名称（用于参数化查询）
     */
    private String paramName;
    
    /**
     * 验证条件是否有效
     */
    public boolean isValid() {
        return field != null && !field.isBlank() && operator != null;
    }
    
    /**
     * 判断是否为需要值的操作符
     */
    public boolean requiresValue() {
        return operator != FilterOperator.isNull && operator != FilterOperator.isNotNull;
    }
    
    /**
     * 判断是否为数组操作符
     */
    public boolean isArrayOperator() {
        return operator == FilterOperator.in || operator == FilterOperator.notIn;
    }
    
    /**
     * 获取值列表（用于in/notIn操作符）
     */
    @SuppressWarnings("unchecked")
    public List<Object> getValueList() {
        if (!isArrayOperator()) {
            return List.of(value);
        }
        if (value instanceof List) {
            return (List<Object>) value;
        }
        return List.of(value);
    }
}
