package com.ontology.platform.common.enums;

/**
 * 属性数据类型枚举
 */
public enum PropertyDataType {

    STRING("STRING", "字符串"),
    TEXT("TEXT", "长文本"),
    INTEGER("INTEGER", "整数"),
    DECIMAL("DECIMAL", "浮点数"),
    BOOLEAN("BOOLEAN", "布尔值"),
    DATE("DATE", "日期"),
    DATETIME("DATETIME", "日期时间"),
    UUID("UUID", "UUID"),
    ENUM("ENUM", "枚举"),
    ARRAY("ARRAY", "数组"),
    OBJECT("OBJECT", "对象"),
    JSON("JSON", "JSON");

    private final String value;
    private final String description;

    PropertyDataType(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static PropertyDataType fromValue(String value) {
        for (PropertyDataType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown property data type: " + value);
    }
}
