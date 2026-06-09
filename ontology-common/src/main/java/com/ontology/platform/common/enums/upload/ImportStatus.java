package com.ontology.platform.common.enums.upload;

public enum ImportStatus {
    PENDING("pending"),
    PARSING("parsing"),
    VALIDATING("validating"),
    IMPORTING("importing"),
    COMPLETED("completed"),
    FAILED("failed"),
    CANCELLED("cancelled");

    private final String code;

    ImportStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
