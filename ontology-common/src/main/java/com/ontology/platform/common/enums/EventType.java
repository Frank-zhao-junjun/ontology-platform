package com.ontology.platform.common.enums;

import lombok.Getter;

@Getter
public enum EventType {
    DOMAIN_EVENT("DOMAIN_EVENT", "领域事件"),
    INTEGRATION_EVENT("INTEGRATION_EVENT", "集成事件");

    private final String code;
    private final String label;

    EventType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static EventType fromCode(String code) {
        for (EventType t : values()) {
            if (t.code.equalsIgnoreCase(code)) return t;
        }
        return DOMAIN_EVENT;
    }
}
