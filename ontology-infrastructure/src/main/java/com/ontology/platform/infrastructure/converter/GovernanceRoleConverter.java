package com.ontology.platform.infrastructure.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.domain.entity.GovernanceRole;
import com.ontology.platform.infrastructure.persistence.GovernanceRolePO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GovernanceRoleConverter {
    private final ObjectMapper objectMapper;

    public GovernanceRole toDomain(GovernanceRolePO po) {
        if (po == null) return null;
        return GovernanceRole.builder()
                .id(po.getId()).ontologyId(po.getOntologyId())
                .name(po.getName()).description(po.getDescription())
                .permissions(parsePermissions(po.getPermissions()))
                .createdAt(po.getCreatedAt()).updatedAt(po.getUpdatedAt())
                .build();
    }

    public GovernanceRolePO toPO(GovernanceRole entity) {
        return GovernanceRolePO.builder()
                .id(entity.getId()).ontologyId(entity.getOntologyId())
                .name(entity.getName()).description(entity.getDescription())
                .permissions(toPermissionsJson(entity.getPermissions()))
                .createdAt(entity.getCreatedAt()).updatedAt(entity.getUpdatedAt())
                .build();
    }

    @SuppressWarnings("unchecked")
    private List<GovernanceRole.RolePermission> parsePermissions(String json) {
        if (json == null || json.isBlank()) return new ArrayList<>();
        try {
            List<Map<String, Object>> raw = objectMapper.readValue(json, new TypeReference<>() {});
            return raw.stream().map(m -> GovernanceRole.RolePermission.builder()
                    .objectTypeId((String) m.get("objectTypeId"))
                    .ops((List<String>) m.getOrDefault("ops", List.of()))
                    .build()).toList();
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }

    private String toPermissionsJson(List<GovernanceRole.RolePermission> perms) {
        try {
            return objectMapper.writeValueAsString(perms);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }
}
