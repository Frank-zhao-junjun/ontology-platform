package com.ontology.platform.infrastructure.persistence;

import com.ontology.platform.domain.entity.Metric;
import com.ontology.platform.domain.repository.MetricRepository;
import com.ontology.platform.infrastructure.repository.MetricJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JpaMetricRepository implements MetricRepository {
    private final MetricJpaRepository jpa;

    @Override
    public Metric save(Metric metric) {
        jpa.save(PersistenceMapper.toEntity(metric));
        return metric;
    }

    @Override
    public Optional<Metric> findById(String id) {
        return jpa.findById(id).map(PersistenceMapper::toDomain);
    }

    @Override
    public List<Metric> findByContextId(String contextId) {
        return jpa.findByContextId(contextId).stream().map(PersistenceMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public boolean existsByContextIdAndManifestCode(String contextId, String manifestCode) {
        return jpa.existsByContextIdAndManifestCode(contextId, manifestCode);
    }
}
