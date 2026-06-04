package com.ontology.platform.domain.repository;

import com.ontology.platform.domain.entity.ObjectPermission;
import java.util.List;

public interface ObjectPermissionRepository {
    void save(ObjectPermission permission);
    boolean existsByRoleAndObjectType(String roleId, String objectTypeId);
    List<ObjectPermission> findByRoleId(String roleId);
}
