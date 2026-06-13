package com.ontology.platform.domain.repository;

import com.ontology.platform.domain.entity.DomainEvent;
import java.util.List;
import java.util.Optional;

public interface DomainEventRepository {
    Optional<DomainEvent> findById(String id);
    List<DomainEvent> findByOntologyId(String ontologyId);
    List<DomainEvent> findByOntologyIdAndEntityId(String ontologyId, String entityId);
    List<DomainEvent> findByOntologyIdAndEventType(String ontologyId, String eventType);
    DomainEvent save(DomainEvent entity);
    void deleteById(String id);
}
