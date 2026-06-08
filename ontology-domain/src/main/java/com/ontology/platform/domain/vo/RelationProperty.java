package com.ontology.platform.domain.vo;

import com.ontology.platform.common.enums.PropertyDataType;
import lombok.*;

/**
 * 关系属性值对象
 * Relation Property Value Object
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelationProperty {

    private String name;
    private String displayName;
    private PropertyDataType dataType;
    private boolean isRequired;
    private Object defaultValue;

    /**
     * 创建关系属性
     */
    public static RelationProperty create(
            String name,
            String displayName,
            PropertyDataType dataType,
            boolean isRequired) {
        return RelationProperty.builder()
                .name(name)
                .displayName(displayName)
                .dataType(dataType)
                .isRequired(isRequired)
                .build();
    }

    /**
     * 验证值
     */
    public boolean validateValue(Object value) {
        if (value == null) {
            return !isRequired;
        }

        return switch (dataType) {
            case STRING, TEXT -> value instanceof String;
            case INTEGER -> value instanceof Integer || value instanceof Long;
            case DECIMAL -> value instanceof Float || value instanceof Double;
            case BOOLEAN -> value instanceof Boolean;
            default -> true;
        };
    }
}
