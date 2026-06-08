package com.ontology.platform.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "event_routes", uniqueConstraints = @UniqueConstraint(columnNames = {"context_id", "manifest_code"}))
@Getter
@Setter
public class EventRouteEntity {
    @Id
    @Column(length = 36)
    private String id;
    @Column(name = "context_id", nullable = false, length = 36)
    private String contextId;
    @Column(name = "manifest_code", nullable = false, length = 80)
    private String manifestCode;
    @Column(name = "source_event_id", nullable = false, length = 36)
    private String sourceEventId;
    @Column(name = "route_targets_json", nullable = false, columnDefinition = "TEXT")
    private String routeTargetsJson = "[]";
    @Column(name = "filter_conditions_json", columnDefinition = "TEXT")
    private String filterConditionsJson = "[]";
    @Column(name = "created_at")
    private Instant createdAt;
}
