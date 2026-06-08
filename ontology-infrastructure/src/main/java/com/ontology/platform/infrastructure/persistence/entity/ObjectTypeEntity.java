package com.ontology.platform.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;

@Entity
@Table(name = "object_types", uniqueConstraints = @UniqueConstraint(columnNames = {"context_id", "code"}))
@Getter
@Setter
public class ObjectTypeEntity {
    @Id
    @Column(length = 36)
    private String id;
    @Column(name = "context_id", nullable = false, length = 36)
    private String contextId;
    @Column(name = "aggregate_root_id", length = 36)
    private String aggregateRootId;
    @Column(name = "parent_object_id", length = 36)
    private String parentObjectId;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(nullable = false, length = 50)
    private String code;
    @Column(name = "object_kind", nullable = false, length = 20)
    private String objectKind;
    private String description;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private String attributes = "[]";
    @Column(name = "is_active")
    private boolean active = true;
    @Column(name = "created_at")
    private Instant createdAt;
}
