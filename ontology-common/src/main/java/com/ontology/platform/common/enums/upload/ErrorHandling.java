package com.ontology.platform.common.enums.upload;

public enum ErrorHandling {
    SKIP,
    STOP;

    public static ErrorHandling fromCode(String code) {
        if (code == null || code.isBlank()) {
            return SKIP;
        }
        return valueOf(code.trim().toUpperCase());
    }
}
