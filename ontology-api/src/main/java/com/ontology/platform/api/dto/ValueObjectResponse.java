package com.ontology.platform.api.dto;

import com.ontology.platform.domain.entity.ValueObject;

import java.util.LinkedHashMap;
import java.util.Map;

public class ValueObjectResponse {
    private ValueObjectResponse() {}

    public static Map<String, Object> toMap(ValueObject vo) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", vo.getId());
        m.put("name", vo.getName());
        m.put("code", vo.getCode());
        m.put("nameEn", vo.getNameEn());
        m.put("description", vo.getDescription());
        m.put("propertiesJson", vo.getPropertiesJson());
        m.put("createdAt", vo.getCreatedAt() != null ? vo.getCreatedAt().toString() : null);
        m.put("updatedAt", vo.getUpdatedAt() != null ? vo.getUpdatedAt().toString() : null);
        return m;
    }
}
