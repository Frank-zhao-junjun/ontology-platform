package com.ontology.platform.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Entity
@Table(name = "roles", uniqueConstraints = @UniqueConstraint(columnNames = {"context_id", "code"}))
@Getter
@Setter
public class RoleEntity {
    @Id
    @Column(length = 36)
    private String id;
    @Column(name = "context_id", length = 36)
    private String contextId;
    @Column(nullable = false, length = 50)
    private String name;
    @Column(nullable = false, length = 30)
    private String code;
    private String description;
    @Column(name = "is_global")
    private boolean global;
    @Column(name = "created_at")
    private Instant createdAt;
}
