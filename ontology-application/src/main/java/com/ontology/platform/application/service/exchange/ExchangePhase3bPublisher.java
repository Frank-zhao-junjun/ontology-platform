package com.ontology.platform.application.service.exchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.domain.dto.imports.OntologyExchangeDocument;
import com.ontology.platform.infrastructure.persistence.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Persists Phase 3b models (organization, metrics, process, metadata) on exchange publish.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ExchangePhase3bPublisher {

    private final DepartmentPOMapper departmentMapper;
    private final PositionEntryPOMapper positionMapper;
    private final BusinessMetricPOMapper metricMapper;
    private final OrchestrationPOMapper orchestrationMapper;
    private final ProcessStepPOMapper processStepMapper;
    private final MetadataTemplatePOMapper metadataTemplateMapper;
    private final ObjectMapper objectMapper;

    /**
     * Persist organizationModel, metricsModel, processModel, and extensions.metadataList.
     *
     * @param ontologyId target ontology (uses metadata.id or project.id as fallback)
     * @param doc        parsed exchange document
     * @return counts per section
     */
    public Map<String, Integer> publish(String ontologyId, OntologyExchangeDocument doc) {
        Map<String, Integer> counts = new HashMap<>();
        if (doc == null || doc.getSpec() == null || doc.getSpec().getProject() == null) {
            return counts;
        }

        var project = doc.getSpec().getProject();
        String effectiveOntologyId = resolveOntologyId(ontologyId, doc);

        counts.put("departments", persistDepartments(effectiveOntologyId, project.getOrganizationModel()));
        counts.put("positions", persistPositions(effectiveOntologyId, project.getOrganizationModel()));
        counts.put("metrics", persistMetrics(effectiveOntologyId, project.getMetricsModel()));
        counts.put("orchestrations", persistOrchestrations(effectiveOntologyId, project.getProcessModel()));
        counts.put("processSteps", persistProcessSteps(effectiveOntologyId, project.getProcessModel()));
        counts.put("metadataTemplates", persistMetadataTemplates(effectiveOntologyId, doc.getSpec().getExtensions()));

        log.info("Phase 3b publish complete: ontologyId={}, counts={}", effectiveOntologyId, counts);
        return counts;
    }

    private String resolveOntologyId(String ontologyId, OntologyExchangeDocument doc) {
        if (ontologyId != null && !ontologyId.isBlank()) return ontologyId;
        if (doc.getMetadata() != null && doc.getMetadata().getId() != null) {
            return doc.getMetadata().getId();
        }
        return doc.getSpec().getProject().getId();
    }

    private int persistDepartments(String ontologyId, OntologyExchangeDocument.OrganizationModel org) {
        if (org == null || org.getDepartments() == null) return 0;
        int count = 0;
        for (var dept : org.getDepartments()) {
            Instant now = Instant.now();
            departmentMapper.insert(DepartmentPO.builder()
                    .id(dept.getId())
                    .ontologyId(ontologyId)
                    .name(dept.getName())
                    .nameEn(dept.getNameEn())
                    .description(dept.getDescription())
                    .parentDepartmentId(dept.getParentDepartmentId())
                    .createdAt(now)
                    .updatedAt(now)
                    .build());
            count++;
        }
        return count;
    }

    private int persistPositions(String ontologyId, OntologyExchangeDocument.OrganizationModel org) {
        if (org == null || org.getPositions() == null) return 0;
        int count = 0;
        for (var pos : org.getPositions()) {
            Instant now = Instant.now();
            positionMapper.insert(PositionEntryPO.builder()
                    .id(pos.getId())
                    .ontologyId(ontologyId)
                    .name(pos.getName())
                    .nameEn(pos.getNameEn())
                    .description(pos.getDescription())
                    .departmentId(pos.getDepartmentId())
                    .responsibilities(toJson(pos.getResponsibilities()))
                    .createdAt(now)
                    .updatedAt(now)
                    .build());
            count++;
        }
        return count;
    }

    private int persistMetrics(String ontologyId, OntologyExchangeDocument.MetricsModel metrics) {
        if (metrics == null || metrics.getMetrics() == null) return 0;
        int count = 0;
        for (var m : metrics.getMetrics()) {
            Instant now = Instant.now();
            metricMapper.insert(BusinessMetricPO.builder()
                    .id(m.getId())
                    .ontologyId(ontologyId)
                    .name(m.getName())
                    .nameEn(m.getNameEn())
                    .description(m.getDescription())
                    .formula(m.getFormula())
                    .dataSourceRef(m.getDataSourceRef())
                    .period(m.getPeriod())
                    .targetEntity(m.getTargetEntity())
                    .createdAt(now)
                    .updatedAt(now)
                    .build());
            count++;
        }
        return count;
    }

    private int persistOrchestrations(String ontologyId, OntologyExchangeDocument.ProcessModel process) {
        if (process == null || process.getOrchestrations() == null) return 0;
        int count = 0;
        for (var orch : process.getOrchestrations()) {
            Instant now = Instant.now();
            orchestrationMapper.insert(OrchestrationPO.builder()
                    .id(orch.getId())
                    .ontologyId(ontologyId)
                    .name(orch.getName())
                    .description(orch.getDescription())
                    .entryPoints(toJson(orch.getEntryPoints()))
                    .createdAt(now)
                    .updatedAt(now)
                    .build());
            count++;
        }
        return count;
    }

    private int persistProcessSteps(String ontologyId, OntologyExchangeDocument.ProcessModel process) {
        if (process == null || process.getOrchestrations() == null) return 0;
        int count = 0;
        int sortOrder = 0;
        for (var orch : process.getOrchestrations()) {
            if (orch.getSteps() == null) continue;
            for (var step : orch.getSteps()) {
                Instant now = Instant.now();
                processStepMapper.insert(ProcessStepPO.builder()
                        .id(step.getId())
                        .ontologyId(ontologyId)
                        .orchestrationId(orch.getId())
                        .name(step.getName())
                        .stepType(step.getType())
                        .description(step.getDescription())
                        .sortOrder(sortOrder++)
                        .createdAt(now)
                        .updatedAt(now)
                        .build());
                count++;
            }
        }
        return count;
    }

    private int persistMetadataTemplates(String ontologyId, OntologyExchangeDocument.Extensions extensions) {
        if (extensions == null || extensions.getMetadataList() == null) return 0;
        int count = 0;
        for (var meta : extensions.getMetadataList()) {
            Instant now = Instant.now();
            metadataTemplateMapper.insert(MetadataTemplatePO.builder()
                    .id(meta.getId())
                    .ontologyId(ontologyId)
                    .name(meta.getName())
                    .nameEn(meta.getNameEn())
                    .description(meta.getDescription())
                    .domain(meta.getDomain())
                    .templateType(meta.getType())
                    .createdAt(now)
                    .updatedAt(now)
                    .build());
            count++;
        }
        return count;
    }

    private String toJson(Object value) {
        if (value == null) return "[]";
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }
}
