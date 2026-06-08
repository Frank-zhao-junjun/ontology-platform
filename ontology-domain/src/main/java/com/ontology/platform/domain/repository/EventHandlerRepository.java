package com.ontology.platform.domain.repository;

import com.ontology.platform.domain.entity.EventHandler;

import java.util.List;
import java.util.Optional;

public interface EventHandlerRepository {
    void save(EventHandler handler);
    Optional<EventHandler> findById(String id);
    Optional<EventHandler> findByContextIdAndManifestCode(String contextId, String manifestCode);
    List<EventHandler> findByContextId(String contextId);
    List<EventHandler> findByEventId(String eventId);
    List<EventHandler> findByHandlerBehaviorId(String behaviorId);
    boolean existsByContextIdAndManifestCode(String contextId, String manifestCode);
}
