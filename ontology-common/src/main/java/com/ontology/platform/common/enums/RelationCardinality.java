package com.ontology.platform.common.enums;

/**
 * 关系基数枚举
 */
public enum RelationCardinality {

    ONE_TO_ONE("1:1", "一对一"),
    ONE_TO_MANY("1:N", "一对多"),
    MANY_TO_ONE("N:1", "多对一"),
    MANY_TO_MANY("M:N", "多对多");

    private final String value;
    private final String description;

    RelationCardinality(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static RelationCardinality fromValue(String value) {
        for (RelationCardinality cardinality : values()) {
            if (cardinality.value.equalsIgnoreCase(value)) {
                return cardinality;
            }
        }
        throw new IllegalArgumentException("Unknown relation cardinality: " + value);
    }
}
