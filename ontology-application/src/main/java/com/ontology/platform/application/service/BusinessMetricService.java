package com.ontology.platform.application.service;

import com.ontology.platform.application.dto.domain.CreateBusinessMetricRequest;
import com.ontology.platform.application.dto.domain.BusinessMetricResponse;
import com.ontology.platform.domain.entity.BusinessMetric;
import com.ontology.platform.infrastructure.persistence.BusinessMetricPO;
import com.ontology.platform.infrastructure.persistence.BusinessMetricPOMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessMetricService {

    private final BusinessMetricPOMapper mapper;

    @Transactional
    public BusinessMetricResponse create(String ontologyId, CreateBusinessMetricRequest request, String userId) {
        log.info("Creating BusinessMetric: ontologyId={}, name={}", ontologyId, request.getName());
        BusinessMetric entity = BusinessMetric.create(ontologyId);
        mapRequest(request, entity);
        mapper.insert(toPO(entity));
        return toResponse(entity);
    }

    public BusinessMetricResponse getById(String id) {
        BusinessMetricPO po = mapper.selectById(id);
        if (po == null) return null;
        return toResponse(fromPO(po));
    }

    public List<BusinessMetricResponse> listByOntologyId(String ontologyId) {
        return mapper.selectByOntologyId(ontologyId).stream()
                .map(po -> toResponse(fromPO(po)))
                .collect(Collectors.toList());
    }

    @Transactional
    public void delete(String id) {
        if (mapper.selectById(id) != null) mapper.deleteById(id);
    }

    private void mapRequest(CreateBusinessMetricRequest req, BusinessMetric entity) {
        if (req.getName() != null) entity.setName(req.getName());
        if (req.getNameEn() != null) entity.setNameEn(req.getNameEn());
        if (req.getDescription() != null) entity.setDescription(req.getDescription());
        if (req.getFormula() != null) entity.setFormula(req.getFormula());
        if (req.getDataSourceRef() != null) entity.setDataSourceRef(req.getDataSourceRef());
        if (req.getPeriod() != null) entity.setPeriod(req.getPeriod());
        if (req.getTargetEntity() != null) entity.setTargetEntity(req.getTargetEntity());
    }

    private BusinessMetricPO toPO(BusinessMetric entity) {
        return BusinessMetricPO.builder()
                .id(entity.getId())
                .ontologyId(entity.getOntologyId())
                .name(entity.getName())
                .nameEn(entity.getNameEn())
                .description(entity.getDescription())
                .formula(entity.getFormula())
                .dataSourceRef(entity.getDataSourceRef())
                .period(entity.getPeriod())
                .targetEntity(entity.getTargetEntity())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private BusinessMetric fromPO(BusinessMetricPO po) {
        return BusinessMetric.builder()
                .id(po.getId())
                .ontologyId(po.getOntologyId())
                .name(po.getName())
                .nameEn(po.getNameEn())
                .description(po.getDescription())
                .formula(po.getFormula())
                .dataSourceRef(po.getDataSourceRef())
                .period(po.getPeriod())
                .targetEntity(po.getTargetEntity())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }

    private BusinessMetricResponse toResponse(BusinessMetric entity) {
        return BusinessMetricResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .nameEn(entity.getNameEn())
                .description(entity.getDescription())
                .formula(entity.getFormula())
                .dataSourceRef(entity.getDataSourceRef())
                .period(entity.getPeriod())
                .targetEntity(entity.getTargetEntity())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
