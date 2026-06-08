package com.ontology.platform.domain.repository;

import com.ontology.platform.domain.entity.FieldPermission;
import java.util.List;

public interface FieldPermissionRepository {
    void save(FieldPermission permission);
    boolean existsByRoleAndObjectTypeAndField(String roleId, String objectTypeId, String fieldName);
    List<FieldPermission> findByRoleId(String roleId);
}
