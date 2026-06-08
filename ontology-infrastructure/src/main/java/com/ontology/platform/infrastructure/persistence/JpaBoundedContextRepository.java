package com.ontology.platform.infrastructure.persistence;

import com.ontology.platform.domain.entity.BoundedContext;
import com.ontology.platform.domain.repository.BoundedContextRepository;
import com.ontology.platform.infrastructure.repository.BoundedContextJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JpaBoundedContextRepository implements BoundedContextRepository {
    private final BoundedContextJpaRepository jpa;

    @Override public void save(BoundedContext ctx) { jpa.save(PersistenceMapper.toEntity(ctx)); }
    @Override public Optional<BoundedContext> findById(String id) { return jpa.findById(id).map(PersistenceMapper::toDomain); }
    @Override public Optional<BoundedContext> findByCode(String code) { return jpa.findByCode(code).map(PersistenceMapper::toDomain); }
    @Override public List<BoundedContext> findAll() { return jpa.findAll().stream().map(PersistenceMapper::toDomain).collect(Collectors.toList()); }
    @Override public boolean existsByCode(String code) { return jpa.existsByCode(code); }
    @Override public void update(BoundedContext ctx) { jpa.save(PersistenceMapper.toEntity(ctx)); }
}
