package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.infrastructure.persistence.entity.RolePermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RolePermissionJpaRepository extends JpaRepository<RolePermissionEntity, String> {
    boolean existsByRoleIdAndObjectTypeId(String roleId, String objectTypeId);
    List<RolePermissionEntity> findByRoleId(String roleId);
}
