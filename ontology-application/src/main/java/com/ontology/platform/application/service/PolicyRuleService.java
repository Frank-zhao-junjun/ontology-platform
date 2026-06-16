package com.ontology.platform.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.application.dto.domain.CreatePolicyRuleRequest;
import com.ontology.platform.application.dto.domain.PolicyRuleResponse;
import com.ontology.platform.domain.entity.PolicyRule;
import com.ontology.platform.domain.repository.PolicyRuleRepository;
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

    private final PolicyRuleRepository repository;
    private final ObjectMapper objectMapper;

    @Transactional
    public PolicyRuleResponse create(String ontologyId, CreatePolicyRuleRequest request, String userId) {
        log.info("Creating PolicyRule: ontologyId={}, name={}", ontologyId, request.getPolicyName());
        
        PolicyRule entity = PolicyRule.create(ontologyId, request.getPolicyName(), request.getPolicyType());
        // Map request fields to entity
        mapRequestToEntity(request, entity);
        
        entity = repository.save(entity);
        
        return toResponse(entity);
    }

    public PolicyRuleResponse getById(String id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElse(null);
    }

    public List<PolicyRuleResponse> listByOntologyId(String ontologyId) {
        return repository.findByOntologyId(ontologyId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PolicyRuleResponse update(String id, CreatePolicyRuleRequest request) {
        PolicyRule entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("PolicyRule not found: " + id));
        
        mapRequestToEntity(request, entity);
        entity.setUpdatedAt(Instant.now());
        
        entity = repository.save(entity);
        return toResponse(entity);
    }

    @Transactional
    public void delete(String id) {
        repository.deleteById(id);
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
