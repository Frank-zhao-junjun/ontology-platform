package com.ontology.platform.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.application.dto.domain.CreateApiDefinitionRequest;
import com.ontology.platform.application.dto.domain.ApiDefinitionResponse;
import com.ontology.platform.domain.entity.ApiDefinition;
import com.ontology.platform.infrastructure.persistence.ApiDefinitionPO;
import com.ontology.platform.infrastructure.persistence.ApiDefinitionPOMapper;
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

    private final ApiDefinitionPOMapper mapper;
    private final ObjectMapper objectMapper;

    @Transactional
    public ApiDefinitionResponse create(String ontologyId, CreateApiDefinitionRequest request, String userId) {
        log.info("Creating ApiDefinition: ontologyId={}, name={}", ontologyId, request.getApiDefinitionName());
        
        ApiDefinition entity = ApiDefinition.create(ontologyId, request.getApiDefinitionName(), null);
        // Map request fields to entity
        mapRequestToEntity(request, entity);
        
        ApiDefinitionPO po = toPO(entity);
        mapper.insert(po);
        
        return toResponse(entity);
    }

    public ApiDefinitionResponse getById(String id) {
        ApiDefinitionPO po = mapper.selectById(id);
        if (po == null) return null;
        return toResponse(fromPO(po));
    }

    public List<ApiDefinitionResponse> listByOntologyId(String ontologyId) {
        List<ApiDefinitionPO> pos = mapper.selectByOntologyId(ontologyId);
        return pos.stream().map(po -> toResponse(fromPO(po))).collect(Collectors.toList());
    }

    @Transactional
    public ApiDefinitionResponse update(String id, CreateApiDefinitionRequest request) {
        ApiDefinitionPO po = mapper.selectById(id);
        if (po == null) throw new RuntimeException("ApiDefinition not found: " + id);
        
        ApiDefinition entity = fromPO(po);
        mapRequestToEntity(request, entity);
        entity.setUpdatedAt(Instant.now());
        
        mapper.updateById(toPO(entity));
        return toResponse(entity);
    }

    @Transactional
    public void delete(String id) {
        ApiDefinitionPO po = mapper.selectById(id);
        if (po != null) {
            po.setDeleted(true);
            po.setUpdatedAt(Instant.now());
            mapper.updateById(po);
        }
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

        private ApiDefinitionPO toPO(ApiDefinition entity) {
        return ApiDefinitionPO.builder()
                .id(entity.getId())
                .ontologyId(entity.getOntologyId())
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
                .deleted(entity.getDeleted())
                .build();
    }

        private ApiDefinition fromPO(ApiDefinitionPO po) {
        return ApiDefinition.builder()
                .id(po.getId())
                .ontologyId(po.getOntologyId())
                .apiName(po.getApiName())
                .description(po.getDescription())
                .url(po.getUrl())
                .httpMethod(po.getHttpMethod())
                .requestSchema(po.getRequestSchema())
                .responseSchema(po.getResponseSchema())
                .authType(po.getAuthType())
                .rateLimit(po.getRateLimit())
                .timeoutMs(po.getTimeoutMs())
                .enabled(po.getEnabled())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .deleted(po.getDeleted())
                .build();
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
