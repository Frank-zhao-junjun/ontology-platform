package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.domain.entity.DomainEvent;
import com.ontology.platform.domain.repository.DomainEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class InMemoryDomainEventRepository implements DomainEventRepository {

    private final Map<String, DomainEvent> store = new ConcurrentHashMap<>();

    @Override
    public Optional<DomainEvent> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<DomainEvent> findByOntologyId(String ontologyId) {
        return store.values().stream()
                .filter(e -> e.getOntologyId().equals(ontologyId) && !Boolean.TRUE.equals(e.getDeleted()))
                .collect(Collectors.toList());
    }

    @Override
    public List<DomainEvent> findByOntologyIdAndEntityId(String ontologyId, String entityId) {
        return store.values().stream()
                .filter(e -> e.getOntologyId().equals(ontologyId)
                        && e.getEntityId().equals(entityId)
                        && !Boolean.TRUE.equals(e.getDeleted()))
                .collect(Collectors.toList());
    }

    @Override
    public List<DomainEvent> findByOntologyIdAndEventType(String ontologyId, String eventType) {
        return store.values().stream()
                .filter(e -> e.getOntologyId().equals(ontologyId)
                        && e.getEventType().equals(eventType)
                        && !Boolean.TRUE.equals(e.getDeleted()))
                .collect(Collectors.toList());
    }

    @Override
    public DomainEvent save(DomainEvent entity) {
        store.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public void deleteById(String id) {
        store.remove(id);
    }
}
