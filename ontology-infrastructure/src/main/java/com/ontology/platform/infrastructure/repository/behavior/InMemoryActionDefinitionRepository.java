package com.ontology.platform.infrastructure.repository.behavior;

import com.ontology.platform.domain.entity.ActionDefinition;
import com.ontology.platform.domain.repository.behavior.ActionDefinitionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class InMemoryActionDefinitionRepository implements ActionDefinitionRepository {

    private final Map<String, ActionDefinition> store = new ConcurrentHashMap<>();

    @Override
    public Optional<ActionDefinition> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<ActionDefinition> findByOntologyId(String ontologyId) {
        return store.values().stream()
                .filter(a -> a.getOntologyId().equals(ontologyId) && !Boolean.TRUE.equals(a.getDeleted()))
                .collect(Collectors.toList());
    }

    @Override
    public List<ActionDefinition> findByOntologyIdAndEntityId(String ontologyId, String entityId) {
        return store.values().stream()
                .filter(a -> a.getOntologyId().equals(ontologyId)
                        && a.getEntityId().equals(entityId)
                        && !Boolean.TRUE.equals(a.getDeleted()))
                .collect(Collectors.toList());
    }

    @Override
    public List<ActionDefinition> findByOntologyIdAndDomain(String ontologyId, String domain) {
        return store.values().stream()
                .filter(a -> a.getOntologyId().equals(ontologyId)
                        && domain.equals(a.getDomain())
                        && !Boolean.TRUE.equals(a.getDeleted()))
                .collect(Collectors.toList());
    }

    @Override
    public ActionDefinition save(ActionDefinition entity) {
        store.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public void deleteById(String id) {
        store.remove(id);
    }
}
