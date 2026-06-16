package com.ontology.platform.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.application.dto.domain.CreateReportDefinitionRequest;
import com.ontology.platform.application.dto.domain.ReportDefinitionResponse;
import com.ontology.platform.domain.entity.ReportDefinition;
import com.ontology.platform.infrastructure.persistence.ReportDefinitionPO;
import com.ontology.platform.infrastructure.persistence.ReportDefinitionPOMapper;
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
public class ReportDefinitionService {

    private final ReportDefinitionPOMapper mapper;
    private final ObjectMapper objectMapper;

    @Transactional
    public ReportDefinitionResponse create(String ontologyId, CreateReportDefinitionRequest request, String userId) {
        log.info("Creating ReportDefinition: ontologyId={}, name={}", ontologyId, request.getReportDefinitionName());
        
        ReportDefinition entity = ReportDefinition.create(ontologyId, request.getReportDefinitionName(), null);
        // Map request fields to entity
        mapRequestToEntity(request, entity);
        
        ReportDefinitionPO po = toPO(entity);
        mapper.insert(po);
        
        return toResponse(entity);
    }

    public ReportDefinitionResponse getById(String id) {
        ReportDefinitionPO po = mapper.selectById(id);
        if (po == null) return null;
        return toResponse(fromPO(po));
    }

    public List<ReportDefinitionResponse> listByOntologyId(String ontologyId) {
        List<ReportDefinitionPO> pos = mapper.selectByOntologyId(ontologyId);
        return pos.stream().map(po -> toResponse(fromPO(po))).collect(Collectors.toList());
    }

    @Transactional
    public ReportDefinitionResponse update(String id, CreateReportDefinitionRequest request) {
        ReportDefinitionPO po = mapper.selectById(id);
        if (po == null) throw new RuntimeException("ReportDefinition not found: " + id);
        
        ReportDefinition entity = fromPO(po);
        mapRequestToEntity(request, entity);
        entity.setUpdatedAt(Instant.now());
        
        mapper.updateById(toPO(entity));
        return toResponse(entity);
    }

    @Transactional
    public void delete(String id) {
        ReportDefinitionPO po = mapper.selectById(id);
        if (po != null) {
            po.setDeleted(true);
            po.setUpdatedAt(Instant.now());
            mapper.updateById(po);
        }
    }

    // ── mapping helpers ──

        private void mapRequestToEntity(CreateReportDefinitionRequest req, ReportDefinition entity) {
        if (req.getReportName() != null) entity.setReportName(req.getReportName());
        if (req.getDescription() != null) entity.setDescription(req.getDescription());
        if (req.getReportFormat() != null) entity.setReportFormat(req.getReportFormat());
        if (req.getFields() != null) entity.setFields(req.getFields());
        if (req.getDataSource() != null) entity.setDataSource(req.getDataSource());
        if (req.getQueryId() != null) entity.setQueryId(req.getQueryId());
        if (req.getScheduleCron() != null) entity.setScheduleCron(req.getScheduleCron());
        if (req.getEnabled() != null) entity.setEnabled(req.getEnabled());
        if (req.getConfig() != null) entity.setConfig(req.getConfig());
    }

        private ReportDefinitionPO toPO(ReportDefinition entity) {
        return ReportDefinitionPO.builder()
                .id(entity.getId())
                .ontologyId(entity.getOntologyId())
                .reportName(entity.getReportName())
                .description(entity.getDescription())
                .reportFormat(entity.getReportFormat())
                .fields(entity.getFields())
                .dataSource(entity.getDataSource())
                .queryId(entity.getQueryId())
                .scheduleCron(entity.getScheduleCron())
                .enabled(entity.getEnabled())
                .config(entity.getConfig())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .deleted(entity.getDeleted())
                .build();
    }

        private ReportDefinition fromPO(ReportDefinitionPO po) {
        return ReportDefinition.builder()
                .id(po.getId())
                .ontologyId(po.getOntologyId())
                .reportName(po.getReportName())
                .description(po.getDescription())
                .reportFormat(po.getReportFormat())
                .fields(po.getFields())
                .dataSource(po.getDataSource())
                .queryId(po.getQueryId())
                .scheduleCron(po.getScheduleCron())
                .enabled(po.getEnabled())
                .config(po.getConfig())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .deleted(po.getDeleted())
                .build();
    }

        private ReportDefinitionResponse toResponse(ReportDefinition entity) {
        return ReportDefinitionResponse.builder()
                .id(entity.getId())
                .reportName(entity.getReportName())
                .description(entity.getDescription())
                .reportFormat(entity.getReportFormat())
                .fields(entity.getFields())
                .dataSource(entity.getDataSource())
                .queryId(entity.getQueryId())
                .scheduleCron(entity.getScheduleCron())
                .enabled(entity.getEnabled())
                .config(entity.getConfig())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
