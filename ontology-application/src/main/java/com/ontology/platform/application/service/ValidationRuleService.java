package com.ontology.platform.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.application.dto.domain.CreateValidationRuleRequest;
import com.ontology.platform.application.dto.domain.ValidationRuleResponse;
import com.ontology.platform.domain.entity.ValidationRule;
import com.ontology.platform.infrastructure.persistence.ValidationRulePO;
import com.ontology.platform.infrastructure.persistence.ValidationRulePOMapper;
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

    private final ValidationRulePOMapper mapper;
    private final ObjectMapper objectMapper;

    @Transactional
    public ValidationRuleResponse create(String ontologyId, CreateValidationRuleRequest request, String userId) {
        log.info("Creating ValidationRule: ontologyId={}, name={}", ontologyId, request.getValidationRuleName());
        
        ValidationRule entity = ValidationRule.create(ontologyId, request.getValidationRuleName(), null);
        // Map request fields to entity
        mapRequestToEntity(request, entity);
        
        ValidationRulePO po = toPO(entity);
        mapper.insert(po);
        
        return toResponse(entity);
    }

    public ValidationRuleResponse getById(String id) {
        ValidationRulePO po = mapper.selectById(id);
        if (po == null) return null;
        return toResponse(fromPO(po));
    }

    public List<ValidationRuleResponse> listByOntologyId(String ontologyId) {
        List<ValidationRulePO> pos = mapper.selectByOntologyId(ontologyId);
        return pos.stream().map(po -> toResponse(fromPO(po))).collect(Collectors.toList());
    }

    @Transactional
    public ValidationRuleResponse update(String id, CreateValidationRuleRequest request) {
        ValidationRulePO po = mapper.selectById(id);
        if (po == null) throw new RuntimeException("ValidationRule not found: " + id);
        
        ValidationRule entity = fromPO(po);
        mapRequestToEntity(request, entity);
        entity.setUpdatedAt(Instant.now());
        
        mapper.updateById(toPO(entity));
        return toResponse(entity);
    }

    @Transactional
    public void delete(String id) {
        ValidationRulePO po = mapper.selectById(id);
        if (po != null) {
            po.setDeleted(true);
            po.setUpdatedAt(Instant.now());
            mapper.updateById(po);
        }
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

        private ValidationRulePO toPO(ValidationRule entity) {
        return ValidationRulePO.builder()
                .id(entity.getId())
                .ontologyId(entity.getOntologyId())
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
                .deleted(entity.getDeleted())
                .build();
    }

        private ValidationRule fromPO(ValidationRulePO po) {
        return ValidationRule.builder()
                .id(po.getId())
                .ontologyId(po.getOntologyId())
                .entityId(po.getEntityId())
                .fieldName(po.getFieldName())
                .ruleType(po.getRuleType())
                .ruleName(po.getRuleName())
                .description(po.getDescription())
                .severity(po.getSeverity())
                .expression(po.getExpression())
                .errorMessage(po.getErrorMessage())
                .enabled(po.getEnabled())
                .sortOrder(po.getSortOrder())
                .extendedData(po.getExtendedData())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .deleted(po.getDeleted())
                .build();
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
