package com.ontology.platform.infrastructure.converter;

import com.ontology.platform.domain.entity.NotificationDefinition;
import com.ontology.platform.infrastructure.persistence.NotificationDefinitionPO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class NotificationDefinitionConverter {

    public NotificationDefinition toEntity(NotificationDefinitionPO po) {
        if (po == null) return null;
        return NotificationDefinition.builder()
                .id(po.getId())
                .ontologyId(po.getOntologyId())
                .notifName(po.getNotifName())
                .description(po.getDescription())
                .channel(po.getChannel())
                .template(po.getTemplate())
                .recipients(po.getRecipients())
                .triggerEvent(po.getTriggerEvent())
                .enabled(po.getEnabled())
                .config(po.getConfig())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .deleted(po.getDeleted())
                .build();
    }

    public NotificationDefinitionPO toPO(NotificationDefinition entity) {
        if (entity == null) return null;
        return NotificationDefinitionPO.builder()
                .id(entity.getId())
                .ontologyId(entity.getOntologyId())
                .notifName(entity.getNotifName())
                .description(entity.getDescription())
                .channel(entity.getChannel())
                .template(entity.getTemplate())
                .recipients(entity.getRecipients())
                .triggerEvent(entity.getTriggerEvent())
                .enabled(entity.getEnabled())
                .config(entity.getConfig())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .deleted(entity.getDeleted())
                .build();
    }

    public List<NotificationDefinition> toEntityList(List<NotificationDefinitionPO> poList) {
        if (poList == null) return List.of();
        return poList.stream().map(this::toEntity).collect(Collectors.toList());
    }
}
