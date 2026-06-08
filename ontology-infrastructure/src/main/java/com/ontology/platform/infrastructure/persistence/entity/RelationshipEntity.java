package com.ontology.platform.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Entity
@Table(name = "relationships")
@Getter
@Setter
public class RelationshipEntity {
    @Id
    @Column(length = 36)
    private String id;
    @Column(name = "context_id", nullable = false, length = 36)
    private String contextId;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(nullable = false, length = 50)
    private String code;
    @Column(name = "source_object_id", nullable = false, length = 36)
    private String sourceObjectId;
    @Column(name = "target_object_id", nullable = false, length = 36)
    private String targetObjectId;
    @Column(nullable = false, length = 10)
    private String cardinality;
    @Column(name = "relation_kind", nullable = false, length = 20)
    private String relationKind;
    @Column(name = "is_cross_context")
    private boolean crossContext;
    @Column(name = "target_context_id", length = 36)
    private String targetContextId;
    @Column(name = "created_at")
    private Instant createdAt;
}
