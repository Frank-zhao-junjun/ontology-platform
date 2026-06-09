package com.ontology.platform.domain.vo;

import com.ontology.platform.common.enums.PropertyDataType;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 属性值对象
 * Property Value Object
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Property {

    private String id;
    private String objectTypeId;
    private String name;
    private String displayName;
    private String description;
    private PropertyDataType dataType;
    private boolean isComputed;
    private boolean isRequired;
    private boolean isUnique;
    private boolean isSearchable;
    private boolean isSortable;
    private Object defaultValue;
    @Builder.Default
    private List<PropertyConstraint> constraints = new ArrayList<>();
    @Builder.Default
    private List<Property> nestedProperties = new ArrayList<>();
    private int sortOrder;
    private Instant createdAt;
    private Instant updatedAt;

    /**
     * 创建新的属性
     */
    public static Property create(
            String objectTypeId,
            String name,
            String displayName,
            String description,
            PropertyDataType dataType,
            boolean isRequired) {
        return Property.builder()
                .id(UUID.randomUUID().toString())
                .objectTypeId(objectTypeId)
                .name(name)
                .displayName(displayName)
                .description(description)
                .dataType(dataType)
                .isRequired(isRequired)
                .isUnique(false)
                .isSearchable(true)
                .isSortable(true)
                .isComputed(false)
                .sortOrder(0)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    /**
     * 验证值是否符合数据类型
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
            case DATE, DATETIME -> value instanceof String || value instanceof Instant;
            case UUID -> value instanceof String;
            case ENUM, ARRAY, OBJECT, JSON -> true;
        };
    }

    /**
     * 更新属性信息
     */
    public Property update(String displayName, String description, boolean isRequired) {
        this.displayName = displayName;
        this.description = description;
        this.isRequired = isRequired;
        this.updatedAt = Instant.now();
        return this;
    }

    /**
     * 更新排序顺序
     */
    public Property updateSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
        this.updatedAt = Instant.now();
        return this;
    }

    public Property addConstraint(PropertyConstraint constraint) {
        this.constraints.add(constraint);
        this.updatedAt = Instant.now();
        return this;
    }
}
