package com.ontology.platform.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.application.dto.domain.CreateGuardrailRuleRequest;
import com.ontology.platform.application.dto.domain.GuardrailRuleResponse;
import com.ontology.platform.domain.entity.GuardrailRule;
import com.ontology.platform.infrastructure.persistence.GuardrailRulePO;
import com.ontology.platform.infrastructure.persistence.GuardrailRulePOMapper;
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

    private final GuardrailRulePOMapper mapper;
    private final ObjectMapper objectMapper;

    @Transactional
    public GuardrailRuleResponse create(String ontologyId, CreateGuardrailRuleRequest request, String userId) {
        log.info("Creating GuardrailRule: ontologyId={}, name={}", ontologyId, request.getGuardrailRuleName());
        
        GuardrailRule entity = GuardrailRule.create(ontologyId, request.getGuardrailRuleName(), null);
        // Map request fields to entity
        mapRequestToEntity(request, entity);
        
        GuardrailRulePO po = toPO(entity);
        mapper.insert(po);
        
        return toResponse(entity);
    }

    public GuardrailRuleResponse getById(String id) {
        GuardrailRulePO po = mapper.selectById(id);
        if (po == null) return null;
        return toResponse(fromPO(po));
    }

    public List<GuardrailRuleResponse> listByOntologyId(String ontologyId) {
        List<GuardrailRulePO> pos = mapper.selectByOntologyId(ontologyId);
        return pos.stream().map(po -> toResponse(fromPO(po))).collect(Collectors.toList());
    }

    @Transactional
    public GuardrailRuleResponse update(String id, CreateGuardrailRuleRequest request) {
        GuardrailRulePO po = mapper.selectById(id);
        if (po == null) throw new RuntimeException("GuardrailRule not found: " + id);
        
        GuardrailRule entity = fromPO(po);
        mapRequestToEntity(request, entity);
        entity.setUpdatedAt(Instant.now());
        
        mapper.updateById(toPO(entity));
        return toResponse(entity);
    }

    @Transactional
    public void delete(String id) {
        GuardrailRulePO po = mapper.selectById(id);
        if (po != null) {
            po.setDeleted(true);
            po.setUpdatedAt(Instant.now());
            mapper.updateById(po);
        }
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

        private GuardrailRulePO toPO(GuardrailRule entity) {
        return GuardrailRulePO.builder()
                .id(entity.getId())
                .ontologyId(entity.getOntologyId())
                .ruleName(entity.getRuleName())
                .description(entity.getDescription())
                .conditionExpr(entity.getConditionExpr())
                .actionType(entity.getActionType())
                .actionConfig(entity.getActionConfig())
                .priority(entity.getPriority())
                .enabled(entity.getEnabled())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .deleted(entity.getDeleted())
                .build();
    }

        private GuardrailRule fromPO(GuardrailRulePO po) {
        return GuardrailRule.builder()
                .id(po.getId())
                .ontologyId(po.getOntologyId())
                .ruleName(po.getRuleName())
                .description(po.getDescription())
                .conditionExpr(po.getConditionExpr())
                .actionType(po.getActionType())
                .actionConfig(po.getActionConfig())
                .priority(po.getPriority())
                .enabled(po.getEnabled())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .deleted(po.getDeleted())
                .build();
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
