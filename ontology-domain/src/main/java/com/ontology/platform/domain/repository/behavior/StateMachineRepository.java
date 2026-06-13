package com.ontology.platform.domain.repository.behavior;

import com.ontology.platform.domain.entity.StateMachine;
import java.util.List;
import java.util.Optional;

public interface StateMachineRepository {
    Optional<StateMachine> findById(String id);
    List<StateMachine> findByOntologyId(String ontologyId);
    List<StateMachine> findByOntologyIdAndEntityId(String ontologyId, String entityId);
    StateMachine save(StateMachine entity);
    void deleteById(String id);
}
