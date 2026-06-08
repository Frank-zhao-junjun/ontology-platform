package com.ontology.platform.domain.repository;

import com.ontology.platform.domain.entity.Metric;

import java.util.List;
import java.util.Optional;

public interface MetricRepository {
    Metric save(Metric metric);
    Optional<Metric> findById(String id);
    List<Metric> findByContextId(String contextId);
    boolean existsByContextIdAndManifestCode(String contextId, String manifestCode);
}
