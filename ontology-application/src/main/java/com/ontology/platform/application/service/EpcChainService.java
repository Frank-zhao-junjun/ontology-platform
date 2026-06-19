package com.ontology.platform.application.service;

import com.ontology.platform.application.dto.domain.CreateEpcChainRequest;
import com.ontology.platform.application.dto.domain.EpcChainResponse;
import com.ontology.platform.domain.entity.EpcChain;
import com.ontology.platform.infrastructure.persistence.EpcChainPO;
import com.ontology.platform.infrastructure.persistence.EpcChainPOMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j @Service @RequiredArgsConstructor
public class EpcChainService {
    private final EpcChainPOMapper mapper;

    @Transactional
    public EpcChainResponse create(String ontologyId, CreateEpcChainRequest request, String userId) {
        log.info("Creating EpcChain: name={}", request.getName());
        EpcChain entity = EpcChain.create();
        mapRequest(request, entity);
        EpcChainPO po = toPO(entity);
        mapper.insert(po);
        return toResponse(entity);
    }

    public EpcChainResponse getById(String id) {
        EpcChainPO po = mapper.selectById(id);
        return po == null ? null : toResponse(fromPO(po));
    }

    public List<EpcChainResponse> list() {
        return mapper.selectList(null).stream()
                .map(po -> toResponse(fromPO(po))).collect(Collectors.toList());
    }

    @Transactional
    public void delete(String id) { mapper.deleteById(id); }

    private void mapRequest(CreateEpcChainRequest req, EpcChain entity) {
        if (req.getName() != null) entity.setName(req.getName());
        if (req.getAggregateRootId() != null) entity.setAggregateRootId(req.getAggregateRootId());
        if (req.getDescription() != null) entity.setDescription(req.getDescription());
        if (req.getChainType() != null) entity.setChainType(req.getChainType());
        entity.setIsActive(req.getIsActive() != null ? req.getIsActive() : true);
    }

    private EpcChainPO toPO(EpcChain entity) {
        return EpcChainPO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .aggregateRootId(entity.getAggregateRootId())
                .description(entity.getDescription())
                .chainType(entity.getChainType())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

        private EpcChain fromPO(EpcChainPO po) {
        return EpcChain.builder()
                .id(po.getId())
                .name(po.getName())
                .aggregateRootId(po.getAggregateRootId())
                .description(po.getDescription())
                .chainType(po.getChainType())
                .isActive(po.getIsActive())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }

        private EpcChainResponse toResponse(EpcChain entity) {
        return EpcChainResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .aggregateRootId(entity.getAggregateRootId())
                .description(entity.getDescription())
                .chainType(entity.getChainType())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
