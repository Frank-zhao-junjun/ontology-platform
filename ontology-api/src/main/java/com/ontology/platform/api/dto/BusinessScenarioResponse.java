package com.ontology.platform.api.dto;

import com.ontology.platform.domain.entity.BusinessScenario;

import java.util.LinkedHashMap;
import java.util.Map;

public class BusinessScenarioResponse {
    private BusinessScenarioResponse() {}

    public static Map<String, Object> toMap(BusinessScenario s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", s.getId());
        m.put("contextId", s.getContextId());
        m.put("name", s.getName());
        m.put("code", s.getCode());
        m.put("nameEn", s.getNameEn());
        m.put("description", s.getDescription());
        m.put("applicableObjectTypeIdsJson", s.getApplicableObjectTypeIdsJson());
        m.put("createdAt", s.getCreatedAt() != null ? s.getCreatedAt().toString() : null);
        return m;
    }
}
