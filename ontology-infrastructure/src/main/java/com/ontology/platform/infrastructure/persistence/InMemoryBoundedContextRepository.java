package com.ontology.platform.infrastructure.persistence;

import com.ontology.platform.domain.entity.BoundedContext;
import com.ontology.platform.domain.repository.BoundedContextRepository;
import org.springframework.stereotype.Repository;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryBoundedContextRepository implements BoundedContextRepository {
    private final Map<String, BoundedContext> store = new ConcurrentHashMap<>();

    @Override public void save(BoundedContext ctx) { store.put(ctx.getId(), ctx); }
    @Override public Optional<BoundedContext> findById(String id) { return Optional.ofNullable(store.get(id)); }
    @Override public Optional<BoundedContext> findByCode(String code) { return store.values().stream().filter(c -> c.getCode().equals(code)).findFirst(); }
    @Override public List<BoundedContext> findAll() { return new ArrayList<>(store.values()); }
    @Override public boolean existsByCode(String code) { return store.values().stream().anyMatch(c -> c.getCode().equals(code)); }
    @Override public void update(BoundedContext ctx) { store.put(ctx.getId(), ctx); }
}
