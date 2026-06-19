package com.ontology.platform.application.service;

import com.ontology.platform.application.dto.domain.CreateEpcModelRefRequest;
import com.ontology.platform.application.dto.domain.EpcModelRefResponse;
import com.ontology.platform.domain.entity.EpcModelRef;
import com.ontology.platform.infrastructure.persistence.EpcModelRefPO;
import com.ontology.platform.infrastructure.persistence.EpcModelRefPOMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j @Service @RequiredArgsConstructor
public class EpcModelRefService {
    private final EpcModelRefPOMapper mapper;

    @Transactional
    public EpcModelRefResponse create(String ontologyId, CreateEpcModelRefRequest request, String userId) {
        log.info("Creating EpcModelRef");
        EpcModelRef entity = EpcModelRef.create();
        mapRequest(request, entity);
        EpcModelRefPO po = toPO(entity);
        mapper.insert(po);
        return toResponse(entity);
    }

    public EpcModelRefResponse getById(String id) {
        EpcModelRefPO po = mapper.selectById(id);
        return po == null ? null : toResponse(fromPO(po));
    }

    public List<EpcModelRefResponse> list() {
        return mapper.selectList(null).stream()
                .map(po -> toResponse(fromPO(po))).collect(Collectors.toList());
    }

    @Transactional
    public void delete(String id) { mapper.deleteById(id); }

        private void mapRequest(CreateEpcModelRefRequest req, EpcModelRef entity) {
        entity.setChainId(req.getChainId());
        entity.setModelType(req.getModelType());
        entity.setModelId(req.getModelId());
        entity.setRefMetadata(req.getRefMetadata());
    }

        private EpcModelRefPO toPO(EpcModelRef entity) {
        return EpcModelRefPO.builder()
                .id(entity.getId())
                .chainId(entity.getChainId())
                .modelType(entity.getModelType())
                .modelId(entity.getModelId())
                .refMetadata(entity.getRefMetadata())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

        private EpcModelRef fromPO(EpcModelRefPO po) {
        return EpcModelRef.builder()
                .id(po.getId())
                .chainId(po.getChainId())
                .modelType(po.getModelType())
                .modelId(po.getModelId())
                .refMetadata(po.getRefMetadata())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }

        private EpcModelRefResponse toResponse(EpcModelRef entity) {
        return EpcModelRefResponse.builder()
                .id(entity.getId())
                .chainId(entity.getChainId())
                .modelType(entity.getModelType())
                .modelId(entity.getModelId())
                .refMetadata(entity.getRefMetadata())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
