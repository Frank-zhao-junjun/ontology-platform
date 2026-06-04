package com.ontology.platform.domain.entity;

import lombok.Builder;
import lombok.Getter;
import java.time.Instant;
import java.util.UUID;

@Getter
public class AggregateRoot {
    private final String id, contextId, name, code, description;
    private final Instant createdAt;
    private boolean active;

    @Builder
    public AggregateRoot(String id, String contextId, String name, String code, String description, Boolean active) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.contextId = contextId; this.name = name; this.code = code;
        this.description = description; this.active = active != null ? active : true;
        this.createdAt = Instant.now();
    }

    public static AggregateRoot create(String contextId, String name, String code, String description) {
        return AggregateRoot.builder().contextId(contextId).name(name).code(code).description(description).build();
    }

    public void deactivate() { this.active = false; }
}
