package com.ontology.platform.infrastructure.persistence;

import com.ontology.platform.domain.entity.AggregateRoot;
import com.ontology.platform.domain.repository.AggregateRootRepository;
import com.ontology.platform.infrastructure.repository.AggregateRootJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JpaAggregateRootRepository implements AggregateRootRepository {
    private final AggregateRootJpaRepository jpa;

    @Override public void save(AggregateRoot ar) { jpa.save(PersistenceMapper.toEntity(ar)); }
    @Override public Optional<AggregateRoot> findById(String id) { return jpa.findById(id).map(PersistenceMapper::toDomain); }
    @Override public List<AggregateRoot> findByContextId(String contextId) {
        return jpa.findByContextId(contextId).stream().map(PersistenceMapper::toDomain).collect(Collectors.toList());
    }
    @Override public boolean existsByCode(String contextId, String code) { return jpa.existsByContextIdAndCode(contextId, code); }
}
