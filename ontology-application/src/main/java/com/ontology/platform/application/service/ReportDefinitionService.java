package com.ontology.platform.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.application.dto.domain.CreateReportDefinitionRequest;
import com.ontology.platform.application.dto.domain.ReportDefinitionResponse;
import com.ontology.platform.domain.entity.ReportDefinition;
import com.ontology.platform.domain.repository.ReportDefinitionRepository;
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

    private final ReportDefinitionRepository repository;
    private final ObjectMapper objectMapper;

    @Transactional
    public ReportDefinitionResponse create(String ontologyId, CreateReportDefinitionRequest request, String userId) {
        log.info("Creating ReportDefinition: ontologyId={}, name={}", ontologyId, request.getReportName());
        
        ReportDefinition entity = ReportDefinition.create(ontologyId, request.getReportName(), request.getReportFormat());
        // Map request fields to entity
        mapRequestToEntity(request, entity);
        
        entity = repository.save(entity);
        
        return toResponse(entity);
    }

    public ReportDefinitionResponse getById(String id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElse(null);
    }

    public List<ReportDefinitionResponse> listByOntologyId(String ontologyId) {
        return repository.findByOntologyId(ontologyId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReportDefinitionResponse update(String id, CreateReportDefinitionRequest request) {
        ReportDefinition entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("ReportDefinition not found: " + id));
        
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
