package com.ontology.platform.application.service;

import com.ontology.platform.common.enums.ErrorCode;
import com.ontology.platform.common.exception.BusinessException;
import com.ontology.platform.common.exception.ResourceNotFoundException;
import com.ontology.platform.domain.entity.AgentSandbox;
import com.ontology.platform.domain.entity.FieldPermission;
import com.ontology.platform.domain.entity.ObjectPermission;
import com.ontology.platform.domain.entity.Role;
import com.ontology.platform.domain.repository.AgentSandboxRepository;
import com.ontology.platform.domain.repository.FieldPermissionRepository;
import com.ontology.platform.domain.repository.ObjectPermissionRepository;
import com.ontology.platform.domain.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class GovernanceService {
    private final ModelingService modelingService;
    private final RoleRepository roleRepo;
    private final ObjectPermissionRepository objectPermissionRepo;
    private final FieldPermissionRepository fieldPermissionRepo;
    private final AgentSandboxRepository sandboxRepo;

    public Role createRole(String contextId, String name, String code, String description) {
        if (roleRepo.existsByContextIdAndCode(contextId, code))
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "角色 code '" + code + "' 已存在");
        Role role = Role.create(contextId, name, code, description);
        roleRepo.save(role);
        return role;
    }

    public List<Role> listRoles(String contextId, Boolean isGlobal) {
        return roleRepo.findAll().stream()
                .filter(r -> {
                    if (isGlobal != null && isGlobal != r.isGlobal()) return false;
                    if (contextId != null && !contextId.isBlank()) return contextId.equals(r.getContextId());
                    return true;
                })
                .collect(Collectors.toList());
    }

    public Role getRole(String roleId) {
        return roleRepo.findById(roleId).orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleId));
    }

    public ObjectPermission addObjectPermission(String roleId, String objectTypeId,
                                                boolean read, boolean write, boolean delete, boolean execute) {
        getRole(roleId);
        modelingService.getObjectType(objectTypeId);
        if (objectPermissionRepo.existsByRoleAndObjectType(roleId, objectTypeId))
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "该角色对此对象类型的权限已配置");
        ObjectPermission p = ObjectPermission.create(roleId, objectTypeId, read, write, delete, execute);
        objectPermissionRepo.save(p);
        return p;
    }

    public List<ObjectPermission> listObjectPermissions(String roleId) {
        getRole(roleId);
        return objectPermissionRepo.findByRoleId(roleId);
    }

    public FieldPermission addFieldPermission(String roleId, String objectTypeId, String fieldName,
                                              boolean visible, boolean editable) {
        getRole(roleId);
        modelingService.getObjectType(objectTypeId);
        if (fieldName == null || fieldName.isBlank())
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "fieldName 不能为空");
        if (fieldPermissionRepo.existsByRoleAndObjectTypeAndField(roleId, objectTypeId, fieldName))
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "该角色对此字段的权限已配置");
        FieldPermission p = FieldPermission.create(roleId, objectTypeId, fieldName, visible, editable);
        fieldPermissionRepo.save(p);
        return p;
    }

    public List<FieldPermission> listFieldPermissions(String roleId) {
        getRole(roleId);
        return fieldPermissionRepo.findByRoleId(roleId);
    }

    public AgentSandbox createSandbox(String name, String manifestVersionId, String agentRoleId,
                                      List<String> allowedTools, List<String> allowedAggregateRoots,
                                      List<String> allowedBehaviors, int maxOpsPerSecond) {
        if (agentRoleId != null && !agentRoleId.isBlank()) getRole(agentRoleId);
        AgentSandbox sb = AgentSandbox.create(name, manifestVersionId, agentRoleId,
                allowedTools, allowedAggregateRoots, allowedBehaviors, maxOpsPerSecond);
        sandboxRepo.save(sb);
        return sb;
    }

    public List<AgentSandbox> listSandboxes() {
        return sandboxRepo.findAll();
    }
}
