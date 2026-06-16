package com.ontology.platform.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.application.dto.domain.CreateProbeDefinitionRequest;
import com.ontology.platform.application.dto.domain.ProbeDefinitionResponse;
import com.ontology.platform.domain.entity.ProbeDefinition;
import com.ontology.platform.infrastructure.persistence.ProbeDefinitionPO;
import com.ontology.platform.infrastructure.persistence.ProbeDefinitionPOMapper;
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

    private final ProbeDefinitionPOMapper mapper;
    private final ObjectMapper objectMapper;

    @Transactional
    public ProbeDefinitionResponse create(String ontologyId, CreateProbeDefinitionRequest request, String userId) {
        log.info("Creating ProbeDefinition: ontologyId={}, name={}", ontologyId, request.getProbeDefinitionName());
        
        ProbeDefinition entity = ProbeDefinition.create(ontologyId, request.getProbeDefinitionName(), null);
        // Map request fields to entity
        mapRequestToEntity(request, entity);
        
        ProbeDefinitionPO po = toPO(entity);
        mapper.insert(po);
        
        return toResponse(entity);
    }

    public ProbeDefinitionResponse getById(String id) {
        ProbeDefinitionPO po = mapper.selectById(id);
        if (po == null) return null;
        return toResponse(fromPO(po));
    }

    public List<ProbeDefinitionResponse> listByOntologyId(String ontologyId) {
        List<ProbeDefinitionPO> pos = mapper.selectByOntologyId(ontologyId);
        return pos.stream().map(po -> toResponse(fromPO(po))).collect(Collectors.toList());
    }

    @Transactional
    public ProbeDefinitionResponse update(String id, CreateProbeDefinitionRequest request) {
        ProbeDefinitionPO po = mapper.selectById(id);
        if (po == null) throw new RuntimeException("ProbeDefinition not found: " + id);
        
        ProbeDefinition entity = fromPO(po);
        mapRequestToEntity(request, entity);
        entity.setUpdatedAt(Instant.now());
        
        mapper.updateById(toPO(entity));
        return toResponse(entity);
    }

    @Transactional
    public void delete(String id) {
        ProbeDefinitionPO po = mapper.selectById(id);
        if (po != null) {
            po.setDeleted(true);
            po.setUpdatedAt(Instant.now());
            mapper.updateById(po);
        }
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

        private ProbeDefinitionPO toPO(ProbeDefinition entity) {
        return ProbeDefinitionPO.builder()
                .id(entity.getId())
                .ontologyId(entity.getOntologyId())
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
                .deleted(entity.getDeleted())
                .build();
    }

        private ProbeDefinition fromPO(ProbeDefinitionPO po) {
        return ProbeDefinition.builder()
                .id(po.getId())
                .ontologyId(po.getOntologyId())
                .probeName(po.getProbeName())
                .description(po.getDescription())
                .target(po.getTarget())
                .probeType(po.getProbeType())
                .frequencySec(po.getFrequencySec())
                .timeoutMs(po.getTimeoutMs())
                .alertCondition(po.getAlertCondition())
                .alertSeverity(po.getAlertSeverity())
                .enabled(po.getEnabled())
                .config(po.getConfig())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .deleted(po.getDeleted())
                .build();
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
