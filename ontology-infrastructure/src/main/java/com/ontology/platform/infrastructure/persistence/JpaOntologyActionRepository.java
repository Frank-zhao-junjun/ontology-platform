package com.ontology.platform.infrastructure.persistence;

import com.ontology.platform.domain.entity.OntologyAction;
import com.ontology.platform.domain.repository.OntologyActionRepository;
import com.ontology.platform.infrastructure.repository.OntologyActionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JpaOntologyActionRepository implements OntologyActionRepository {
    private final OntologyActionJpaRepository jpa;

    @Override
    public void save(OntologyAction action) {
        jpa.save(PersistenceMapper.toEntity(action));
    }

    @Override
    public Optional<OntologyAction> findById(String id) {
        return jpa.findById(id).map(PersistenceMapper::toDomain);
    }

    @Override
    public Optional<OntologyAction> findByContextIdAndManifestCode(String contextId, String manifestCode) {
        return jpa.findByContextIdAndManifestCode(contextId, manifestCode).map(PersistenceMapper::toDomain);
    }

    @Override
    public List<OntologyAction> findByContextId(String contextId) {
        return jpa.findByContextId(contextId).stream().map(PersistenceMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public boolean existsByContextIdAndManifestCode(String contextId, String manifestCode) {
        return jpa.existsByContextIdAndManifestCode(contextId, manifestCode);
    }
}
