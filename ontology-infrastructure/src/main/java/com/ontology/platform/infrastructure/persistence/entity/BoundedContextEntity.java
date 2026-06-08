package com.ontology.platform.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Entity
@Table(name = "bounded_contexts")
@Getter
@Setter
public class BoundedContextEntity {
    @Id
    @Column(length = 36)
    private String id;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(nullable = false, length = 50, unique = true)
    private String code;
    private String description;
    @Column(name = "domain_tag", length = 50)
    private String domainTag;
    @Column(name = "ontology_id", nullable = false, length = 36)
    private String ontologyId;
    @Column(name = "workflow_state", nullable = false, length = 20)
    private String workflowState;
    @Column(name = "created_by", length = 100)
    private String createdBy;
    @Column(name = "created_at")
    private Instant createdAt;
    @Column(name = "updated_at")
    private Instant updatedAt;
}
