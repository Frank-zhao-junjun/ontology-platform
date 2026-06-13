package com.ontology.platform.infrastructure.repository.behavior;

import com.ontology.platform.domain.entity.StateMachine;
import com.ontology.platform.domain.repository.behavior.StateMachineRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class InMemoryStateMachineRepository implements StateMachineRepository {

    private final Map<String, StateMachine> store = new ConcurrentHashMap<>();

    @Override
    public Optional<StateMachine> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<StateMachine> findByOntologyId(String ontologyId) {
        return store.values().stream()
                .filter(s -> s.getOntologyId().equals(ontologyId) && !Boolean.TRUE.equals(s.getDeleted()))
                .collect(Collectors.toList());
    }

    @Override
    public List<StateMachine> findByOntologyIdAndEntityId(String ontologyId, String entityId) {
        return store.values().stream()
                .filter(s -> s.getOntologyId().equals(ontologyId)
                        && s.getEntityId().equals(entityId)
                        && !Boolean.TRUE.equals(s.getDeleted()))
                .collect(Collectors.toList());
    }

    @Override
    public StateMachine save(StateMachine entity) {
        store.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public void deleteById(String id) {
        store.remove(id);
    }
}
