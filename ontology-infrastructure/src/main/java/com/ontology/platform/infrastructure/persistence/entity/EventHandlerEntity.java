package com.ontology.platform.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "event_handlers")
@Getter
@Setter
public class EventHandlerEntity {
    @Id
    @Column(length = 36)
    private String id;
    @Column(name = "context_id", nullable = false, length = 36)
    private String contextId;
    @Column(name = "manifest_code", nullable = false, length = 80)
    private String manifestCode;
    @Column(name = "event_id", nullable = false, length = 36)
    private String eventId;
    @Column(name = "handler_behavior_id", nullable = false, length = 36)
    private String handlerBehaviorId;
    @Column(name = "scenario_id", length = 36)
    private String scenarioId;
    @Column(name = "precondition_state", length = 80)
    private String preconditionState;
    @Column(nullable = false)
    private int priority = 100;
    @Column(name = "execution_mode", length = 10)
    private String executionMode = "SYNC";
    @Column(name = "created_at")
    private Instant createdAt;
}
