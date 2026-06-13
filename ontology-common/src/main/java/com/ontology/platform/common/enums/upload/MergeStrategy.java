package com.ontology.platform.common.enums.upload;

public enum MergeStrategy {
    INSERT,
    UPSERT,
    REPLACE;

    public static MergeStrategy fromCode(String code) {
        if (code == null || code.isBlank()) {
            return INSERT;
        }
        return valueOf(code.trim().toUpperCase());
    }
}
