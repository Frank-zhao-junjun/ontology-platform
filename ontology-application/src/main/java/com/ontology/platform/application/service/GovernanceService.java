package com.ontology.platform.application.service;

import com.ontology.platform.common.enums.ErrorCode;
import com.ontology.platform.common.exception.BusinessException;
import com.ontology.platform.common.exception.ResourceNotFoundException;
import com.ontology.platform.domain.entity.AgentSandbox;
import com.ontology.platform.domain.entity.ObjectPermission;
import com.ontology.platform.domain.entity.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GovernanceService {
    private final ModelingService modelingService;
    private final Map<String, Role> roles = new ConcurrentHashMap<>();
    private final Map<String, ObjectPermission> objectPermissions = new ConcurrentHashMap<>();
    private final Map<String, AgentSandbox> sandboxes = new ConcurrentHashMap<>();

    public Role createRole(String contextId, String name, String code, String description) {
        String ctxKey = contextId != null ? contextId : "";
        if (roles.values().stream().anyMatch(r ->
                Objects.equals(r.getContextId() != null ? r.getContextId() : "", ctxKey) && r.getCode().equals(code)))
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "角色 code '" + code + "' 已存在");
        Role role = Role.create(contextId, name, code, description);
        roles.put(role.getId(), role);
        return role;
    }

    public List<Role> listRoles(String contextId, Boolean isGlobal) {
        return roles.values().stream()
                .filter(r -> {
                    if (isGlobal != null && isGlobal != r.isGlobal()) return false;
                    if (contextId != null && !contextId.isBlank())
                        return contextId.equals(r.getContextId());
                    return true;
                })
                .collect(Collectors.toList());
    }

    public Role getRole(String roleId) {
        return Optional.ofNullable(roles.get(roleId))
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleId));
    }

    public ObjectPermission addObjectPermission(String roleId, String objectTypeId,
                                                boolean read, boolean write, boolean delete, boolean execute) {
        getRole(roleId);
        modelingService.getObjectType(objectTypeId);
        if (objectPermissions.values().stream().anyMatch(p ->
                p.getRoleId().equals(roleId) && p.getObjectTypeId().equals(objectTypeId)))
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "该角色对此对象类型的权限已配置");
        ObjectPermission p = ObjectPermission.create(roleId, objectTypeId, read, write, delete, execute);
        objectPermissions.put(p.getId(), p);
        return p;
    }

    public List<ObjectPermission> listObjectPermissions(String roleId) {
        getRole(roleId);
        return objectPermissions.values().stream().filter(p -> p.getRoleId().equals(roleId)).collect(Collectors.toList());
    }

    public AgentSandbox createSandbox(String name, String manifestVersionId, String agentRoleId,
                                      List<String> allowedTools, List<String> allowedAggregateRoots,
                                      List<String> allowedBehaviors, int maxOpsPerSecond) {
        if (agentRoleId != null && !agentRoleId.isBlank()) getRole(agentRoleId);
        AgentSandbox sb = AgentSandbox.create(name, manifestVersionId, agentRoleId,
                allowedTools, allowedAggregateRoots, allowedBehaviors, maxOpsPerSecond);
        sandboxes.put(sb.getId(), sb);
        return sb;
    }

    public List<AgentSandbox> listSandboxes() {
        return new ArrayList<>(sandboxes.values());
    }
}
