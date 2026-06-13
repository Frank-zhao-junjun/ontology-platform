package com.ontology.platform.infrastructure.repository.governance;

import com.ontology.platform.domain.entity.governance.RolePermission;
import com.ontology.platform.domain.repository.governance.RolePermissionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j @Repository
public class InMemoryRolePermissionRepository implements RolePermissionRepository {
    private final Map<String, RolePermission> store = new ConcurrentHashMap<>();
    public List<RolePermission> findByRoleId(String roleId) {
        return store.values().stream().filter(p -> p.getRoleId().equals(roleId)).collect(Collectors.toList());
    }
    public List<RolePermission> findByDomain(String domain) {
        return store.values().stream().filter(p -> p.getDomain().equals(domain)).collect(Collectors.toList());
    }
    public RolePermission save(RolePermission perm) { store.put(perm.getId(), perm); return perm; }
}
