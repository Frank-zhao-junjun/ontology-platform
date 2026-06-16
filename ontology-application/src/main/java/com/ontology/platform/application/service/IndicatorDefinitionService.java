package com.ontology.platform.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.application.dto.domain.CreateIndicatorDefinitionRequest;
import com.ontology.platform.application.dto.domain.IndicatorDefinitionResponse;
import com.ontology.platform.domain.entity.IndicatorDefinition;
import com.ontology.platform.domain.repository.IndicatorDefinitionRepository;
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

    private final IndicatorDefinitionRepository repository;
    private final ObjectMapper objectMapper;

    @Transactional
    public IndicatorDefinitionResponse create(String ontologyId, CreateIndicatorDefinitionRequest request, String userId) {
        log.info("Creating IndicatorDefinition: ontologyId={}, name={}", ontologyId, request.getIndicatorName());
        
        IndicatorDefinition entity = IndicatorDefinition.create(ontologyId, request.getIndicatorName(), null);
        // Map request fields to entity
        mapRequestToEntity(request, entity);
        
        entity = repository.save(entity);
        
        return toResponse(entity);
    }

    public IndicatorDefinitionResponse getById(String id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElse(null);
    }

    public List<IndicatorDefinitionResponse> listByOntologyId(String ontologyId) {
        return repository.findByOntologyId(ontologyId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public IndicatorDefinitionResponse update(String id, CreateIndicatorDefinitionRequest request) {
        IndicatorDefinition entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("IndicatorDefinition not found: " + id));
        
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
