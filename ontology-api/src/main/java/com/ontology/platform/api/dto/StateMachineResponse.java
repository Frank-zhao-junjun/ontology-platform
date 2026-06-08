package com.ontology.platform.api.dto;

import com.ontology.platform.domain.entity.StateMachine;

import java.util.LinkedHashMap;
import java.util.Map;

public class StateMachineResponse {
    private StateMachineResponse() {}

    public static Map<String, Object> toMap(StateMachine sm) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", sm.getId());
        m.put("contextId", sm.getContextId());
        m.put("name", sm.getName());
        m.put("nameEn", sm.getNameEn());
        m.put("objectTypeId", sm.getObjectTypeId());
        m.put("statusField", sm.getStatusField());
        m.put("statesJson", sm.getStatesJson());
        m.put("transitionsJson", sm.getTransitionsJson());
        m.put("createdAt", sm.getCreatedAt() != null ? sm.getCreatedAt().toString() : null);
        m.put("updatedAt", sm.getUpdatedAt() != null ? sm.getUpdatedAt().toString() : null);
        return m;
    }
}
