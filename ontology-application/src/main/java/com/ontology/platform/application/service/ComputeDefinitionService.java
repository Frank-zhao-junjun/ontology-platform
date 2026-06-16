package com.ontology.platform.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.application.dto.domain.CreateComputeDefinitionRequest;
import com.ontology.platform.application.dto.domain.ComputeDefinitionResponse;
import com.ontology.platform.domain.entity.ComputeDefinition;
import com.ontology.platform.domain.repository.ComputeDefinitionRepository;
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
public class ComputeDefinitionService {

    private final ComputeDefinitionRepository repository;
    private final ObjectMapper objectMapper;

    @Transactional
    public ComputeDefinitionResponse create(String ontologyId, CreateComputeDefinitionRequest request, String userId) {
        log.info("Creating ComputeDefinition: ontologyId={}, name={}", ontologyId, request.getComputeName());
        
        ComputeDefinition entity = ComputeDefinition.create(ontologyId, request.getComputeName(), null);
        // Map request fields to entity
        mapRequestToEntity(request, entity);
        
        entity = repository.save(entity);
        
        return toResponse(entity);
    }

    public ComputeDefinitionResponse getById(String id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElse(null);
    }

    public List<ComputeDefinitionResponse> listByOntologyId(String ontologyId) {
        return repository.findByOntologyId(ontologyId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ComputeDefinitionResponse update(String id, CreateComputeDefinitionRequest request) {
        ComputeDefinition entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("ComputeDefinition not found: " + id));
        
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

        private void mapRequestToEntity(CreateComputeDefinitionRequest req, ComputeDefinition entity) {
        if (req.getComputeName() != null) entity.setComputeName(req.getComputeName());
        if (req.getDescription() != null) entity.setDescription(req.getDescription());
        if (req.getInputSchema() != null) entity.setInputSchema(req.getInputSchema());
        if (req.getFormula() != null) entity.setFormula(req.getFormula());
        if (req.getOutputType() != null) entity.setOutputType(req.getOutputType());
        if (req.getOutputSchema() != null) entity.setOutputSchema(req.getOutputSchema());
        if (req.getTimeoutMs() != null) entity.setTimeoutMs(req.getTimeoutMs());
        if (req.getEnabled() != null) entity.setEnabled(req.getEnabled());
    }

        private ComputeDefinitionResponse toResponse(ComputeDefinition entity) {
        return ComputeDefinitionResponse.builder()
                .id(entity.getId())
                .computeName(entity.getComputeName())
                .description(entity.getDescription())
                .inputSchema(entity.getInputSchema())
                .formula(entity.getFormula())
                .outputType(entity.getOutputType())
                .outputSchema(entity.getOutputSchema())
                .timeoutMs(entity.getTimeoutMs())
                .enabled(entity.getEnabled())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
