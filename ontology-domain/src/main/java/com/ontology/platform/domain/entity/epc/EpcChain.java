package com.ontology.platform.domain.entity.epc;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class EpcChain {
    private String id;
    private String name;
    private String aggregateRootId;
    private String description;
    private String chainType;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;

    public static EpcChain create() {
        return EpcChain.builder().id(UUID.randomUUID().toString())
                .createdAt(Instant.now()).updatedAt(Instant.now()).build();
    }
}
