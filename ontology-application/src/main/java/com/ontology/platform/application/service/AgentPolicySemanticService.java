package com.ontology.platform.application.service;

import com.ontology.platform.application.dto.domain.CreateAgentPolicySemanticRequest;
import com.ontology.platform.application.dto.domain.AgentPolicySemanticResponse;
import com.ontology.platform.domain.entity.AgentPolicySemantic;
import com.ontology.platform.infrastructure.persistence.AgentPolicySemanticPO;
import com.ontology.platform.infrastructure.persistence.AgentPolicySemanticPOMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j @Service @RequiredArgsConstructor
public class AgentPolicySemanticService {
    private final AgentPolicySemanticPOMapper mapper;

    @Transactional
    public AgentPolicySemanticResponse create(String ontologyId, CreateAgentPolicySemanticRequest request, String userId) {
        log.info("Creating AgentPolicySemantic");
        AgentPolicySemantic entity = AgentPolicySemantic.create();
        AgentPolicySemanticPO po = toPO(entity);
        mapper.insert(po);
        return toResponse(entity);
    }

    public AgentPolicySemanticResponse getById(String id) {
        AgentPolicySemanticPO po = mapper.selectById(id);
        return po == null ? null : toResponse(fromPO(po));
    }

    public List<AgentPolicySemanticResponse> list() {
        return mapper.selectList(null).stream()
                .map(po -> toResponse(fromPO(po))).collect(Collectors.toList());
    }

    @Transactional
    public void delete(String id) { mapper.deleteById(id); }

    private AgentPolicySemanticPO toPO(AgentPolicySemantic entity) {
        return AgentPolicySemanticPO.builder()
                .id(entity.getId())        .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private AgentPolicySemantic fromPO(AgentPolicySemanticPO po) {
        return AgentPolicySemantic.builder()
                .id(po.getId())        .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }

    private AgentPolicySemanticResponse toResponse(AgentPolicySemantic entity) {
        return AgentPolicySemanticResponse.builder()
                .id(entity.getId())        .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
