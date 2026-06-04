package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.infrastructure.persistence.entity.DomainEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DomainEventJpaRepository extends JpaRepository<DomainEventEntity, String> {
    List<DomainEventEntity> findByContextId(String contextId);
    Optional<DomainEventEntity> findByContextIdAndManifestCode(String contextId, String manifestCode);
    boolean existsByContextIdAndManifestCode(String contextId, String manifestCode);
}
