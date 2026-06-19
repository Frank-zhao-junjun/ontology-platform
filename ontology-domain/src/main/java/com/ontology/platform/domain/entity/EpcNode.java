package com.ontology.platform.domain.entity;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class EpcNode {
    private String id;
    private String chainId;
    private String nodeType;
    private String name;
    private String description;
    private String refType;
    private String refId;
    private String metadata;
    private Integer sortOrder;
    private Instant createdAt;
    private Instant updatedAt;

    public static EpcNode create() {
        return EpcNode.builder().id(UUID.randomUUID().toString())
                .createdAt(Instant.now()).updatedAt(Instant.now()).build();
    }
}
