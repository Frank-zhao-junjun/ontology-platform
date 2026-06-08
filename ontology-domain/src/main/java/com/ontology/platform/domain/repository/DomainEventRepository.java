package com.ontology.platform.domain.repository;

import com.ontology.platform.domain.entity.DomainEventDefinition;

import java.util.List;
import java.util.Optional;

public interface DomainEventRepository {
    void save(DomainEventDefinition event);
    Optional<DomainEventDefinition> findById(String id);
    Optional<DomainEventDefinition> findByContextIdAndManifestCode(String contextId, String manifestCode);
    List<DomainEventDefinition> findByContextId(String contextId);
    boolean existsByContextIdAndManifestCode(String contextId, String manifestCode);
}
