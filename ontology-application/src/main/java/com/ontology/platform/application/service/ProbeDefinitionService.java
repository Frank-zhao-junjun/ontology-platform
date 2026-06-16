package com.ontology.platform.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.application.dto.domain.CreateProbeDefinitionRequest;
import com.ontology.platform.application.dto.domain.ProbeDefinitionResponse;
import com.ontology.platform.domain.entity.ProbeDefinition;
import com.ontology.platform.domain.repository.ProbeDefinitionRepository;
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
public class ProbeDefinitionService {

    private final ProbeDefinitionRepository repository;
    private final ObjectMapper objectMapper;

    @Transactional
    public ProbeDefinitionResponse create(String ontologyId, CreateProbeDefinitionRequest request, String userId) {
        log.info("Creating ProbeDefinition: ontologyId={}, name={}", ontologyId, request.getProbeName());
        
        ProbeDefinition entity = ProbeDefinition.create(ontologyId, request.getProbeName(), request.getTarget());
        // Map request fields to entity
        mapRequestToEntity(request, entity);
        
        entity = repository.save(entity);
        
        return toResponse(entity);
    }

    public ProbeDefinitionResponse getById(String id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElse(null);
    }

    public List<ProbeDefinitionResponse> listByOntologyId(String ontologyId) {
        return repository.findByOntologyId(ontologyId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProbeDefinitionResponse update(String id, CreateProbeDefinitionRequest request) {
        ProbeDefinition entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("ProbeDefinition not found: " + id));
        
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

        private void mapRequestToEntity(CreateProbeDefinitionRequest req, ProbeDefinition entity) {
        if (req.getProbeName() != null) entity.setProbeName(req.getProbeName());
        if (req.getDescription() != null) entity.setDescription(req.getDescription());
        if (req.getTarget() != null) entity.setTarget(req.getTarget());
        if (req.getProbeType() != null) entity.setProbeType(req.getProbeType());
        if (req.getFrequencySec() != null) entity.setFrequencySec(req.getFrequencySec());
        if (req.getTimeoutMs() != null) entity.setTimeoutMs(req.getTimeoutMs());
        if (req.getAlertCondition() != null) entity.setAlertCondition(req.getAlertCondition());
        if (req.getAlertSeverity() != null) entity.setAlertSeverity(req.getAlertSeverity());
        if (req.getEnabled() != null) entity.setEnabled(req.getEnabled());
        if (req.getConfig() != null) entity.setConfig(req.getConfig());
    }

        private ProbeDefinitionResponse toResponse(ProbeDefinition entity) {
        return ProbeDefinitionResponse.builder()
                .id(entity.getId())
                .probeName(entity.getProbeName())
                .description(entity.getDescription())
                .target(entity.getTarget())
                .probeType(entity.getProbeType())
                .frequencySec(entity.getFrequencySec())
                .timeoutMs(entity.getTimeoutMs())
                .alertCondition(entity.getAlertCondition())
                .alertSeverity(entity.getAlertSeverity())
                .enabled(entity.getEnabled())
                .config(entity.getConfig())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
