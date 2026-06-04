package com.ontology.platform.infrastructure.persistence;

import com.ontology.platform.domain.entity.DomainEventDefinition;
import com.ontology.platform.domain.repository.DomainEventRepository;
import com.ontology.platform.infrastructure.repository.DomainEventJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JpaDomainEventRepository implements DomainEventRepository {
    private final DomainEventJpaRepository jpa;

    @Override
    public void save(DomainEventDefinition event) {
        jpa.save(PersistenceMapper.toEntity(event));
    }

    @Override
    public Optional<DomainEventDefinition> findById(String id) {
        return jpa.findById(id).map(PersistenceMapper::toDomain);
    }

    @Override
    public Optional<DomainEventDefinition> findByContextIdAndManifestCode(String contextId, String manifestCode) {
        return jpa.findByContextIdAndManifestCode(contextId, manifestCode).map(PersistenceMapper::toDomain);
    }

    @Override
    public List<DomainEventDefinition> findByContextId(String contextId) {
        return jpa.findByContextId(contextId).stream().map(PersistenceMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public boolean existsByContextIdAndManifestCode(String contextId, String manifestCode) {
        return jpa.existsByContextIdAndManifestCode(contextId, manifestCode);
    }
}
