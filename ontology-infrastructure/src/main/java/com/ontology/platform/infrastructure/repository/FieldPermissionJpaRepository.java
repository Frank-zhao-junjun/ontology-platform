package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.infrastructure.persistence.entity.FieldPermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FieldPermissionJpaRepository extends JpaRepository<FieldPermissionEntity, String> {
    boolean existsByRoleIdAndObjectTypeIdAndFieldName(String roleId, String objectTypeId, String fieldName);
    List<FieldPermissionEntity> findByRoleId(String roleId);
}
