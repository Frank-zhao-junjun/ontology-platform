package com.ontology.platform.domain.repository;

import com.ontology.platform.domain.entity.OntologyAction;

import java.util.List;
import java.util.Optional;

public interface OntologyActionRepository {
    void save(OntologyAction action);
    Optional<OntologyAction> findById(String id);
    Optional<OntologyAction> findByContextIdAndManifestCode(String contextId, String manifestCode);
    List<OntologyAction> findByContextId(String contextId);
    boolean existsByContextIdAndManifestCode(String contextId, String manifestCode);
}
