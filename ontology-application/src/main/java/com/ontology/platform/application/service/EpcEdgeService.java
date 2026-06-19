package com.ontology.platform.application.service;

import com.ontology.platform.application.dto.domain.CreateEpcEdgeRequest;
import com.ontology.platform.application.dto.domain.EpcEdgeResponse;
import com.ontology.platform.domain.entity.EpcEdge;
import com.ontology.platform.infrastructure.persistence.EpcEdgePO;
import com.ontology.platform.infrastructure.persistence.EpcEdgePOMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j @Service @RequiredArgsConstructor
public class EpcEdgeService {
    private final EpcEdgePOMapper mapper;

    @Transactional
    public EpcEdgeResponse create(String ontologyId, CreateEpcEdgeRequest request, String userId) {
        log.info("Creating EpcEdge");
        EpcEdge entity = EpcEdge.create();
        EpcEdgePO po = toPO(entity);
        mapper.insert(po);
        return toResponse(entity);
    }

    public EpcEdgeResponse getById(String id) {
        EpcEdgePO po = mapper.selectById(id);
        return po == null ? null : toResponse(fromPO(po));
    }

    public List<EpcEdgeResponse> list() {
        return mapper.selectList(null).stream()
                .map(po -> toResponse(fromPO(po))).collect(Collectors.toList());
    }

    @Transactional
    public void delete(String id) { mapper.deleteById(id); }

        private EpcEdgePO toPO(EpcEdge entity) {
        return EpcEdgePO.builder()
                .id(entity.getId())
                .chainId(entity.getChainId())
                .sourceNodeId(entity.getSourceNodeId())
                .targetNodeId(entity.getTargetNodeId())
                .edgeType(entity.getEdgeType())
                .label(entity.getLabel())
                .conditionExpr(entity.getConditionExpr())
                .metadata(entity.getMetadata())
                .sortOrder(entity.getSortOrder())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

        private EpcEdge fromPO(EpcEdgePO po) {
        return EpcEdge.builder()
                .id(po.getId())
                .chainId(po.getChainId())
                .sourceNodeId(po.getSourceNodeId())
                .targetNodeId(po.getTargetNodeId())
                .edgeType(po.getEdgeType())
                .label(po.getLabel())
                .conditionExpr(po.getConditionExpr())
                .metadata(po.getMetadata())
                .sortOrder(po.getSortOrder())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }

        private EpcEdgeResponse toResponse(EpcEdge entity) {
        return EpcEdgeResponse.builder()
                .id(entity.getId())
                .chainId(entity.getChainId())
                .sourceNodeId(entity.getSourceNodeId())
                .targetNodeId(entity.getTargetNodeId())
                .edgeType(entity.getEdgeType())
                .label(entity.getLabel())
                .conditionExpr(entity.getConditionExpr())
                .metadata(entity.getMetadata())
                .sortOrder(entity.getSortOrder())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
