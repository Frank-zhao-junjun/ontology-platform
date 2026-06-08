package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.infrastructure.persistence.entity.BusinessScenarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BusinessScenarioJpaRepository extends JpaRepository<BusinessScenarioEntity, String> {
    List<BusinessScenarioEntity> findByContextIdOrderByCreatedAtAsc(String contextId);
    boolean existsByContextIdAndCode(String contextId, String code);
}
