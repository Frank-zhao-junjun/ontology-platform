package com.ontology.platform.domain.entity;

import lombok.Getter;
import java.time.Instant;
import java.util.UUID;

@Getter
public class FieldPermission {
    private final String id;
    private final String roleId;
    private final String objectTypeId;
    private final String fieldName;
    private final boolean visible;
    private final boolean editable;
    private final Instant createdAt;

    private FieldPermission(String id, String roleId, String objectTypeId, String fieldName,
                            boolean visible, boolean editable, Instant createdAt) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.roleId = roleId;
        this.objectTypeId = objectTypeId;
        this.fieldName = fieldName;
        this.visible = visible;
        this.editable = editable;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public static FieldPermission create(String roleId, String objectTypeId, String fieldName,
                                         boolean visible, boolean editable) {
        return new FieldPermission(null, roleId, objectTypeId, fieldName, visible, editable, null);
    }

    public static FieldPermission rehydrate(String id, String roleId, String objectTypeId, String fieldName,
                                            boolean visible, boolean editable, Instant createdAt) {
        return new FieldPermission(id, roleId, objectTypeId, fieldName, visible, editable, createdAt);
    }
}
