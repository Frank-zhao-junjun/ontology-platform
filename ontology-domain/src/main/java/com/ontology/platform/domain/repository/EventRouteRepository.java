package com.ontology.platform.domain.repository;

import com.ontology.platform.domain.entity.EventRoute;

import java.util.List;
import java.util.Optional;

public interface EventRouteRepository {
    void save(EventRoute route);
    Optional<EventRoute> findById(String id);
    Optional<EventRoute> findByContextIdAndManifestCode(String contextId, String manifestCode);
    List<EventRoute> findByContextId(String contextId);
    List<EventRoute> findBySourceEventId(String eventId);
    boolean existsByContextIdAndManifestCode(String contextId, String manifestCode);
}
