package com.ontology.platform.application.service;

import com.ontology.platform.application.dto.domain.CreateGovernanceRoleRequest;
import com.ontology.platform.application.dto.domain.GovernanceRoleResponse;
import com.ontology.platform.domain.entity.GovernanceRole;
import com.ontology.platform.domain.repository.GovernanceRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j @Service @RequiredArgsConstructor
public class GovernanceRoleService {
    private final GovernanceRoleRepository repository;

    @Transactional
    public GovernanceRoleResponse create(String ontologyId, CreateGovernanceRoleRequest req) {
        var entity = GovernanceRole.create(ontologyId);
        mapRequest(req, entity);
        return toResponse(repository.save(entity));
    }

    public GovernanceRoleResponse getById(String id) {
        return repository.findById(id).map(this::toResponse).orElse(null);
    }

    public List<GovernanceRoleResponse> listByOntologyId(String ontologyId) {
        return repository.findByOntologyId(ontologyId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional public void delete(String id) { repository.deleteById(id); }

    private void mapRequest(CreateGovernanceRoleRequest req, GovernanceRole entity) {
        if (req.getName() != null) entity.setName(req.getName());
        if (req.getDescription() != null) entity.setDescription(req.getDescription());
        if (req.getPermissions() != null) entity.setPermissions(req.getPermissions().stream()
                .map(p -> GovernanceRole.RolePermission.builder()
                        .objectTypeId(p.getObjectTypeId()).ops(p.getOps()).build())
                .toList());
    }

    private GovernanceRoleResponse toResponse(GovernanceRole entity) {
        return GovernanceRoleResponse.builder()
                .id(entity.getId()).ontologyId(entity.getOntologyId())
                .name(entity.getName()).description(entity.getDescription())
                .permissions(entity.getPermissions().stream()
                        .map(p -> GovernanceRoleResponse.PermissionEntry.builder()
                                .objectTypeId(p.getObjectTypeId()).ops(p.getOps()).build())
                        .toList())
                .createdAt(entity.getCreatedAt()).updatedAt(entity.getUpdatedAt())
                .build();
    }
}
