package com.ontology.platform.domain.entity;

import lombok.Getter;
import java.time.Instant;
import java.util.UUID;

@Getter
public class ObjectPermission {
    private final String id;
    private final String roleId;
    private final String objectTypeId;
    private final boolean permRead;
    private final boolean permWrite;
    private final boolean permDelete;
    private final boolean permExecute;
    private final Instant createdAt;

    private ObjectPermission(String id, String roleId, String objectTypeId,
                             boolean permRead, boolean permWrite, boolean permDelete, boolean permExecute,
                             Instant createdAt) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.roleId = roleId;
        this.objectTypeId = objectTypeId;
        this.permRead = permRead;
        this.permWrite = permWrite;
        this.permDelete = permDelete;
        this.permExecute = permExecute;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public static ObjectPermission create(String roleId, String objectTypeId,
                                          boolean read, boolean write, boolean delete, boolean execute) {
        return new ObjectPermission(null, roleId, objectTypeId, read, write, delete, execute, null);
    }

    public static ObjectPermission rehydrate(String id, String roleId, String objectTypeId,
                                             boolean read, boolean write, boolean delete, boolean execute,
                                             Instant createdAt) {
        return new ObjectPermission(id, roleId, objectTypeId, read, write, delete, execute, createdAt);
    }
}
