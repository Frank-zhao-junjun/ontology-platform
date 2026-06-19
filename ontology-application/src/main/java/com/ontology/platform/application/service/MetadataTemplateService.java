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

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetadataTemplateService {

    private final MetadataTemplatePOMapper mapper;

    @Transactional
    public MetadataTemplateResponse create(String ontologyId, CreateMetadataTemplateRequest request, String userId) {
        log.info("Creating MetadataTemplate: ontologyId={}, name={}", ontologyId, request.getName());
        MetadataTemplate entity = MetadataTemplate.create(ontologyId);
        mapRequest(request, entity);
        mapper.insert(toPO(entity));
        return toResponse(entity);
    }

    public MetadataTemplateResponse getById(String id) {
        MetadataTemplatePO po = mapper.selectById(id);
        if (po == null) return null;
        return toResponse(fromPO(po));
    }

    public List<MetadataTemplateResponse> listByOntologyId(String ontologyId) {
        return mapper.selectByOntologyId(ontologyId).stream()
                .map(po -> toResponse(fromPO(po)))
                .collect(Collectors.toList());
    }

    @Transactional
    public void delete(String id) {
        if (mapper.selectById(id) != null) mapper.deleteById(id);
    }

    private void mapRequest(CreateMetadataTemplateRequest req, MetadataTemplate entity) {
        if (req.getName() != null) entity.setName(req.getName());
        if (req.getNameEn() != null) entity.setNameEn(req.getNameEn());
        if (req.getDescription() != null) entity.setDescription(req.getDescription());
        if (req.getDomain() != null) entity.setDomain(req.getDomain());
        if (req.getTemplateType() != null) entity.setTemplateType(req.getTemplateType());
    }

    private MetadataTemplatePO toPO(MetadataTemplate entity) {
        return MetadataTemplatePO.builder()
                .id(entity.getId())
                .ontologyId(entity.getOntologyId())
                .name(entity.getName())
                .nameEn(entity.getNameEn())
                .description(entity.getDescription())
                .domain(entity.getDomain())
                .templateType(entity.getTemplateType())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private MetadataTemplate fromPO(MetadataTemplatePO po) {
        return MetadataTemplate.builder()
                .id(po.getId())
                .ontologyId(po.getOntologyId())
                .name(po.getName())
                .nameEn(po.getNameEn())
                .description(po.getDescription())
                .domain(po.getDomain())
                .templateType(po.getTemplateType())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }

    private MetadataTemplateResponse toResponse(MetadataTemplate entity) {
        return MetadataTemplateResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .nameEn(entity.getNameEn())
                .description(entity.getDescription())
                .domain(entity.getDomain())
                .templateType(entity.getTemplateType())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
