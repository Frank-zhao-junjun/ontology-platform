package com.ontology.platform.common.enums;
public enum DomainTag {
    MANUFACTURING("manufacturing"), QUALITY("quality"), EQUIPMENT("equipment"), SUPPLY_CHAIN("supply_chain");
    private final String code;
    DomainTag(String c) { this.code = c; }
    public String getCode() { return code; }
    public static DomainTag fromCode(String code) { for (DomainTag t : values()) if (t.code.equals(code)) return t; throw new IllegalArgumentException("Unknown: " + code); }
}
