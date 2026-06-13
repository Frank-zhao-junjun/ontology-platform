package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.domain.entity.Causality;
import com.ontology.platform.domain.repository.CausalityRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class InMemoryCausalityRepository implements CausalityRepository {

    private final Map<String, Causality> store = new ConcurrentHashMap<>();

    @Override
    public Optional<Causality> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Causality> findByOntologyId(String ontologyId) {
        return store.values().stream()
                .filter(c -> c.getOntologyId().equals(ontologyId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Causality> findByCauseEventId(String causeEventId) {
        return store.values().stream()
                .filter(c -> c.getCauseEventId().equals(causeEventId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Causality> findByEffectEventId(String effectEventId) {
        return store.values().stream()
                .filter(c -> c.getEffectEventId().equals(effectEventId))
                .collect(Collectors.toList());
    }

    @Override
    public Causality save(Causality entity) {
        store.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public void deleteById(String id) {
        store.remove(id);
    }
}
