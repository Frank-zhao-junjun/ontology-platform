package com.ontology.platform.infrastructure.persistence;

import com.ontology.platform.domain.entity.FieldPermission;
import com.ontology.platform.domain.repository.FieldPermissionRepository;
import com.ontology.platform.infrastructure.repository.FieldPermissionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JpaFieldPermissionRepository implements FieldPermissionRepository {
    private final FieldPermissionJpaRepository jpa;

    @Override public void save(FieldPermission permission) { jpa.save(PersistenceMapper.toEntity(permission)); }
    @Override public boolean existsByRoleAndObjectTypeAndField(String roleId, String objectTypeId, String fieldName) {
        return jpa.existsByRoleIdAndObjectTypeIdAndFieldName(roleId, objectTypeId, fieldName);
    }
    @Override public List<FieldPermission> findByRoleId(String roleId) {
        return jpa.findByRoleId(roleId).stream().map(PersistenceMapper::toDomain).collect(Collectors.toList());
    }
}
