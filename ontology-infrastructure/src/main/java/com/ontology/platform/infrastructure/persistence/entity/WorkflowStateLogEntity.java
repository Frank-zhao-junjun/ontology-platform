package com.ontology.platform.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "workflow_state_log")
@Getter
@Setter
public class WorkflowStateLogEntity {
    @Id
    @Column(length = 36)
    private String id;
    @Column(name = "context_id", nullable = false, length = 36)
    private String contextId;
    @Column(name = "from_state", nullable = false, length = 20)
    private String fromState;
    @Column(name = "to_state", nullable = false, length = 20)
    private String toState;
    @Column(name = "operated_by", nullable = false, length = 100)
    private String operatedBy;
    @Column(name = "operated_at")
    private Instant operatedAt;
    @Column(columnDefinition = "TEXT")
    private String comment;
}
