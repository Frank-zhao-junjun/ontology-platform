package com.ontology.platform.common.enums;

public enum InvocationMode {
    ACTIVE, EVENT_DRIVEN, BOTH;

    public static InvocationMode fromManifest(String mcpToolName) {
        return mcpToolName != null && !mcpToolName.isBlank() ? BOTH : BOTH;
    }

    public static InvocationMode fromCode(String code) {
        if (code == null) {
            return BOTH;
        }
        return InvocationMode.valueOf(code.toUpperCase());
    }
}
