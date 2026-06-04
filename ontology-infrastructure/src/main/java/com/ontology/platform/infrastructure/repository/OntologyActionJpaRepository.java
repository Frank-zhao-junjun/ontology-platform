package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.infrastructure.persistence.entity.OntologyActionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OntologyActionJpaRepository extends JpaRepository<OntologyActionEntity, String> {
    List<OntologyActionEntity> findByContextId(String contextId);
    Optional<OntologyActionEntity> findByContextIdAndManifestCode(String contextId, String manifestCode);
    boolean existsByContextIdAndManifestCode(String contextId, String manifestCode);
}
