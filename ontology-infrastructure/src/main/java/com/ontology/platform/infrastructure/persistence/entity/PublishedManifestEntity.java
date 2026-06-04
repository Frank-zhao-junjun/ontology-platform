package com.ontology.platform.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "published_manifests", uniqueConstraints = @UniqueConstraint(columnNames = {"context_id", "version"}))
@Getter
@Setter
public class PublishedManifestEntity {
    @Id
    @Column(length = 36)
    private String id;
    @Column(name = "context_id", nullable = false, length = 36)
    private String contextId;
    @Column(name = "ontology_id", nullable = false, length = 100)
    private String ontologyId;
    @Column(nullable = false, length = 50)
    private String version;
    @Column(name = "api_version", nullable = false, length = 40)
    private String apiVersion = "ontology.platform/v1";
    @Column(nullable = false, length = 20)
    private String status = "PUBLISHED";
    @Column(name = "snapshot_json", nullable = false, columnDefinition = "TEXT")
    private String snapshotJson;
    @Column(name = "created_at")
    private Instant createdAt;
}
