package com.ontology.platform.infrastructure.repository.behavior;

import com.ontology.platform.domain.entity.StateTransition;
import com.ontology.platform.domain.repository.behavior.StateTransitionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class InMemoryStateTransitionRepository implements StateTransitionRepository {

    private final Map<String, StateTransition> store = new ConcurrentHashMap<>();

    @Override
    public Optional<StateTransition> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<StateTransition> findByStateMachineId(String stateMachineId) {
        return store.values().stream()
                .filter(t -> t.getStateMachineId().equals(stateMachineId))
                .collect(Collectors.toList());
    }

    @Override
    public StateTransition save(StateTransition entity) {
        store.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public void deleteById(String id) {
        store.remove(id);
    }
}
