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

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrchestrationService {

    private final OrchestrationPOMapper mapper;

    @Transactional
    public OrchestrationResponse create(String ontologyId, CreateOrchestrationRequest request, String userId) {
        log.info("Creating Orchestration: ontologyId={}, name={}", ontologyId, request.getName());
        Orchestration entity = Orchestration.create(ontologyId);
        mapRequest(request, entity);
        mapper.insert(toPO(entity));
        return toResponse(entity);
    }

    public OrchestrationResponse getById(String id) {
        OrchestrationPO po = mapper.selectById(id);
        if (po == null) return null;
        return toResponse(fromPO(po));
    }

    public List<OrchestrationResponse> listByOntologyId(String ontologyId) {
        return mapper.selectByOntologyId(ontologyId).stream()
                .map(po -> toResponse(fromPO(po)))
                .collect(Collectors.toList());
    }

    @Transactional
    public void delete(String id) {
        if (mapper.selectById(id) != null) mapper.deleteById(id);
    }

    private void mapRequest(CreateOrchestrationRequest req, Orchestration entity) {
        if (req.getName() != null) entity.setName(req.getName());
        if (req.getDescription() != null) entity.setDescription(req.getDescription());
        if (req.getEntryPoints() != null) entity.setEntryPoints(req.getEntryPoints());
    }

        private OrchestrationPO toPO(Orchestration entity) {
        return OrchestrationPO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .entryPoints(entity.getEntryPoints())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

        private Orchestration fromPO(OrchestrationPO po) {
        return Orchestration.builder()
                .id(po.getId())
                .name(po.getName())
                .description(po.getDescription())
                .entryPoints(po.getEntryPoints())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }

        private OrchestrationResponse toResponse(Orchestration entity) {
        return OrchestrationResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .entryPoints(entity.getEntryPoints())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
