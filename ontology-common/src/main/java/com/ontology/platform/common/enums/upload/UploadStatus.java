package com.ontology.platform.common.enums.upload;

public enum UploadStatus {
    PENDING("pending"),
    UPLOADING("uploading"),
    COMPLETED("completed"),
    EXPIRED("expired"),
    CANCELLED("cancelled");

    private final String code;

    UploadStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
