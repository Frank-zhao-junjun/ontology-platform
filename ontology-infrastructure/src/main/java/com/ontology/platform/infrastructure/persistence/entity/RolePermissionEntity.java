package com.ontology.platform.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Entity
@Table(name = "role_permissions", uniqueConstraints = @UniqueConstraint(columnNames = {"role_id", "object_type_id"}))
@Getter
@Setter
public class RolePermissionEntity {
    @Id
    @Column(length = 36)
    private String id;
    @Column(name = "role_id", nullable = false, length = 36)
    private String roleId;
    @Column(name = "object_type_id", nullable = false, length = 36)
    private String objectTypeId;
    @Column(name = "perm_read")
    private boolean permRead;
    @Column(name = "perm_write")
    private boolean permWrite;
    @Column(name = "perm_delete")
    private boolean permDelete;
    @Column(name = "perm_execute")
    private boolean permExecute;
    @Column(name = "created_at")
    private Instant createdAt;
}
