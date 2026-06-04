package com.ontology.platform.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "domain_events", uniqueConstraints = @UniqueConstraint(columnNames = {"context_id", "manifest_code"}))
@Getter
@Setter
public class DomainEventEntity {
    @Id
    @Column(length = 36)
    private String id;
    @Column(name = "context_id", nullable = false, length = 36)
    private String contextId;
    @Column(name = "manifest_code", nullable = false, length = 80)
    private String manifestCode;
    @Column(nullable = false, length = 200)
    private String name;
    @Column(name = "name_en", length = 200)
    private String nameEn;
    @Column(name = "aggregate_root_id", nullable = false, length = 36)
    private String aggregateRootId;
    @Column(name = "trigger_action_id", length = 36)
    private String triggerActionId;
    @Column(name = "payload_schema_json", nullable = false, columnDefinition = "TEXT")
    private String payloadSchemaJson = "{}";
    @Column(name = "created_at")
    private Instant createdAt;
}
