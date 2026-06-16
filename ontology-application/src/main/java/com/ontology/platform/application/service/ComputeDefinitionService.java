package com.ontology.platform.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.application.dto.domain.CreateComputeDefinitionRequest;
import com.ontology.platform.application.dto.domain.ComputeDefinitionResponse;
import com.ontology.platform.domain.entity.ComputeDefinition;
import com.ontology.platform.infrastructure.persistence.ComputeDefinitionPO;
import com.ontology.platform.infrastructure.persistence.ComputeDefinitionPOMapper;
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

    private final ComputeDefinitionPOMapper mapper;
    private final ObjectMapper objectMapper;

    @Transactional
    public ComputeDefinitionResponse create(String ontologyId, CreateComputeDefinitionRequest request, String userId) {
        log.info("Creating ComputeDefinition: ontologyId={}, name={}", ontologyId, request.getComputeDefinitionName());
        
        ComputeDefinition entity = ComputeDefinition.create(ontologyId, request.getComputeDefinitionName(), null);
        // Map request fields to entity
        mapRequestToEntity(request, entity);
        
        ComputeDefinitionPO po = toPO(entity);
        mapper.insert(po);
        
        return toResponse(entity);
    }

    public ComputeDefinitionResponse getById(String id) {
        ComputeDefinitionPO po = mapper.selectById(id);
        if (po == null) return null;
        return toResponse(fromPO(po));
    }

    public List<ComputeDefinitionResponse> listByOntologyId(String ontologyId) {
        List<ComputeDefinitionPO> pos = mapper.selectByOntologyId(ontologyId);
        return pos.stream().map(po -> toResponse(fromPO(po))).collect(Collectors.toList());
    }

    @Transactional
    public ComputeDefinitionResponse update(String id, CreateComputeDefinitionRequest request) {
        ComputeDefinitionPO po = mapper.selectById(id);
        if (po == null) throw new RuntimeException("ComputeDefinition not found: " + id);
        
        ComputeDefinition entity = fromPO(po);
        mapRequestToEntity(request, entity);
        entity.setUpdatedAt(Instant.now());
        
        mapper.updateById(toPO(entity));
        return toResponse(entity);
    }

    @Transactional
    public void delete(String id) {
        ComputeDefinitionPO po = mapper.selectById(id);
        if (po != null) {
            po.setDeleted(true);
            po.setUpdatedAt(Instant.now());
            mapper.updateById(po);
        }
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

        private ComputeDefinitionPO toPO(ComputeDefinition entity) {
        return ComputeDefinitionPO.builder()
                .id(entity.getId())
                .ontologyId(entity.getOntologyId())
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
                .deleted(entity.getDeleted())
                .build();
    }

        private ComputeDefinition fromPO(ComputeDefinitionPO po) {
        return ComputeDefinition.builder()
                .id(po.getId())
                .ontologyId(po.getOntologyId())
                .computeName(po.getComputeName())
                .description(po.getDescription())
                .inputSchema(po.getInputSchema())
                .formula(po.getFormula())
                .outputType(po.getOutputType())
                .outputSchema(po.getOutputSchema())
                .timeoutMs(po.getTimeoutMs())
                .enabled(po.getEnabled())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .deleted(po.getDeleted())
                .build();
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
