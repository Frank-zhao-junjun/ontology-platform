package com.ontology.platform.application.service;

import com.ontology.platform.application.dto.domain.CreateSemanticRelationRequest;
import com.ontology.platform.application.dto.domain.SemanticRelationResponse;
import com.ontology.platform.domain.entity.SemanticRelation;
import com.ontology.platform.infrastructure.persistence.SemanticRelationPO;
import com.ontology.platform.infrastructure.persistence.SemanticRelationPOMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j @Service @RequiredArgsConstructor
public class SemanticRelationService {
    private final SemanticRelationPOMapper mapper;

    @Transactional
    public SemanticRelationResponse create(String ontologyId, CreateSemanticRelationRequest request, String userId) {
        log.info("Creating SemanticRelation");
        SemanticRelation entity = SemanticRelation.create();
        mapRequest(entity, request);
        SemanticRelationPO po = toPO(entity);
        mapper.insert(po);
        return toResponse(entity);
    }

    public SemanticRelationResponse getById(String id) {
        SemanticRelationPO po = mapper.selectById(id);
        return po == null ? null : toResponse(fromPO(po));
    }

    public List<SemanticRelationResponse> list() {
        return mapper.selectList(null).stream()
                .map(po -> toResponse(fromPO(po))).collect(Collectors.toList());
    }

    @Transactional
    public void delete(String id) { mapper.deleteById(id); }

    private void mapRequest(SemanticRelation entity, CreateSemanticRelationRequest request) {
        entity.setSourceTermId(request.getSourceTermId());
        entity.setTargetTermId(request.getTargetTermId());
        entity.setRelationType(request.getRelationType());
        entity.setDescription(request.getDescription());
    }

        private SemanticRelationPO toPO(SemanticRelation entity) {
        return SemanticRelationPO.builder()
                .id(entity.getId())
                .sourceTermId(entity.getSourceTermId())
                .targetTermId(entity.getTargetTermId())
                .relationType(entity.getRelationType())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                
                .build();
    }

        private SemanticRelation fromPO(SemanticRelationPO po) {
        return SemanticRelation.builder()
                .id(po.getId())
                .sourceTermId(po.getSourceTermId())
                .targetTermId(po.getTargetTermId())
                .relationType(po.getRelationType())
                .description(po.getDescription())
                .createdAt(po.getCreatedAt())
                
                .build();
    }

        private SemanticRelationResponse toResponse(SemanticRelation entity) {
        return SemanticRelationResponse.builder()
                .id(entity.getId())
                .sourceTermId(entity.getSourceTermId())
                .targetTermId(entity.getTargetTermId())
                .relationType(entity.getRelationType())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                
                .build();
    }
}
