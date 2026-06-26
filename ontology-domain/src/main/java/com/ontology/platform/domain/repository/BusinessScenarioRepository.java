package com.ontology.platform.domain.repository;

import com.ontology.platform.domain.entity.BusinessScenario;

import java.util.List;
import java.util.Optional;

/**
 * 业务场景仓储接口
 * BusinessScenario Repository Interface
 */
public interface BusinessScenarioRepository {

    Optional<BusinessScenario> findById(String id);

    List<BusinessScenario> findByOntologyId(String ontologyId);

    BusinessScenario save(BusinessScenario businessScenario);

    void deleteById(String id);
}
