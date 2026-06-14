package com.ontology.platform.infrastructure.converter;

import com.ontology.platform.domain.entity.DomainEvent;
import com.ontology.platform.infrastructure.persistence.DomainEventPO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DomainEventConverter {

    public DomainEvent toEntity(DomainEventPO po) {
        if (po == null) return null;
        return DomainEvent.builder()
                .id(po.getId())
                .ontologyId(po.getOntologyId())
                .entityId(po.getEntityId())
                .name(po.getName())
                .displayName(po.getDisplayName())
                .description(po.getDescription())
                .eventType(po.getEventType())
                .severity(po.getSeverity())
                .payloadSchema(po.getPayloadSchema())
                .source(po.getSource())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .deleted(po.getDeleted())
                .build();
    }

    public DomainEventPO toPO(DomainEvent entity) {
        if (entity == null) return null;
        return DomainEventPO.builder()
                .id(entity.getId())
                .ontologyId(entity.getOntologyId())
                .entityId(entity.getEntityId())
                .name(entity.getName())
                .displayName(entity.getDisplayName())
                .description(entity.getDescription())
                .eventType(entity.getEventType())
                .severity(entity.getSeverity())
                .payloadSchema(entity.getPayloadSchema())
                .source(entity.getSource())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .deleted(entity.getDeleted())
                .build();
    }

    public List<DomainEvent> toEntityList(List<DomainEventPO> poList) {
        if (poList == null) return List.of();
        return poList.stream().map(this::toEntity).collect(Collectors.toList());
    }
}
