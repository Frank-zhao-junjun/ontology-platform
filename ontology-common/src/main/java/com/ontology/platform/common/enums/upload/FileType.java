package com.ontology.platform.common.enums.upload;

public enum FileType {
    CSV("csv"),
    XLSX("xlsx"),
    JSON("json");

    private final String code;

    FileType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static FileType fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (FileType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return null;
    }
}
