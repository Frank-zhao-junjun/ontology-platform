package com.ontology.platform.application.service;

import com.ontology.platform.application.dto.domain.CreateOrchestrationRequest;
import com.ontology.platform.application.dto.domain.OrchestrationResponse;
import com.ontology.platform.domain.entity.Orchestration;
import com.ontology.platform.infrastructure.persistence.OrchestrationPO;
import com.ontology.platform.infrastructure.persistence.OrchestrationPOMapper;
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
public class OrchestrationService {

    private final OrchestrationPOMapper mapper;

    @Transactional
    public OrchestrationResponse create(String ontologyId, CreateOrchestrationRequest request, String userId) {
        log.info("Creating Orchestration: ontologyId={}", ontologyId);
        Orchestration entity = Orchestration.create();
        return toResponse(entity);
    }

    public OrchestrationResponse getById(String id) {
        OrchestrationPO po = mapper.selectById(id);
        if (po == null) return null;
        return toResponse(fromPO(po));
    }

    public List<OrchestrationResponse> listByOntologyId(String ontologyId) {
        List<OrchestrationPO> pos = mapper.selectByOntologyId(ontologyId);
        return pos.stream().map(po -> toResponse(fromPO(po))).collect(Collectors.toList());
    }

    @Transactional
    public void delete(String id) {
        OrchestrationPO po = mapper.selectById(id);
        if (po != null) mapper.deleteById(id);
    }

    private OrchestrationPO toPO(Orchestration entity) {
        return OrchestrationPO.builder().id(entity.getId())
                .createdAt(entity.getCreatedAt()).updatedAt(entity.getUpdatedAt()).build();
    }

    private Orchestration fromPO(OrchestrationPO po) {
        return Orchestration.builder().id(po.getId())
                .createdAt(po.getCreatedAt()).updatedAt(po.getUpdatedAt()).build();
    }

    private OrchestrationResponse toResponse(Orchestration entity) {
        return OrchestrationResponse.builder().id(entity.getId())
                .createdAt(entity.getCreatedAt()).updatedAt(entity.getUpdatedAt()).build();
    }
}
