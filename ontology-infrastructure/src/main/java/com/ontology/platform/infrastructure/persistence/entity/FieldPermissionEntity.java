package com.ontology.platform.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Entity
@Table(name = "field_permissions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"role_id", "object_type_id", "field_name"}))
@Getter
@Setter
public class FieldPermissionEntity {
    @Id
    @Column(length = 36)
    private String id;
    @Column(name = "role_id", nullable = false, length = 36)
    private String roleId;
    @Column(name = "object_type_id", nullable = false, length = 36)
    private String objectTypeId;
    @Column(name = "field_name", nullable = false, length = 100)
    private String fieldName;
    @Column(name = "is_visible")
    private boolean visible = true;
    @Column(name = "is_editable")
    private boolean editable;
    @Column(name = "created_at")
    private Instant createdAt;
}
