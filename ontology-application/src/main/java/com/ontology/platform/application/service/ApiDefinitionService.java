package com.ontology.platform.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.application.dto.domain.CreateApiDefinitionRequest;
import com.ontology.platform.application.dto.domain.ApiDefinitionResponse;
import com.ontology.platform.domain.entity.ApiDefinition;
import com.ontology.platform.domain.repository.ApiDefinitionRepository;
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
public class ApiDefinitionService {

    private final ApiDefinitionRepository repository;
    private final ObjectMapper objectMapper;

    @Transactional
    public ApiDefinitionResponse create(String ontologyId, CreateApiDefinitionRequest request, String userId) {
        log.info("Creating ApiDefinition: ontologyId={}, name={}", ontologyId, request.getApiName());
        
        ApiDefinition entity = ApiDefinition.create(ontologyId, request.getApiName(), request.getUrl(), request.getHttpMethod());
        // Map request fields to entity
        mapRequestToEntity(request, entity);
        
        entity = repository.save(entity);
        
        return toResponse(entity);
    }

    public ApiDefinitionResponse getById(String id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElse(null);
    }

    public List<ApiDefinitionResponse> listByOntologyId(String ontologyId) {
        return repository.findByOntologyId(ontologyId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ApiDefinitionResponse update(String id, CreateApiDefinitionRequest request) {
        ApiDefinition entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("ApiDefinition not found: " + id));
        
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

        private void mapRequestToEntity(CreateApiDefinitionRequest req, ApiDefinition entity) {
        if (req.getApiName() != null) entity.setApiName(req.getApiName());
        if (req.getDescription() != null) entity.setDescription(req.getDescription());
        if (req.getUrl() != null) entity.setUrl(req.getUrl());
        if (req.getHttpMethod() != null) entity.setHttpMethod(req.getHttpMethod());
        if (req.getRequestSchema() != null) entity.setRequestSchema(req.getRequestSchema());
        if (req.getResponseSchema() != null) entity.setResponseSchema(req.getResponseSchema());
        if (req.getAuthType() != null) entity.setAuthType(req.getAuthType());
        if (req.getRateLimit() != null) entity.setRateLimit(req.getRateLimit());
        if (req.getTimeoutMs() != null) entity.setTimeoutMs(req.getTimeoutMs());
        if (req.getEnabled() != null) entity.setEnabled(req.getEnabled());
    }

        private ApiDefinitionResponse toResponse(ApiDefinition entity) {
        return ApiDefinitionResponse.builder()
                .id(entity.getId())
                .apiName(entity.getApiName())
                .description(entity.getDescription())
                .url(entity.getUrl())
                .httpMethod(entity.getHttpMethod())
                .requestSchema(entity.getRequestSchema())
                .responseSchema(entity.getResponseSchema())
                .authType(entity.getAuthType())
                .rateLimit(entity.getRateLimit())
                .timeoutMs(entity.getTimeoutMs())
                .enabled(entity.getEnabled())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
