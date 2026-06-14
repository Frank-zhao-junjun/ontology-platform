package com.ontology.platform.infrastructure.repository.governance;

import com.ontology.platform.domain.entity.governance.RolePermission;
import com.ontology.platform.domain.repository.governance.RolePermissionRepository;
import com.ontology.platform.infrastructure.converter.RolePermissionConverter;
import com.ontology.platform.infrastructure.persistence.RolePermissionPO;
import com.ontology.platform.infrastructure.persistence.RolePermissionPOMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RolePermissionRepositoryImpl implements RolePermissionRepository {

    private final RolePermissionPOMapper rolePermissionPOMapper;
    private final RolePermissionConverter rolePermissionConverter;

    @Override
    public List<RolePermission> findByRoleId(String roleId) {
        log.debug("Finding permissions by roleId: {}", roleId);
        List<RolePermissionPO> poList = rolePermissionPOMapper.selectByRoleId(roleId);
        return rolePermissionConverter.toEntityList(poList);
    }

    @Override
    public List<RolePermission> findByDomain(String domain) {
        log.debug("Finding permissions by domain: {}", domain);
        List<RolePermissionPO> poList = rolePermissionPOMapper.selectByDomain(domain);
        return rolePermissionConverter.toEntityList(poList);
    }

    @Override
    public RolePermission save(RolePermission perm) {
        log.debug("Saving role permission: {}", perm.getId());
        RolePermissionPO po = rolePermissionConverter.toPO(perm);
        rolePermissionPOMapper.insert(po);
        return perm;
    }
}
