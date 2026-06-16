package com.ontology.platform.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.application.dto.domain.CreateGuardrailRuleRequest;
import com.ontology.platform.application.dto.domain.GuardrailRuleResponse;
import com.ontology.platform.domain.entity.GuardrailRule;
import com.ontology.platform.domain.repository.GuardrailRuleRepository;
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
public class GuardrailRuleService {

    private final GuardrailRuleRepository repository;
    private final ObjectMapper objectMapper;

    @Transactional
    public GuardrailRuleResponse create(String ontologyId, CreateGuardrailRuleRequest request, String userId) {
        log.info("Creating GuardrailRule: ontologyId={}, name={}", ontologyId, request.getRuleName());
        
        GuardrailRule entity = GuardrailRule.create(ontologyId, request.getRuleName(), request.getConditionExpr(), request.getActionType());
        // Map request fields to entity
        mapRequestToEntity(request, entity);
        
        entity = repository.save(entity);
        
        return toResponse(entity);
    }

    public GuardrailRuleResponse getById(String id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElse(null);
    }

    public List<GuardrailRuleResponse> listByOntologyId(String ontologyId) {
        return repository.findByOntologyId(ontologyId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public GuardrailRuleResponse update(String id, CreateGuardrailRuleRequest request) {
        GuardrailRule entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("GuardrailRule not found: " + id));
        
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

        private void mapRequestToEntity(CreateGuardrailRuleRequest req, GuardrailRule entity) {
        if (req.getRuleName() != null) entity.setRuleName(req.getRuleName());
        if (req.getDescription() != null) entity.setDescription(req.getDescription());
        if (req.getConditionExpr() != null) entity.setConditionExpr(req.getConditionExpr());
        if (req.getActionType() != null) entity.setActionType(req.getActionType());
        if (req.getActionConfig() != null) entity.setActionConfig(req.getActionConfig());
        if (req.getPriority() != null) entity.setPriority(req.getPriority());
        if (req.getEnabled() != null) entity.setEnabled(req.getEnabled());
    }

        private GuardrailRuleResponse toResponse(GuardrailRule entity) {
        return GuardrailRuleResponse.builder()
                .id(entity.getId())
                .ruleName(entity.getRuleName())
                .description(entity.getDescription())
                .conditionExpr(entity.getConditionExpr())
                .actionType(entity.getActionType())
                .actionConfig(entity.getActionConfig())
                .priority(entity.getPriority())
                .enabled(entity.getEnabled())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
