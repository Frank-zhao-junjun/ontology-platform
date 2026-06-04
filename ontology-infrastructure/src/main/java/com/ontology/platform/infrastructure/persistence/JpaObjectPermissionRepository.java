package com.ontology.platform.infrastructure.persistence;

import com.ontology.platform.domain.entity.ObjectPermission;
import com.ontology.platform.domain.repository.ObjectPermissionRepository;
import com.ontology.platform.infrastructure.repository.RolePermissionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JpaObjectPermissionRepository implements ObjectPermissionRepository {
    private final RolePermissionJpaRepository jpa;

    @Override public void save(ObjectPermission permission) { jpa.save(PersistenceMapper.toEntity(permission)); }
    @Override public boolean existsByRoleAndObjectType(String roleId, String objectTypeId) {
        return jpa.existsByRoleIdAndObjectTypeId(roleId, objectTypeId);
    }
    @Override public List<ObjectPermission> findByRoleId(String roleId) {
        return jpa.findByRoleId(roleId).stream().map(PersistenceMapper::toDomain).collect(Collectors.toList());
    }
}
