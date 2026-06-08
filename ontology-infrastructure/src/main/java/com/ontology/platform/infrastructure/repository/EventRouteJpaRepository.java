package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.infrastructure.persistence.entity.EventRouteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRouteJpaRepository extends JpaRepository<EventRouteEntity, String> {
    Optional<EventRouteEntity> findByContextIdAndManifestCode(String contextId, String manifestCode);
    List<EventRouteEntity> findByContextId(String contextId);
    List<EventRouteEntity> findBySourceEventId(String eventId);
    boolean existsByContextIdAndManifestCode(String contextId, String manifestCode);
}
