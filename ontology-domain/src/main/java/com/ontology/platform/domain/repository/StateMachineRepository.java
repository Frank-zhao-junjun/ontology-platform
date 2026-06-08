package com.ontology.platform.domain.repository;

import com.ontology.platform.domain.entity.StateMachine;

import java.util.List;
import java.util.Optional;

public interface StateMachineRepository {
    StateMachine save(StateMachine sm);
    Optional<StateMachine> findById(String id);
    List<StateMachine> findByContextId(String contextId);
    List<StateMachine> findByObjectTypeId(String objectTypeId);
    boolean existsByContextIdAndObjectTypeId(String contextId, String objectTypeId);
}
