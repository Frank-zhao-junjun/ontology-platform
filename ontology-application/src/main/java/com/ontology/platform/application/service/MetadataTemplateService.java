package com.ontology.platform.application.service;

import com.ontology.platform.application.dto.domain.CreateMetadataTemplateRequest;
import com.ontology.platform.application.dto.domain.MetadataTemplateResponse;
import com.ontology.platform.domain.entity.MetadataTemplate;
import com.ontology.platform.infrastructure.persistence.MetadataTemplatePO;
import com.ontology.platform.infrastructure.persistence.MetadataTemplatePOMapper;
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
public class MetadataTemplateService {

    private final MetadataTemplatePOMapper mapper;

    @Transactional
    public MetadataTemplateResponse create(String ontologyId, CreateMetadataTemplateRequest request, String userId) {
        log.info("Creating MetadataTemplate: ontologyId={}", ontologyId);
        MetadataTemplate entity = MetadataTemplate.create();
        return toResponse(entity);
    }

    public MetadataTemplateResponse getById(String id) {
        MetadataTemplatePO po = mapper.selectById(id);
        if (po == null) return null;
        return toResponse(fromPO(po));
    }

    public List<MetadataTemplateResponse> listByOntologyId(String ontologyId) {
        List<MetadataTemplatePO> pos = mapper.selectByOntologyId(ontologyId);
        return pos.stream().map(po -> toResponse(fromPO(po))).collect(Collectors.toList());
    }

    @Transactional
    public void delete(String id) {
        MetadataTemplatePO po = mapper.selectById(id);
        if (po != null) mapper.deleteById(id);
    }

    private MetadataTemplatePO toPO(MetadataTemplate entity) {
        return MetadataTemplatePO.builder().id(entity.getId())
                .createdAt(entity.getCreatedAt()).updatedAt(entity.getUpdatedAt()).build();
    }

    private MetadataTemplate fromPO(MetadataTemplatePO po) {
        return MetadataTemplate.builder().id(po.getId())
                .createdAt(po.getCreatedAt()).updatedAt(po.getUpdatedAt()).build();
    }

    private MetadataTemplateResponse toResponse(MetadataTemplate entity) {
        return MetadataTemplateResponse.builder().id(entity.getId())
                .createdAt(entity.getCreatedAt()).updatedAt(entity.getUpdatedAt()).build();
    }
}
