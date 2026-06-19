package com.ontology.platform.application.service;

import com.ontology.platform.application.dto.domain.CreateEpcNodeRequest;
import com.ontology.platform.application.dto.domain.EpcNodeResponse;
import com.ontology.platform.domain.entity.EpcNode;
import com.ontology.platform.infrastructure.persistence.EpcNodePO;
import com.ontology.platform.infrastructure.persistence.EpcNodePOMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j @Service @RequiredArgsConstructor
public class EpcNodeService {
    private final EpcNodePOMapper mapper;

    @Transactional
    public EpcNodeResponse create(String ontologyId, CreateEpcNodeRequest request, String userId) {
        log.info("Creating EpcNode");
        EpcNode entity = EpcNode.create();
        EpcNodePO po = toPO(entity);
        mapper.insert(po);
        return toResponse(entity);
    }

    public EpcNodeResponse getById(String id) {
        EpcNodePO po = mapper.selectById(id);
        return po == null ? null : toResponse(fromPO(po));
    }

    public List<EpcNodeResponse> list() {
        return mapper.selectList(null).stream()
                .map(po -> toResponse(fromPO(po))).collect(Collectors.toList());
    }

    @Transactional
    public void delete(String id) { mapper.deleteById(id); }

        private EpcNodePO toPO(EpcNode entity) {
        return EpcNodePO.builder()
                .id(entity.getId())
                .chainId(entity.getChainId())
                .nodeType(entity.getNodeType())
                .name(entity.getName())
                .description(entity.getDescription())
                .refType(entity.getRefType())
                .refId(entity.getRefId())
                .metadata(entity.getMetadata())
                .sortOrder(entity.getSortOrder())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

        private EpcNode fromPO(EpcNodePO po) {
        return EpcNode.builder()
                .id(po.getId())
                .chainId(po.getChainId())
                .nodeType(po.getNodeType())
                .name(po.getName())
                .description(po.getDescription())
                .refType(po.getRefType())
                .refId(po.getRefId())
                .metadata(po.getMetadata())
                .sortOrder(po.getSortOrder())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }

        private EpcNodeResponse toResponse(EpcNode entity) {
        return EpcNodeResponse.builder()
                .id(entity.getId())
                .chainId(entity.getChainId())
                .nodeType(entity.getNodeType())
                .name(entity.getName())
                .description(entity.getDescription())
                .refType(entity.getRefType())
                .refId(entity.getRefId())
                .metadata(entity.getMetadata())
                .sortOrder(entity.getSortOrder())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
