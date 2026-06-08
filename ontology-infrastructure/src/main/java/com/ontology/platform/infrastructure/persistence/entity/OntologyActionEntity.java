package com.ontology.platform.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "ontology_actions", uniqueConstraints = @UniqueConstraint(columnNames = {"context_id", "manifest_code"}))
@Getter
@Setter
public class OntologyActionEntity {
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
    private String description;
    @Column(name = "aggregate_root_id", nullable = false, length = 36)
    private String aggregateRootId;
    @Column(name = "invocation_mode", nullable = false, length = 20)
    private String invocationMode = "BOTH";
    @Column(name = "parameters_json", nullable = false, columnDefinition = "TEXT")
    private String parametersJson = "[]";
    @Column(name = "publishes_event_ids_json", nullable = false, columnDefinition = "TEXT")
    private String publishesEventIdsJson = "[]";
    @Column(name = "allowed_state_from_json", nullable = false, columnDefinition = "TEXT")
    private String allowedStateFromJson = "[]";
    @Column(name = "business_scenario_ids_json", nullable = false, columnDefinition = "TEXT")
    private String businessScenarioIdsJson = "[]";
    @Column(name = "mcp_tool_name", length = 120)
    private String mcpToolName;
    @Column(name = "created_at")
    private Instant createdAt;
}
