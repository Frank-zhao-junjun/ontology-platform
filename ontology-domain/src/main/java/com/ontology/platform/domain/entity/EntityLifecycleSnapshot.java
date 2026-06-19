package com.ontology.platform.domain.entity;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class EntityLifecycleSnapshot {
    private String id;
    private String entityId;
    private String ontologyId;
    private String lifecycleData;
    private String snapshotVersion;
    private Instant createdAt;
    private Instant updatedAt;

    public static EntityLifecycleSnapshot create() {
        return EntityLifecycleSnapshot.builder().id(UUID.randomUUID().toString())
                .createdAt(Instant.now()).updatedAt(Instant.now()).build();
    }
}
