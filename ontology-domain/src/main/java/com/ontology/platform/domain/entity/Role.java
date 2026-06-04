package com.ontology.platform.domain.entity;

import lombok.Getter;
import java.time.Instant;
import java.util.UUID;

@Getter
public class Role {
    private final String id;
    private final String contextId;
    private final String name;
    private final String code;
    private final String description;
    private final boolean global;
    private final Instant createdAt;

    private Role(String id, String contextId, String name, String code, String description) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.contextId = contextId;
        this.name = name;
        this.code = code;
        this.description = description;
        this.global = contextId == null || contextId.isBlank();
        this.createdAt = Instant.now();
    }

    public static Role create(String contextId, String name, String code, String description) {
        return new Role(null, contextId, name, code, description);
    }
}
