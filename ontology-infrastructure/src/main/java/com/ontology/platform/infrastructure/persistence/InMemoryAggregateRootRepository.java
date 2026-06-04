package com.ontology.platform.infrastructure.persistence;

import com.ontology.platform.domain.entity.AggregateRoot;
import com.ontology.platform.domain.repository.AggregateRootRepository;
import org.springframework.stereotype.Repository;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class InMemoryAggregateRootRepository implements AggregateRootRepository {
    private final Map<String, AggregateRoot> store = new ConcurrentHashMap<>();

    @Override public void save(AggregateRoot ar) { store.put(ar.getId(), ar); }
    @Override public Optional<AggregateRoot> findById(String id) { return Optional.ofNullable(store.get(id)); }
    @Override public List<AggregateRoot> findByContextId(String contextId) { return store.values().stream().filter(a -> a.getContextId().equals(contextId)).collect(Collectors.toList()); }
    @Override public boolean existsByCode(String contextId, String code) { return store.values().stream().anyMatch(a -> a.getContextId().equals(contextId) && a.getCode().equals(code)); }
}
