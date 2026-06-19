package com.ontology.platform.domain.entity;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class IntentSlot {
    private String id;
    private String intentId;
    private String name;
    private String slotType;
    private Boolean required;
    private String examples;
    private Instant createdAt;
    private Instant updatedAt;

    public static IntentSlot create() {
        return IntentSlot.builder().id(UUID.randomUUID().toString())
                .createdAt(Instant.now()).updatedAt(Instant.now()).build();
    }
}
