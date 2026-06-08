package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.infrastructure.persistence.entity.AggregateRootEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AggregateRootJpaRepository extends JpaRepository<AggregateRootEntity, String> {
    List<AggregateRootEntity> findByContextId(String contextId);
    boolean existsByContextIdAndCode(String contextId, String code);
}
