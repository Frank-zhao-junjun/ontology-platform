package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.infrastructure.persistence.entity.EventHandlerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventHandlerJpaRepository extends JpaRepository<EventHandlerEntity, String> {
    Optional<EventHandlerEntity> findByContextIdAndManifestCode(String contextId, String manifestCode);
    List<EventHandlerEntity> findByContextId(String contextId);
    List<EventHandlerEntity> findByEventId(String eventId);
    List<EventHandlerEntity> findByHandlerBehaviorId(String behaviorId);
    boolean existsByContextIdAndManifestCode(String contextId, String manifestCode);
}
