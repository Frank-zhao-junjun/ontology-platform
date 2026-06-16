package com.ontology.platform.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.application.dto.domain.CreateQueryDefinitionRequest;
import com.ontology.platform.application.dto.domain.QueryDefinitionResponse;
import com.ontology.platform.domain.entity.QueryDefinition;
import com.ontology.platform.domain.repository.QueryDefinitionRepository;
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
public class QueryDefinitionService {

    private final QueryDefinitionRepository repository;
    private final ObjectMapper objectMapper;

    @Transactional
    public QueryDefinitionResponse create(String ontologyId, CreateQueryDefinitionRequest request, String userId) {
        log.info("Creating QueryDefinition: ontologyId={}, name={}", ontologyId, request.getQueryName());
        
        QueryDefinition entity = QueryDefinition.create(ontologyId, request.getQueryName(), request.getQueryTemplate());
        // Map request fields to entity
        mapRequestToEntity(request, entity);
        
        entity = repository.save(entity);
        
        return toResponse(entity);
    }

    public QueryDefinitionResponse getById(String id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElse(null);
    }

    public List<QueryDefinitionResponse> listByOntologyId(String ontologyId) {
        return repository.findByOntologyId(ontologyId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public QueryDefinitionResponse update(String id, CreateQueryDefinitionRequest request) {
        QueryDefinition entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("QueryDefinition not found: " + id));
        
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

        private void mapRequestToEntity(CreateQueryDefinitionRequest req, QueryDefinition entity) {
        if (req.getQueryName() != null) entity.setQueryName(req.getQueryName());
        if (req.getDescription() != null) entity.setDescription(req.getDescription());
        if (req.getQueryType() != null) entity.setQueryType(req.getQueryType());
        if (req.getQueryTemplate() != null) entity.setQueryTemplate(req.getQueryTemplate());
        if (req.getParameters() != null) entity.setParameters(req.getParameters());
        if (req.getResultSchema() != null) entity.setResultSchema(req.getResultSchema());
        if (req.getTimeoutMs() != null) entity.setTimeoutMs(req.getTimeoutMs());
        if (req.getEnabled() != null) entity.setEnabled(req.getEnabled());
    }

        private QueryDefinitionResponse toResponse(QueryDefinition entity) {
        return QueryDefinitionResponse.builder()
                .id(entity.getId())
                .queryName(entity.getQueryName())
                .description(entity.getDescription())
                .queryType(entity.getQueryType())
                .queryTemplate(entity.getQueryTemplate())
                .parameters(entity.getParameters())
                .resultSchema(entity.getResultSchema())
                .timeoutMs(entity.getTimeoutMs())
                .enabled(entity.getEnabled())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
