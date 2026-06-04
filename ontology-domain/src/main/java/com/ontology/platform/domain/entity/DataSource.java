package com.ontology.platform.domain.entity;

import lombok.Getter;
import java.time.Instant;
import java.util.UUID;

@Getter
public class DataSource {
    private final String id;
    private final String name;
    private final String code;
    private final String sourceType;
    private final String connectionConfig;
    private final String credentialRef;
    private final boolean active;
    private final Instant createdAt;

    private DataSource(String id, String name, String code, String sourceType,
                       String connectionConfig, String credentialRef, boolean active, Instant createdAt) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.name = name;
        this.code = code;
        this.sourceType = sourceType;
        this.connectionConfig = connectionConfig != null ? connectionConfig : "{}";
        this.credentialRef = credentialRef;
        this.active = active;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public static DataSource create(String name, String code, String sourceType,
                                    String connectionConfig, String credentialRef) {
        return new DataSource(null, name, code, sourceType, connectionConfig, credentialRef, true, null);
    }

    public static DataSource rehydrate(String id, String name, String code, String sourceType,
                                       String connectionConfig, String credentialRef, boolean active, Instant createdAt) {
        return new DataSource(id, name, code, sourceType, connectionConfig, credentialRef, active, createdAt);
    }
}
