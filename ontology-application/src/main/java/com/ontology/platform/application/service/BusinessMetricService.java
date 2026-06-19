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

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessMetricService {

    private final BusinessMetricPOMapper mapper;

    @Transactional
    public BusinessMetricResponse create(String ontologyId, CreateBusinessMetricRequest request, String userId) {
        log.info("Creating BusinessMetric: ontologyId={}", ontologyId);
        BusinessMetric entity = BusinessMetric.create();
        return toResponse(entity);
    }

    public BusinessMetricResponse getById(String id) {
        BusinessMetricPO po = mapper.selectById(id);
        if (po == null) return null;
        return toResponse(fromPO(po));
    }

    public List<BusinessMetricResponse> listByOntologyId(String ontologyId) {
        List<BusinessMetricPO> pos = mapper.selectByOntologyId(ontologyId);
        return pos.stream().map(po -> toResponse(fromPO(po))).collect(Collectors.toList());
    }

    @Transactional
    public void delete(String id) {
        BusinessMetricPO po = mapper.selectById(id);
        if (po != null) mapper.deleteById(id);
    }

    private BusinessMetricPO toPO(BusinessMetric entity) {
        return BusinessMetricPO.builder().id(entity.getId())
                .createdAt(entity.getCreatedAt()).updatedAt(entity.getUpdatedAt()).build();
    }

    private BusinessMetric fromPO(BusinessMetricPO po) {
        return BusinessMetric.builder().id(po.getId())
                .createdAt(po.getCreatedAt()).updatedAt(po.getUpdatedAt()).build();
    }

    private BusinessMetricResponse toResponse(BusinessMetric entity) {
        return BusinessMetricResponse.builder().id(entity.getId())
                .createdAt(entity.getCreatedAt()).updatedAt(entity.getUpdatedAt()).build();
    }
}
