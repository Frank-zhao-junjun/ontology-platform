package com.ontology.platform.domain.repository.behavior;

import com.ontology.platform.domain.entity.StateTransition;
import java.util.List;
import java.util.Optional;

public interface StateTransitionRepository {
    Optional<StateTransition> findById(String id);
    List<StateTransition> findByStateMachineId(String stateMachineId);
    StateTransition save(StateTransition entity);
    void deleteById(String id);
}
