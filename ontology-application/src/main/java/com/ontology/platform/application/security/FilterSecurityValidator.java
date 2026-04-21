package com.ontology.platform.application.security;

import com.ontology.platform.common.enums.ErrorCode;
import com.ontology.platform.common.enums.FilterOperator;
import com.ontology.platform.common.exception.ValidationException;
import com.ontology.platform.domain.vo.traversal.TraversalFilter;
import com.ontology.platform.domain.vo.traversal.TraversalFilterCondition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Filter参数安全校验器
 * Filter Security Validator
 * 
 * 参考TDD 7.5.4设计，实现完整的Filter参数安全校验
 * 
 * 安全措施：
 * 1. 字段白名单校验
 * 2. 操作符枚举限制
 * 3. 值参数化传递
 * 4. 长度限制
 * 5. 条件数量限制
 */
@Slf4j
@Component
public class FilterSecurityValidator {
    
    // ==================== 安全常量 ====================
    
    /** 允许的操作符（枚举限制） */
    private static final Set<FilterOperator> ALLOWED_OPERATORS = Set.of(
        FilterOperator.eq,
        FilterOperator.ne,
        FilterOperator.gt,
        FilterOperator.gte,
        FilterOperator.lt,
        FilterOperator.lte,
        FilterOperator.in,
        FilterOperator.notIn,
        FilterOperator.contains,
        FilterOperator.startsWith,
        FilterOperator.endsWith,
        FilterOperator.isNull,
        FilterOperator.isNotNull,
        FilterOperator.between
    );
    
    /** 允许的逻辑运算符 */
    private static final Set<String> ALLOWED_LOGIC = Set.of("AND", "OR");
    
    /** 值最大长度限制（防止缓冲区溢出） */
    private static final int MAX_VALUE_LENGTH = 1000;
    
    /** 最大条件数限制 */
    private static final int MAX_CONDITIONS = 20;
    
    /** 数组最大长度限制 */
    private static final int MAX_ARRAY_LENGTH = 100;
    
    /** 字段名格式正则（只允许字母、数字、下划线） */
    private static final Pattern FIELD_NAME_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");
    
    /** 字段名最大长度 */
    private static final int MAX_FIELD_NAME_LENGTH = 100;
    
    // ==================== 公开方法 ====================
    
    /**
     * 验证Filter参数结构
     * @param filters 过滤器列表
     * @return 验证后的过滤器列表
     */
    public List<TraversalFilter> validateFilters(List<TraversalFilter> filters) {
        if (filters == null || filters.isEmpty()) {
            return List.of();
        }
        
        List<TraversalFilter> validated = new ArrayList<>();
        for (TraversalFilter filter : filters) {
            validated.add(validateFilter(filter));
        }
        return validated;
    }
    
    /**
     * 验证单个Filter
     * @param filter 过滤器
     * @return 验证后的过滤器
     */
    public TraversalFilter validateFilter(TraversalFilter filter) {
        if (filter == null) {
            return TraversalFilter.builder()
                    .depth(0)
                    .conditions(List.of())
                    .logic("AND")
                    .build();
        }
        
        // 1. 验证逻辑运算符
        validateLogic(filter.getLogic());
        
        // 2. 验证深度
        int depth = Math.max(0, filter.getDepth());
        
        // 3. 验证条件数量
        List<TraversalFilterCondition> conditions = filter.getConditions();
        if (conditions == null || conditions.isEmpty()) {
            return TraversalFilter.builder()
                    .depth(depth)
                    .targetType(filter.getTargetType())
                    .conditions(List.of())
                    .logic(filter.getLogic() != null ? filter.getLogic() : "AND")
                    .build();
        }
        
        if (conditions.size() > MAX_CONDITIONS) {
            throw new ValidationException(
                ErrorCode.INVALID_FILTER,
                String.format("Too many filter conditions: %d. Max: %d", 
                    conditions.size(), MAX_CONDITIONS)
            );
        }
        
        // 4. 验证并转换每个条件
        List<TraversalFilterCondition> validatedConditions = new ArrayList<>();
        for (int i = 0; i < conditions.size(); i++) {
            validatedConditions.add(validateCondition(conditions.get(i), i));
        }
        
        return TraversalFilter.builder()
                .depth(depth)
                .targetType(filter.getTargetType())
                .conditions(validatedConditions)
                .logic(filter.getLogic() != null ? filter.getLogic() : "AND")
                .build();
    }
    
    // ==================== 私有方法 ====================
    
    /**
     * 验证逻辑运算符
     */
    private void validateLogic(String logic) {
        if (logic == null) {
            return;
        }
        
        if (!ALLOWED_LOGIC.contains(logic.toUpperCase())) {
            throw new ValidationException(
                ErrorCode.INVALID_FILTER,
                String.format("Invalid filter logic: %s. Must be AND or OR", logic)
            );
        }
    }
    
    /**
     * 验证单个过滤条件
     */
    private TraversalFilterCondition validateCondition(TraversalFilterCondition condition, int index) {
        // 1. 验证字段名存在
        if (condition.getField() == null || condition.getField().isBlank()) {
            throw new ValidationException(
                ErrorCode.INVALID_FIELD_NAME,
                String.format("Condition %d: field name is required", index)
            );
        }
        
        // 2. 验证字段名格式（防止注入）
        String fieldName = condition.getField();
        if (fieldName.length() > MAX_FIELD_NAME_LENGTH) {
            throw new ValidationException(
                ErrorCode.INVALID_FIELD_NAME,
                String.format("Condition %d: field name too long, max %d characters", 
                    index, MAX_FIELD_NAME_LENGTH)
            );
        }
        
        if (!FIELD_NAME_PATTERN.matcher(fieldName).matches()) {
            throw new ValidationException(
                ErrorCode.INVALID_FIELD_NAME,
                String.format("Condition %d: invalid field name format: %s. " +
                    "Only letters, numbers and underscores allowed", index, fieldName)
            );
        }
        
        // 3. 验证操作符存在
        FilterOperator operator = condition.getOperator();
        if (operator == null) {
            throw new ValidationException(
                ErrorCode.INVALID_OPERATOR,
                String.format("Condition %d: operator is required", index)
            );
        }
        
        // 4. 验证操作符在白名单中
        if (!ALLOWED_OPERATORS.contains(operator)) {
            throw new ValidationException(
                ErrorCode.INVALID_OPERATOR,
                String.format("Condition %d: invalid operator: %s", index, operator)
            );
        }
        
        // 5. 验证值
        Object validatedValue = validateValue(operator, condition.getValue(), index);
        
        // 6. 生成参数名
        String paramName = "p_cond_" + index;
        
        return TraversalFilterCondition.builder()
                .field(fieldName)
                .operator(operator)
                .value(validatedValue)
                .paramName(paramName)
                .build();
    }
    
    /**
     * 验证Filter值
     */
    private Object validateValue(FilterOperator operator, Object value, int index) {
        // isNull/isNotNull不需要值
        if (operator == FilterOperator.isNull || operator == FilterOperator.isNotNull) {
            return null;
        }
        
        // 其他操作符必须有值
        if (value == null) {
            throw new ValidationException(
                ErrorCode.INVALID_FILTER,
                String.format("Condition %d: value is required for operator %s", index, operator)
            );
        }
        
        // 数组值检查（in, notIn）
        if (operator == FilterOperator.in || operator == FilterOperator.notIn) {
            return validateArrayValue(value, index);
        }
        
        // between需要两个值
        if (operator == FilterOperator.between) {
            return validateBetweenValue(value, index);
        }
        
        // 字符串值长度检查
        if (value instanceof String) {
            String strValue = (String) value;
            if (strValue.length() > MAX_VALUE_LENGTH) {
                throw new ValidationException(
                    ErrorCode.INVALID_FILTER,
                    String.format("Condition %d: value too long, max %d characters", 
                        index, MAX_VALUE_LENGTH)
                );
            }
            
            // 防止LDAP注入和路径遍历
            if (containsDangerousChars(strValue)) {
                throw new ValidationException(
                    ErrorCode.INVALID_FILTER,
                    String.format("Condition %d: value contains dangerous characters", index)
                );
            }
            
            return strValue;
        }
        
        // 数字直接返回
        if (value instanceof Number) {
            return value;
        }
        
        // 布尔值
        if (value instanceof Boolean) {
            return value;
        }
        
        // 不支持的类型
        throw new ValidationException(
            ErrorCode.INVALID_FILTER,
            String.format("Condition %d: unsupported value type: %s", index, value.getClass().getName())
        );
    }
    
    /**
     * 验证数组值
     */
    private List<Object> validateArrayValue(Object value, int index) {
        if (!(value instanceof List)) {
            throw new ValidationException(
                ErrorCode.INVALID_FILTER,
                String.format("Condition %d: operator %s requires array value", 
                    index, value)
            );
        }
        
        List<?> list = (List<?>) value;
        if (list.size() > MAX_ARRAY_LENGTH) {
            throw new ValidationException(
                ErrorCode.INVALID_FILTER,
                String.format("Condition %d: array too large, max %d items", 
                    index, MAX_ARRAY_LENGTH)
            );
        }
        
        // 验证每个元素
        List<Object> validated = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Object item = list.get(i);
            if (item == null) {
                validated.add(null);
            } else if (item instanceof String) {
                String strItem = (String) item;
                if (strItem.length() > MAX_VALUE_LENGTH) {
                    throw new ValidationException(
                        ErrorCode.INVALID_FILTER,
                        String.format("Condition %d: array item too long, max %d characters", 
                            index, MAX_VALUE_LENGTH)
                    );
                }
                validated.add(strItem);
            } else {
                validated.add(item);
            }
        }
        
        return validated;
    }
    
    /**
     * 验证between值（需要两个值）
     */
    private List<Object> validateBetweenValue(Object value, int index) {
        if (!(value instanceof List)) {
            throw new ValidationException(
                ErrorCode.INVALID_FILTER,
                String.format("Condition %d: operator between requires array with 2 values", index)
            );
        }
        
        List<?> list = (List<?>) value;
        if (list.size() != 2) {
            throw new ValidationException(
                ErrorCode.INVALID_FILTER,
                String.format("Condition %d: operator between requires exactly 2 values", index)
            );
        }
        
        return new ArrayList<>(list);
    }
    
    /**
     * 检查危险字符（防止LDAP注入、路径遍历等）
     */
    private boolean containsDangerousChars(String value) {
        // 检查LDAP注入风险字符
        if (value.contains("*") && value.indexOf('*') == 0 && value.lastIndexOf('*') == value.length() - 1) {
            // 两端通配符需要额外校验
            return !isValidWildcard(value);
        }
        
        // 检查路径遍历
        return value.contains("..") 
            || value.contains("/..") 
            || value.contains("\\..")
            || value.contains("~");
    }
    
    /**
     * 验证通配符模式
     * 只允许%和_作为SQL LIKE通配符
     */
    private boolean isValidWildcard(String value) {
        // 移除首尾*后检查中间是否有危险字符
        String inner = value.substring(1, value.length() - 1);
        return !inner.contains("*") 
            && !inner.contains("..")
            && !inner.contains("(")
            && !inner.contains(")")
            && !inner.contains(";");
    }
    
    // ==================== 工具方法 ====================
    
    /**
     * 检查字段名是否符合白名单要求
     * @param fieldName 字段名
     * @param allowedFields 允许的字段集合
     * @return 是否在白名单中
     */
    public boolean isFieldInWhitelist(String fieldName, Set<String> allowedFields) {
        if (allowedFields == null || allowedFields.isEmpty()) {
            return true; // 没有白名单配置时允许
        }
        return allowedFields.contains(fieldName);
    }
}
