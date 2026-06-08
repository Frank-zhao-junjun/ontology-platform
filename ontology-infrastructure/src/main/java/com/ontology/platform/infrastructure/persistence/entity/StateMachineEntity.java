package com.ontology.platform.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "state_machines", uniqueConstraints = @UniqueConstraint(columnNames = {"context_id", "object_type_id"}))
@Getter
@Setter
public class StateMachineEntity {
    @Id
    @Column(length = 36)
    private String id;
    @Column(name = "context_id", nullable = false, length = 36)
    private String contextId;
    @Column(nullable = false, length = 200)
    private String name;
    @Column(name = "name_en", length = 200)
    private String nameEn;
    @Column(name = "object_type_id", nullable = false, length = 36)
    private String objectTypeId;
    @Column(name = "status_field", nullable = false, length = 100)
    private String statusField = "status";
    @Column(name = "states_json", nullable = false, columnDefinition = "TEXT")
    private String statesJson = "[]";
    @Column(name = "transitions_json", nullable = false, columnDefinition = "TEXT")
    private String transitionsJson = "[]";
    @Column(name = "created_at")
    private Instant createdAt;
    @Column(name = "updated_at")
    private Instant updatedAt;
}
