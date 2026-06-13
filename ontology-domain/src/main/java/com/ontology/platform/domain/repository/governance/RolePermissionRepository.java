package com.ontology.platform.domain.repository.governance;
import com.ontology.platform.domain.entity.governance.RolePermission;
import java.util.List;

public interface RolePermissionRepository {
    List<RolePermission> findByRoleId(String roleId);
    List<RolePermission> findByDomain(String domain);
    RolePermission save(RolePermission perm);
}
