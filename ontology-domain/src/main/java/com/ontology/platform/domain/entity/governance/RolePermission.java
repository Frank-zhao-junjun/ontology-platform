package com.ontology.platform.domain.entity.governance;

import lombok.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class RolePermission {
    private String id;
    private String roleId;
    private String resource;
    private List<String> operations;
    private String domain;
    private Instant createdAt;

    public static RolePermission create(String roleId, String resource, List<String> operations, String domain) {
        return RolePermission.builder()
                .id(UUID.randomUUID().toString())
                .roleId(roleId)
                .resource(resource)
                .operations(operations)
                .domain(domain)
                .createdAt(Instant.now())
                .build();
    }

    public boolean allows(String operation) {
        return operations.contains(operation) || operations.contains("*");
    }
}
