package com.ontology.platform.common.enums;

/**
 * 本体状态枚举
 */
public enum OntologyStatus {

    DRAFT("draft", "草稿"),
    PUBLISHED("published", "已发布"),
    ARCHIVED("archived", "已归档");

    private final String value;
    private final String description;

    OntologyStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static OntologyStatus fromValue(String value) {
        for (OntologyStatus status : values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown ontology status: " + value);
    }
}
