package com.ontology.platform.application.service;

import com.ontology.platform.application.dto.domain.CreateSemanticFieldMappingRequest;
import com.ontology.platform.application.dto.domain.SemanticFieldMappingResponse;
import com.ontology.platform.domain.entity.SemanticFieldMapping;
import com.ontology.platform.infrastructure.persistence.SemanticFieldMappingPO;
import com.ontology.platform.infrastructure.persistence.SemanticFieldMappingPOMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j @Service @RequiredArgsConstructor
public class SemanticFieldMappingService {
    private final SemanticFieldMappingPOMapper mapper;

    @Transactional
    public SemanticFieldMappingResponse create(String ontologyId, CreateSemanticFieldMappingRequest request, String userId) {
        log.info("Creating SemanticFieldMapping");
        SemanticFieldMapping entity = SemanticFieldMapping.create();
        mapRequest(request, entity);
        SemanticFieldMappingPO po = toPO(entity);
        mapper.insert(po);
        return toResponse(entity);
    }

    public SemanticFieldMappingResponse getById(String id) {
        SemanticFieldMappingPO po = mapper.selectById(id);
        return po == null ? null : toResponse(fromPO(po));
    }

    public List<SemanticFieldMappingResponse> list() {
        return mapper.selectList(null).stream()
                .map(po -> toResponse(fromPO(po))).collect(Collectors.toList());
    }

    @Transactional
    public void delete(String id) { mapper.deleteById(id); }

    private void mapRequest(CreateSemanticFieldMappingRequest req, SemanticFieldMapping entity) {
        if (req.getEntityId() != null) entity.setEntityId(req.getEntityId());
        if (req.getFieldNameEn() != null) entity.setFieldNameEn(req.getFieldNameEn());
        if (req.getBusinessTermId() != null) entity.setBusinessTermId(req.getBusinessTermId());
        if (req.getMappingType() != null) entity.setMappingType(req.getMappingType());
        if (req.getTransformRule() != null) entity.setTransformRule(req.getTransformRule());
    }

        private SemanticFieldMappingPO toPO(SemanticFieldMapping entity) {
        return SemanticFieldMappingPO.builder()
                .id(entity.getId())
                .entityId(entity.getEntityId())
                .fieldNameEn(entity.getFieldNameEn())
                .businessTermId(entity.getBusinessTermId())
                .mappingType(entity.getMappingType())
                .transformRule(entity.getTransformRule())
                .createdAt(entity.getCreatedAt())
                
                .build();
    }

        private SemanticFieldMapping fromPO(SemanticFieldMappingPO po) {
        return SemanticFieldMapping.builder()
                .id(po.getId())
                .entityId(po.getEntityId())
                .fieldNameEn(po.getFieldNameEn())
                .businessTermId(po.getBusinessTermId())
                .mappingType(po.getMappingType())
                .transformRule(po.getTransformRule())
                .createdAt(po.getCreatedAt())
                
                .build();
    }

        private SemanticFieldMappingResponse toResponse(SemanticFieldMapping entity) {
        return SemanticFieldMappingResponse.builder()
                .id(entity.getId())
                .entityId(entity.getEntityId())
                .fieldNameEn(entity.getFieldNameEn())
                .businessTermId(entity.getBusinessTermId())
                .mappingType(entity.getMappingType())
                .transformRule(entity.getTransformRule())
                .createdAt(entity.getCreatedAt())
                
                .build();
    }
}
