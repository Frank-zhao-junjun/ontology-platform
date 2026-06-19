package com.ontology.platform.domain.entity;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class EpcModelRef {
    private String id;
    private String chainId;
    private String modelType;
    private String modelId;
    private String refMetadata;
    private Instant createdAt;
    private Instant updatedAt;

    public static EpcModelRef create() {
        return EpcModelRef.builder().id(UUID.randomUUID().toString())
                .createdAt(Instant.now()).updatedAt(Instant.now()).build();
    }
}
