package com.ontology.platform.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.application.dto.domain.CreateIndicatorDefinitionRequest;
import com.ontology.platform.application.dto.domain.IndicatorDefinitionResponse;
import com.ontology.platform.domain.entity.IndicatorDefinition;
import com.ontology.platform.infrastructure.persistence.IndicatorDefinitionPO;
import com.ontology.platform.infrastructure.persistence.IndicatorDefinitionPOMapper;
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
public class IndicatorDefinitionService {

    private final IndicatorDefinitionPOMapper mapper;
    private final ObjectMapper objectMapper;

    @Transactional
    public IndicatorDefinitionResponse create(String ontologyId, CreateIndicatorDefinitionRequest request, String userId) {
        log.info("Creating IndicatorDefinition: ontologyId={}, name={}", ontologyId, request.getIndicatorDefinitionName());
        
        IndicatorDefinition entity = IndicatorDefinition.create(ontologyId, request.getIndicatorDefinitionName(), null);
        // Map request fields to entity
        mapRequestToEntity(request, entity);
        
        IndicatorDefinitionPO po = toPO(entity);
        mapper.insert(po);
        
        return toResponse(entity);
    }

    public IndicatorDefinitionResponse getById(String id) {
        IndicatorDefinitionPO po = mapper.selectById(id);
        if (po == null) return null;
        return toResponse(fromPO(po));
    }

    public List<IndicatorDefinitionResponse> listByOntologyId(String ontologyId) {
        List<IndicatorDefinitionPO> pos = mapper.selectByOntologyId(ontologyId);
        return pos.stream().map(po -> toResponse(fromPO(po))).collect(Collectors.toList());
    }

    @Transactional
    public IndicatorDefinitionResponse update(String id, CreateIndicatorDefinitionRequest request) {
        IndicatorDefinitionPO po = mapper.selectById(id);
        if (po == null) throw new RuntimeException("IndicatorDefinition not found: " + id);
        
        IndicatorDefinition entity = fromPO(po);
        mapRequestToEntity(request, entity);
        entity.setUpdatedAt(Instant.now());
        
        mapper.updateById(toPO(entity));
        return toResponse(entity);
    }

    @Transactional
    public void delete(String id) {
        IndicatorDefinitionPO po = mapper.selectById(id);
        if (po != null) {
            po.setDeleted(true);
            po.setUpdatedAt(Instant.now());
            mapper.updateById(po);
        }
    }

    // ── mapping helpers ──

        private void mapRequestToEntity(CreateIndicatorDefinitionRequest req, IndicatorDefinition entity) {
        if (req.getIndicatorName() != null) entity.setIndicatorName(req.getIndicatorName());
        if (req.getDescription() != null) entity.setDescription(req.getDescription());
        if (req.getFormula() != null) entity.setFormula(req.getFormula());
        if (req.getTargetValue() != null) entity.setTargetValue(req.getTargetValue());
        if (req.getUnit() != null) entity.setUnit(req.getUnit());
        if (req.getWarningThreshold() != null) entity.setWarningThreshold(req.getWarningThreshold());
        if (req.getCriticalThreshold() != null) entity.setCriticalThreshold(req.getCriticalThreshold());
        if (req.getAggregationType() != null) entity.setAggregationType(req.getAggregationType());
        if (req.getFrequencySec() != null) entity.setFrequencySec(req.getFrequencySec());
        if (req.getEnabled() != null) entity.setEnabled(req.getEnabled());
        if (req.getExtendedData() != null) entity.setExtendedData(req.getExtendedData());
    }

        private IndicatorDefinitionPO toPO(IndicatorDefinition entity) {
        return IndicatorDefinitionPO.builder()
                .id(entity.getId())
                .ontologyId(entity.getOntologyId())
                .indicatorName(entity.getIndicatorName())
                .description(entity.getDescription())
                .formula(entity.getFormula())
                .targetValue(entity.getTargetValue())
                .unit(entity.getUnit())
                .warningThreshold(entity.getWarningThreshold())
                .criticalThreshold(entity.getCriticalThreshold())
                .aggregationType(entity.getAggregationType())
                .frequencySec(entity.getFrequencySec())
                .enabled(entity.getEnabled())
                .extendedData(entity.getExtendedData())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .deleted(entity.getDeleted())
                .build();
    }

        private IndicatorDefinition fromPO(IndicatorDefinitionPO po) {
        return IndicatorDefinition.builder()
                .id(po.getId())
                .ontologyId(po.getOntologyId())
                .indicatorName(po.getIndicatorName())
                .description(po.getDescription())
                .formula(po.getFormula())
                .targetValue(po.getTargetValue())
                .unit(po.getUnit())
                .warningThreshold(po.getWarningThreshold())
                .criticalThreshold(po.getCriticalThreshold())
                .aggregationType(po.getAggregationType())
                .frequencySec(po.getFrequencySec())
                .enabled(po.getEnabled())
                .extendedData(po.getExtendedData())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .deleted(po.getDeleted())
                .build();
    }

        private IndicatorDefinitionResponse toResponse(IndicatorDefinition entity) {
        return IndicatorDefinitionResponse.builder()
                .id(entity.getId())
                .indicatorName(entity.getIndicatorName())
                .description(entity.getDescription())
                .formula(entity.getFormula())
                .targetValue(entity.getTargetValue())
                .unit(entity.getUnit())
                .warningThreshold(entity.getWarningThreshold())
                .criticalThreshold(entity.getCriticalThreshold())
                .aggregationType(entity.getAggregationType())
                .frequencySec(entity.getFrequencySec())
                .enabled(entity.getEnabled())
                .extendedData(entity.getExtendedData())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
