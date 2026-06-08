package com.ontology.platform.domain.repository;

import com.ontology.platform.domain.entity.BusinessScenario;

import java.util.List;
import java.util.Optional;

public interface BusinessScenarioRepository {
    BusinessScenario save(BusinessScenario scenario);
    Optional<BusinessScenario> findById(String id);
    List<BusinessScenario> findByContextId(String contextId);
    boolean existsByContextIdAndCode(String contextId, String code);
}
