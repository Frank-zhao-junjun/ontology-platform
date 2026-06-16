package com.ontology.platform.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.application.dto.domain.CreateValidationRuleRequest;
import com.ontology.platform.application.dto.domain.ValidationRuleResponse;
import com.ontology.platform.domain.entity.ValidationRule;
import com.ontology.platform.domain.repository.ValidationRuleRepository;
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
public class ValidationRuleService {

    private final ValidationRuleRepository repository;
    private final ObjectMapper objectMapper;

    @Transactional
    public ValidationRuleResponse create(String ontologyId, CreateValidationRuleRequest request, String userId) {
        log.info("Creating ValidationRule: ontologyId={}, name={}", ontologyId, request.getRuleName());
        
        ValidationRule entity = ValidationRule.create(ontologyId, request.getRuleName(), request.getRuleType(), request.getExpression());
        // Map request fields to entity
        mapRequestToEntity(request, entity);
        
        entity = repository.save(entity);
        
        return toResponse(entity);
    }

    public ValidationRuleResponse getById(String id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElse(null);
    }

    public List<ValidationRuleResponse> listByOntologyId(String ontologyId) {
        return repository.findByOntologyId(ontologyId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ValidationRuleResponse update(String id, CreateValidationRuleRequest request) {
        ValidationRule entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("ValidationRule not found: " + id));
        
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

        private void mapRequestToEntity(CreateValidationRuleRequest req, ValidationRule entity) {
        if (req.getEntityId() != null) entity.setEntityId(req.getEntityId());
        if (req.getFieldName() != null) entity.setFieldName(req.getFieldName());
        if (req.getRuleType() != null) entity.setRuleType(req.getRuleType());
        if (req.getRuleName() != null) entity.setRuleName(req.getRuleName());
        if (req.getDescription() != null) entity.setDescription(req.getDescription());
        if (req.getSeverity() != null) entity.setSeverity(req.getSeverity());
        if (req.getExpression() != null) entity.setExpression(req.getExpression());
        if (req.getErrorMessage() != null) entity.setErrorMessage(req.getErrorMessage());
        if (req.getEnabled() != null) entity.setEnabled(req.getEnabled());
        if (req.getSortOrder() != null) entity.setSortOrder(req.getSortOrder());
        if (req.getExtendedData() != null) entity.setExtendedData(req.getExtendedData());
    }

        private ValidationRuleResponse toResponse(ValidationRule entity) {
        return ValidationRuleResponse.builder()
                .id(entity.getId())
                .entityId(entity.getEntityId())
                .fieldName(entity.getFieldName())
                .ruleType(entity.getRuleType())
                .ruleName(entity.getRuleName())
                .description(entity.getDescription())
                .severity(entity.getSeverity())
                .expression(entity.getExpression())
                .errorMessage(entity.getErrorMessage())
                .enabled(entity.getEnabled())
                .sortOrder(entity.getSortOrder())
                .extendedData(entity.getExtendedData())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
