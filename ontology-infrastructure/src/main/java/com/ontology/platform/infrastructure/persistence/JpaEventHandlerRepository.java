package com.ontology.platform.infrastructure.persistence;

import com.ontology.platform.domain.entity.EventHandler;
import com.ontology.platform.domain.repository.EventHandlerRepository;
import com.ontology.platform.infrastructure.repository.EventHandlerJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JpaEventHandlerRepository implements EventHandlerRepository {
    private final EventHandlerJpaRepository jpa;

    @Override
    public void save(EventHandler handler) {
        jpa.save(PersistenceMapper.toEntity(handler));
    }

    @Override
    public Optional<EventHandler> findById(String id) {
        return jpa.findById(id).map(PersistenceMapper::toDomain);
    }

    @Override
    public Optional<EventHandler> findByContextIdAndManifestCode(String contextId, String manifestCode) {
        return jpa.findByContextIdAndManifestCode(contextId, manifestCode).map(PersistenceMapper::toDomain);
    }

    @Override
    public List<EventHandler> findByContextId(String contextId) {
        return jpa.findByContextId(contextId).stream().map(PersistenceMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<EventHandler> findByEventId(String eventId) {
        return jpa.findByEventId(eventId).stream().map(PersistenceMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<EventHandler> findByHandlerBehaviorId(String behaviorId) {
        return jpa.findByHandlerBehaviorId(behaviorId).stream().map(PersistenceMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public boolean existsByContextIdAndManifestCode(String contextId, String manifestCode) {
        return jpa.existsByContextIdAndManifestCode(contextId, manifestCode);
    }
}
