package com.ontology.platform.application.service;

import com.ontology.platform.application.dto.domain.CreateProcessStepRequest;
import com.ontology.platform.application.dto.domain.ProcessStepResponse;
import com.ontology.platform.domain.entity.ProcessStep;
import com.ontology.platform.infrastructure.persistence.ProcessStepPO;
import com.ontology.platform.infrastructure.persistence.ProcessStepPOMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessStepService {

    private final ProcessStepPOMapper mapper;

    @Transactional
    public ProcessStepResponse create(String ontologyId, CreateProcessStepRequest request, String userId) {
        log.info("Creating ProcessStep: ontologyId={}, name={}", ontologyId, request.getName());
        ProcessStep entity = ProcessStep.create(ontologyId);
        mapRequest(request, entity);
        mapper.insert(toPO(entity));
        return toResponse(entity);
    }

    public ProcessStepResponse getById(String id) {
        ProcessStepPO po = mapper.selectById(id);
        if (po == null) return null;
        return toResponse(fromPO(po));
    }

    public List<ProcessStepResponse> listByOntologyId(String ontologyId) {
        return mapper.selectByOntologyId(ontologyId).stream()
                .map(po -> toResponse(fromPO(po)))
                .collect(Collectors.toList());
    }

    @Transactional
    public void delete(String id) {
        if (mapper.selectById(id) != null) mapper.deleteById(id);
    }

    private void mapRequest(CreateProcessStepRequest req, ProcessStep entity) {
        if (req.getOrchestrationId() != null) entity.setOrchestrationId(req.getOrchestrationId());
        if (req.getName() != null) entity.setName(req.getName());
        if (req.getStepType() != null) entity.setStepType(req.getStepType());
        if (req.getDescription() != null) entity.setDescription(req.getDescription());
        if (req.getSortOrder() != null) entity.setSortOrder(req.getSortOrder());
        if (req.getConfig() != null) entity.setConfig(req.getConfig());
    }

    private ProcessStepPO toPO(ProcessStep entity) {
        return ProcessStepPO.builder()
                .id(entity.getId())
                .ontologyId(entity.getOntologyId())
                .orchestrationId(entity.getOrchestrationId())
                .name(entity.getName())
                .stepType(entity.getStepType())
                .description(entity.getDescription())
                .sortOrder(entity.getSortOrder())
                .config(entity.getConfig())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private ProcessStep fromPO(ProcessStepPO po) {
        return ProcessStep.builder()
                .id(po.getId())
                .ontologyId(po.getOntologyId())
                .orchestrationId(po.getOrchestrationId())
                .name(po.getName())
                .stepType(po.getStepType())
                .description(po.getDescription())
                .sortOrder(po.getSortOrder())
                .config(po.getConfig())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }

    private ProcessStepResponse toResponse(ProcessStep entity) {
        return ProcessStepResponse.builder()
                .id(entity.getId())
                .orchestrationId(entity.getOrchestrationId())
                .name(entity.getName())
                .stepType(entity.getStepType())
                .description(entity.getDescription())
                .sortOrder(entity.getSortOrder())
                .config(entity.getConfig())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
