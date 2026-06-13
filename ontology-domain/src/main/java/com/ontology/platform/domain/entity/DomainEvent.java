package com.ontology.platform.domain.entity;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomainEvent {

    private String id;
    private String ontologyId;
    private String entityId;
    private String name;
    private String displayName;
    private String description;
    private String eventType;
    private String severity;
    private String payloadSchema;
    private String source;
    private Instant createdAt;
    private Instant updatedAt;
    private Boolean deleted;

    public static DomainEvent create(String ontologyId, String entityId, String name,
                                      String displayName, String eventType, String severity) {
        return DomainEvent.builder()
                .id(UUID.randomUUID().toString())
                .ontologyId(ontologyId)
                .entityId(entityId)
                .name(name)
                .displayName(displayName)
                .eventType(eventType)
                .severity(severity != null ? severity : "INFO")
                .payloadSchema("{}")
                .deleted(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public void softDelete() {
        this.deleted = true;
        this.updatedAt = Instant.now();
    }
}
