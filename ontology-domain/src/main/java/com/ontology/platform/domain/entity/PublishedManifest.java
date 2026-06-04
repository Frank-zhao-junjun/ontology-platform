package com.ontology.platform.domain.entity;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class PublishedManifest {
    private final String id, contextId, ontologyId, version, apiVersion, status, snapshotJson;
    private final Instant createdAt;

    @Builder
    public PublishedManifest(String id, String contextId, String ontologyId, String version,
                             String apiVersion, String status, String snapshotJson, Instant createdAt) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.contextId = contextId;
        this.ontologyId = ontologyId;
        this.version = version;
        this.apiVersion = apiVersion != null ? apiVersion : "ontology.platform/v1";
        this.status = status != null ? status : "PUBLISHED";
        this.snapshotJson = snapshotJson;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public static PublishedManifest create(String contextId, String ontologyId, String version,
                                           String snapshotJson) {
        return PublishedManifest.builder().contextId(contextId).ontologyId(ontologyId).version(version)
                .snapshotJson(snapshotJson).build();
    }
}
