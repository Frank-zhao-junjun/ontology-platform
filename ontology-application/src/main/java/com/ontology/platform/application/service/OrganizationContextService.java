package com.ontology.platform.application.service;

import com.ontology.platform.application.dto.domain.GovernanceRoleResponse;
import com.ontology.platform.domain.entity.PositionEntry;
import com.ontology.platform.infrastructure.persistence.PositionEntryPO;
import com.ontology.platform.infrastructure.persistence.PositionEntryPOMapper;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Organization Context Service — resolves the Position → Role → Permission chain.
 *
 * <p>Given a position ID (or position entity), resolves:
 * <ol>
 *   <li>Position → GovernanceRole (via position.roleId)</li>
 *   <li>GovernanceRole → Permissions (via role.permissions JSONB)</li>
 *   <li>Effective permissions for the position holder</li>
 * </ol>
 */
@Slf4j @Service @RequiredArgsConstructor
public class OrganizationContextService {

    private final PositionEntryPOMapper positionMapper;
    private final GovernanceRoleService roleService;

    /**
     * Resolve the full permission chain for a position.
     */
    public PositionPermissionChain resolvePermissions(String positionId) {
        PositionEntryPO pos = positionMapper.selectById(positionId);
        if (pos == null) {
            log.warn("Position not found: {}", positionId);
            return null;
        }

        List<GovernanceRoleResponse.PermissionEntry> permissions = new ArrayList<>();
        String roleId = pos.getRoleId();
        String roleName = null;

        if (roleId != null && !roleId.isBlank()) {
            GovernanceRoleResponse role = roleService.getById(roleId);
            if (role != null) {
                roleName = role.getName();
                permissions = role.getPermissions();
                log.debug("Resolved position {} → role {} ({} permissions)",
                        pos.getName(), roleName, permissions.size());
            } else {
                log.warn("Position {} references unknown roleId: {}", pos.getId(), roleId);
            }
        }

        return PositionPermissionChain.builder()
                .positionId(pos.getId())
                .positionName(pos.getName())
                .departmentId(pos.getDepartmentId())
                .roleId(roleId)
                .roleName(roleName)
                .permissions(permissions)
                .build();
    }

    /**
     * Resolve permissions for all positions in an ontology.
     */
    public List<PositionPermissionChain> resolveAll(String ontologyId) {
        List<PositionEntryPO> positions = positionMapper.selectByOntologyId(ontologyId);
        if (positions == null) return List.of();
        return positions.stream()
                .map(p -> resolvePermissions(p.getId()))
                .filter(c -> c != null)
                .toList();
    }

    // ── DTO ──

    @Data @Builder
    public static class PositionPermissionChain {
        private String positionId;
        private String positionName;
        private String departmentId;
        private String roleId;
        private String roleName;
        @Builder.Default
        private List<GovernanceRoleResponse.PermissionEntry> permissions = new ArrayList<>();

        public boolean hasPermission(String objectTypeId, String operation) {
            return permissions.stream().anyMatch(p ->
                    p.getObjectTypeId().equals(objectTypeId) && p.getOps().contains(operation));
        }
    }
}
