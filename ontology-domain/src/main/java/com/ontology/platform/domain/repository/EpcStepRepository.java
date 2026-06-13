package com.ontology.platform.domain.repository;

import com.ontology.platform.domain.entity.EpcStep;
import java.util.List;
import java.util.Optional;

public interface EpcStepRepository {
    Optional<EpcStep> findById(String id);
    List<EpcStep> findByOntologyId(String ontologyId);
    List<EpcStep> findByOntologyIdAndFlowName(String ontologyId, String flowName);
    List<EpcStep> findByFlowNameOrderByStepOrder(String flowName);
    EpcStep save(EpcStep entity);
    void deleteById(String id);
}
