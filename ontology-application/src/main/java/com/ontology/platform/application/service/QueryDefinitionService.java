package com.ontology.platform.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.application.dto.domain.CreateQueryDefinitionRequest;
import com.ontology.platform.application.dto.domain.QueryDefinitionResponse;
import com.ontology.platform.domain.entity.QueryDefinition;
import com.ontology.platform.infrastructure.persistence.QueryDefinitionPO;
import com.ontology.platform.infrastructure.persistence.QueryDefinitionPOMapper;
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

    private final QueryDefinitionPOMapper mapper;
    private final ObjectMapper objectMapper;

    @Transactional
    public QueryDefinitionResponse create(String ontologyId, CreateQueryDefinitionRequest request, String userId) {
        log.info("Creating QueryDefinition: ontologyId={}, name={}", ontologyId, request.getQueryDefinitionName());
        
        QueryDefinition entity = QueryDefinition.create(ontologyId, request.getQueryDefinitionName(), null);
        // Map request fields to entity
        mapRequestToEntity(request, entity);
        
        QueryDefinitionPO po = toPO(entity);
        mapper.insert(po);
        
        return toResponse(entity);
    }

    public QueryDefinitionResponse getById(String id) {
        QueryDefinitionPO po = mapper.selectById(id);
        if (po == null) return null;
        return toResponse(fromPO(po));
    }

    public List<QueryDefinitionResponse> listByOntologyId(String ontologyId) {
        List<QueryDefinitionPO> pos = mapper.selectByOntologyId(ontologyId);
        return pos.stream().map(po -> toResponse(fromPO(po))).collect(Collectors.toList());
    }

    @Transactional
    public QueryDefinitionResponse update(String id, CreateQueryDefinitionRequest request) {
        QueryDefinitionPO po = mapper.selectById(id);
        if (po == null) throw new RuntimeException("QueryDefinition not found: " + id);
        
        QueryDefinition entity = fromPO(po);
        mapRequestToEntity(request, entity);
        entity.setUpdatedAt(Instant.now());
        
        mapper.updateById(toPO(entity));
        return toResponse(entity);
    }

    @Transactional
    public void delete(String id) {
        QueryDefinitionPO po = mapper.selectById(id);
        if (po != null) {
            po.setDeleted(true);
            po.setUpdatedAt(Instant.now());
            mapper.updateById(po);
        }
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

        private QueryDefinitionPO toPO(QueryDefinition entity) {
        return QueryDefinitionPO.builder()
                .id(entity.getId())
                .ontologyId(entity.getOntologyId())
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
                .deleted(entity.getDeleted())
                .build();
    }

        private QueryDefinition fromPO(QueryDefinitionPO po) {
        return QueryDefinition.builder()
                .id(po.getId())
                .ontologyId(po.getOntologyId())
                .queryName(po.getQueryName())
                .description(po.getDescription())
                .queryType(po.getQueryType())
                .queryTemplate(po.getQueryTemplate())
                .parameters(po.getParameters())
                .resultSchema(po.getResultSchema())
                .timeoutMs(po.getTimeoutMs())
                .enabled(po.getEnabled())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .deleted(po.getDeleted())
                .build();
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
