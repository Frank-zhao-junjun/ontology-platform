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

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessStepService {

    private final ProcessStepPOMapper mapper;

    @Transactional
    public ProcessStepResponse create(String ontologyId, CreateProcessStepRequest request, String userId) {
        log.info("Creating ProcessStep: ontologyId={}", ontologyId);
        ProcessStep entity = ProcessStep.create();
        return toResponse(entity);
    }

    public ProcessStepResponse getById(String id) {
        ProcessStepPO po = mapper.selectById(id);
        if (po == null) return null;
        return toResponse(fromPO(po));
    }

    public List<ProcessStepResponse> listByOntologyId(String ontologyId) {
        List<ProcessStepPO> pos = mapper.selectByOntologyId(ontologyId);
        return pos.stream().map(po -> toResponse(fromPO(po))).collect(Collectors.toList());
    }

    @Transactional
    public void delete(String id) {
        ProcessStepPO po = mapper.selectById(id);
        if (po != null) mapper.deleteById(id);
    }

    private ProcessStepPO toPO(ProcessStep entity) {
        return ProcessStepPO.builder().id(entity.getId())
                .createdAt(entity.getCreatedAt()).updatedAt(entity.getUpdatedAt()).build();
    }

    private ProcessStep fromPO(ProcessStepPO po) {
        return ProcessStep.builder().id(po.getId())
                .createdAt(po.getCreatedAt()).updatedAt(po.getUpdatedAt()).build();
    }

    private ProcessStepResponse toResponse(ProcessStep entity) {
        return ProcessStepResponse.builder().id(entity.getId())
                .createdAt(entity.getCreatedAt()).updatedAt(entity.getUpdatedAt()).build();
    }
}
