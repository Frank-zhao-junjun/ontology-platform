package com.ontology.platform.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.application.dto.domain.CreatePolicyRuleRequest;
import com.ontology.platform.application.dto.domain.PolicyRuleResponse;
import com.ontology.platform.domain.entity.PolicyRule;
import com.ontology.platform.infrastructure.persistence.PolicyRulePO;
import com.ontology.platform.infrastructure.persistence.PolicyRulePOMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyRuleService {

    private final PolicyRulePOMapper mapper;
    private final ObjectMapper objectMapper;

    @Transactional
    public PolicyRuleResponse create(String ontologyId, CreatePolicyRuleRequest request, String userId) {
        log.info("Creating PolicyRule: ontologyId={}, name={}", ontologyId, request.getPolicyRuleName());
        
        PolicyRule entity = PolicyRule.create(ontologyId, request.getPolicyRuleName(), null);
        // Map request fields to entity
        mapRequestToEntity(request, entity);
        
        PolicyRulePO po = toPO(entity);
        mapper.insert(po);
        
        return toResponse(entity);
    }

    public PolicyRuleResponse getById(String id) {
        PolicyRulePO po = mapper.selectById(id);
        if (po == null) return null;
        return toResponse(fromPO(po));
    }

    public List<PolicyRuleResponse> listByOntologyId(String ontologyId) {
        List<PolicyRulePO> pos = mapper.selectByOntologyId(ontologyId);
        return pos.stream().map(po -> toResponse(fromPO(po))).collect(Collectors.toList());
    }

    @Transactional
    public PolicyRuleResponse update(String id, CreatePolicyRuleRequest request) {
        PolicyRulePO po = mapper.selectById(id);
        if (po == null) throw new RuntimeException("PolicyRule not found: " + id);
        
        PolicyRule entity = fromPO(po);
        mapRequestToEntity(request, entity);
        entity.setUpdatedAt(Instant.now());
        
        mapper.updateById(toPO(entity));
        return toResponse(entity);
    }

    @Transactional
    public void delete(String id) {
        PolicyRulePO po = mapper.selectById(id);
        if (po != null) {
            po.setDeleted(true);
            po.setUpdatedAt(Instant.now());
            mapper.updateById(po);
        }
    }

    // ── mapping helpers ──

        private void mapRequestToEntity(CreatePolicyRuleRequest req, PolicyRule entity) {
        if (req.getPolicyName() != null) entity.setPolicyName(req.getPolicyName());
        if (req.getDescription() != null) entity.setDescription(req.getDescription());
        if (req.getPolicyType() != null) entity.setPolicyType(req.getPolicyType());
        if (req.getRules() != null) entity.setRules(req.getRules());
        if (req.getEffect() != null) entity.setEffect(req.getEffect());
        if (req.getPriority() != null) entity.setPriority(req.getPriority());
        if (req.getEnabled() != null) entity.setEnabled(req.getEnabled());
    }

        private PolicyRulePO toPO(PolicyRule entity) {
        return PolicyRulePO.builder()
                .id(entity.getId())
                .ontologyId(entity.getOntologyId())
                .policyName(entity.getPolicyName())
                .description(entity.getDescription())
                .policyType(entity.getPolicyType())
                .rules(entity.getRules())
                .effect(entity.getEffect())
                .priority(entity.getPriority())
                .enabled(entity.getEnabled())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .deleted(entity.getDeleted())
                .build();
    }

        private PolicyRule fromPO(PolicyRulePO po) {
        return PolicyRule.builder()
                .id(po.getId())
                .ontologyId(po.getOntologyId())
                .policyName(po.getPolicyName())
                .description(po.getDescription())
                .policyType(po.getPolicyType())
                .rules(po.getRules())
                .effect(po.getEffect())
                .priority(po.getPriority())
                .enabled(po.getEnabled())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .deleted(po.getDeleted())
                .build();
    }

        private PolicyRuleResponse toResponse(PolicyRule entity) {
        return PolicyRuleResponse.builder()
                .id(entity.getId())
                .policyName(entity.getPolicyName())
                .description(entity.getDescription())
                .policyType(entity.getPolicyType())
                .rules(entity.getRules())
                .effect(entity.getEffect())
                .priority(entity.getPriority())
                .enabled(entity.getEnabled())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
