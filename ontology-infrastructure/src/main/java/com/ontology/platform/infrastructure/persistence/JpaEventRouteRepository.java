package com.ontology.platform.infrastructure.persistence;

import com.ontology.platform.domain.entity.EventRoute;
import com.ontology.platform.domain.repository.EventRouteRepository;
import com.ontology.platform.infrastructure.repository.EventRouteJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JpaEventRouteRepository implements EventRouteRepository {
    private final EventRouteJpaRepository jpa;

    @Override
    public void save(EventRoute route) {
        jpa.save(PersistenceMapper.toEntity(route));
    }

    @Override
    public Optional<EventRoute> findById(String id) {
        return jpa.findById(id).map(PersistenceMapper::toDomain);
    }

    @Override
    public Optional<EventRoute> findByContextIdAndManifestCode(String contextId, String manifestCode) {
        return jpa.findByContextIdAndManifestCode(contextId, manifestCode).map(PersistenceMapper::toDomain);
    }

    @Override
    public List<EventRoute> findByContextId(String contextId) {
        return jpa.findByContextId(contextId).stream().map(PersistenceMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<EventRoute> findBySourceEventId(String eventId) {
        return jpa.findBySourceEventId(eventId).stream().map(PersistenceMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public boolean existsByContextIdAndManifestCode(String contextId, String manifestCode) {
        return jpa.existsByContextIdAndManifestCode(contextId, manifestCode);
    }
}
