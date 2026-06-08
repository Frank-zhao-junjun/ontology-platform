package com.ontology.platform.domain.vo;

import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * 属性约束值对象
 * Property Constraint Value Object
 * 定义属性的各种约束条件
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyConstraint {

    /**
     * 约束类型枚举
     */
    public enum ConstraintType {
        // 范围约束
        MIN_VALUE("minValue", "最小值约束"),
        MAX_VALUE("maxValue", "最大值约束"),
        MIN_LENGTH("minLength", "最小长度约束"),
        MAX_LENGTH("maxLength", "最大长度约束"),
        
        // 格式约束
        PATTERN("pattern", "正则表达式约束"),
        
        // 枚举约束
        ENUM_VALUES("enumValues", "枚举值约束"),
        
        // 自定义约束
        CUSTOM("custom", "自定义约束");

        private final String value;
        private final String description;

        ConstraintType(String value, String description) {
            this.value = value;
            this.description = description;
        }
    }

    private String id;
    private ConstraintType type;
    private Object value;
    private String errorMessage;

    /**
     * 创建最小值约束
     */
    public static PropertyConstraint minValue(BigDecimal minValue, String errorMessage) {
        return PropertyConstraint.builder()
                .id(java.util.UUID.randomUUID().toString())
                .type(ConstraintType.MIN_VALUE)
                .value(minValue)
                .errorMessage(errorMessage != null ? errorMessage : "值不能小于" + minValue)
                .build();
    }

    /**
     * 创建最大值约束
     */
    public static PropertyConstraint maxValue(BigDecimal maxValue, String errorMessage) {
        return PropertyConstraint.builder()
                .id(java.util.UUID.randomUUID().toString())
                .type(ConstraintType.MAX_VALUE)
                .value(maxValue)
                .errorMessage(errorMessage != null ? errorMessage : "值不能大于" + maxValue)
                .build();
    }

    /**
     * 创建最小长度约束
     */
    public static PropertyConstraint minLength(int minLength, String errorMessage) {
        return PropertyConstraint.builder()
                .id(java.util.UUID.randomUUID().toString())
                .type(ConstraintType.MIN_LENGTH)
                .value(minLength)
                .errorMessage(errorMessage != null ? errorMessage : "长度不能小于" + minLength)
                .build();
    }

    /**
     * 创建最大长度约束
     */
    public static PropertyConstraint maxLength(int maxLength, String errorMessage) {
        return PropertyConstraint.builder()
                .id(java.util.UUID.randomUUID().toString())
                .type(ConstraintType.MAX_LENGTH)
                .value(maxLength)
                .errorMessage(errorMessage != null ? errorMessage : "长度不能大于" + maxLength)
                .build();
    }

    /**
     * 创建正则表达式约束
     */
    public static PropertyConstraint pattern(String regex, String errorMessage) {
        return PropertyConstraint.builder()
                .id(java.util.UUID.randomUUID().toString())
                .type(ConstraintType.PATTERN)
                .value(regex)
                .errorMessage(errorMessage != null ? errorMessage : "值不符合正则表达式: " + regex)
                .build();
    }

    /**
     * 创建枚举值约束
     */
    public static PropertyConstraint enumValues(List<String> enumValues, String errorMessage) {
        return PropertyConstraint.builder()
                .id(java.util.UUID.randomUUID().toString())
                .type(ConstraintType.ENUM_VALUES)
                .value(enumValues)
                .errorMessage(errorMessage != null ? errorMessage : "值必须在允许的枚举值中: " + enumValues)
                .build();
    }

    /**
     * 创建自定义约束
     */
    public static PropertyConstraint custom(String expression, String errorMessage) {
        return PropertyConstraint.builder()
                .id(java.util.UUID.randomUUID().toString())
                .type(ConstraintType.CUSTOM)
                .value(expression)
                .errorMessage(errorMessage != null ? errorMessage : "值不符合自定义规则")
                .build();
    }

    /**
     * 验证值是否符合约束
     */
    public boolean validate(Object value) {
        if (value == null) {
            return true; // null值由isRequired控制
        }

        return switch (type) {
            case MIN_VALUE -> validateMinValue(value);
            case MAX_VALUE -> validateMaxValue(value);
            case MIN_LENGTH -> validateMinLength(value);
            case MAX_LENGTH -> validateMaxLength(value);
            case PATTERN -> validatePattern(value);
            case ENUM_VALUES -> validateEnumValues(value);
            case CUSTOM -> validateCustom(value);
        };
    }

    /**
     * 验证最小值约束
     */
    private boolean validateMinValue(Object value) {
        if (value == null) return true;
        
        BigDecimal minValue = (BigDecimal) this.value;
        BigDecimal actualValue;
        
        if (value instanceof BigDecimal) {
            actualValue = (BigDecimal) value;
        } else if (value instanceof Number) {
            actualValue = new BigDecimal(value.toString());
        } else {
            return false; // 非数值类型无法比较
        }
        
        return actualValue.compareTo(minValue) >= 0;
    }

    /**
     * 验证最大值约束
     */
    private boolean validateMaxValue(Object value) {
        if (value == null) return true;
        
        BigDecimal maxValue = (BigDecimal) this.value;
        BigDecimal actualValue;
        
        if (value instanceof BigDecimal) {
            actualValue = (BigDecimal) value;
        } else if (value instanceof Number) {
            actualValue = new BigDecimal(value.toString());
        } else {
            return false;
        }
        
        return actualValue.compareTo(maxValue) <= 0;
    }

    /**
     * 验证最小长度约束
     */
    private boolean validateMinLength(Object value) {
        int minLength = (Integer) this.value;
        int actualLength;
        
        if (value instanceof String) {
            actualLength = ((String) value).length();
        } else if (value instanceof List) {
            actualLength = ((List<?>) value).size();
        } else if (value instanceof Number) {
            actualLength = value.toString().length();
        } else {
            return false;
        }
        
        return actualLength >= minLength;
    }

    /**
     * 验证最大长度约束
     */
    private boolean validateMaxLength(Object value) {
        int maxLength = (Integer) this.value;
        int actualLength;
        
        if (value instanceof String) {
            actualLength = ((String) value).length();
        } else if (value instanceof List) {
            actualLength = ((List<?>) value).size();
        } else if (value instanceof Number) {
            actualLength = value.toString().length();
        } else {
            return false;
        }
        
        return actualLength <= maxLength;
    }

    /**
     * 验证正则表达式约束
     */
    private boolean validatePattern(Object value) {
        if (!(value instanceof String)) {
            return false;
        }
        
        String pattern = (String) this.value;
        try {
            return Pattern.matches(pattern, (String) value);
        } catch (PatternSyntaxException e) {
            return false;
        }
    }

    /**
     * 验证枚举值约束
     */
    @SuppressWarnings("unchecked")
    private boolean validateEnumValues(Object value) {
        List<String> enumValues = (List<String>) this.value;
        return enumValues.contains(value.toString());
    }

    /**
     * 验证自定义约束（预留扩展点）
     */
    private boolean validateCustom(Object value) {
        // 自定义约束需要通过规则引擎实现
        // 当前版本返回true，后续扩展
        return true;
    }

    /**
     * 获取错误消息
     */
    public String getErrorMessage(Object value) {
        return errorMessage;
    }
}
