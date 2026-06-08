package com.ontology.platform.application.service;

import com.ontology.platform.common.enums.ErrorCode;
import com.ontology.platform.common.exception.BusinessException;
import com.ontology.platform.common.exception.ResourceNotFoundException;
import com.ontology.platform.domain.entity.Metric;
import com.ontology.platform.domain.repository.MetricRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MetricService {
    private final MetricRepository metricRepo;

    public Metric createMetric(String contextId, String manifestCode, String name, String nameEn,
                               String formula, String dataSourceRefJson, String aggregationDimensionsJson,
                               String period) {
        if (metricRepo.existsByContextIdAndManifestCode(contextId, manifestCode)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "指标 manifestCode 已存在: " + manifestCode);
        }
        Metric metric = Metric.create(contextId, manifestCode, name, nameEn, formula,
                dataSourceRefJson, aggregationDimensionsJson, period);
        metricRepo.save(metric);
        return metric;
    }

    public List<Metric> listMetrics(String contextId) {
        return metricRepo.findByContextId(contextId);
    }

    public Metric getMetric(String metricId) {
        return metricRepo.findById(metricId)
                .orElseThrow(() -> new ResourceNotFoundException("Metric not found: " + metricId));
    }
}
