package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.infrastructure.persistence.entity.MetricEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MetricJpaRepository extends JpaRepository<MetricEntity, String> {
    List<MetricEntity> findByContextId(String contextId);
    Optional<MetricEntity> findByContextIdAndManifestCode(String contextId, String manifestCode);
    boolean existsByContextIdAndManifestCode(String contextId, String manifestCode);
}
