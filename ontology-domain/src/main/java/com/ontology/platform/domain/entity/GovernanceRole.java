package com.ontology.platform.domain.entity;

import lombok.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class GovernanceRole {
    private String id;
    private String ontologyId;
    private String name;
    private String description;
    @Builder.Default
    private List<RolePermission> permissions = new ArrayList<>();
    private Instant createdAt;
    private Instant updatedAt;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class RolePermission {
        private String objectTypeId;
        @Builder.Default
        private List<String> ops = new ArrayList<>();
    }

    public static GovernanceRole create(String ontologyId) {
        return GovernanceRole.builder()
                .id(UUID.randomUUID().toString())
                .ontologyId(ontologyId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
