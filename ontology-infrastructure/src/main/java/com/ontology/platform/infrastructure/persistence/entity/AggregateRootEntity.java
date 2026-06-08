package com.ontology.platform.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Entity
@Table(name = "aggregate_roots", uniqueConstraints = @UniqueConstraint(columnNames = {"context_id", "code"}))
@Getter
@Setter
public class AggregateRootEntity {
    @Id
    @Column(length = 36)
    private String id;
    @Column(name = "context_id", nullable = false, length = 36)
    private String contextId;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(nullable = false, length = 50)
    private String code;
    private String description;
    @Column(name = "is_active")
    private boolean active = true;
    @Column(name = "created_at")
    private Instant createdAt;
}
