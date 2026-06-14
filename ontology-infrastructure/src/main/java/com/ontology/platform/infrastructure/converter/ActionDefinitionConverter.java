package com.ontology.platform.infrastructure.converter;

import com.ontology.platform.domain.entity.ActionDefinition;
import com.ontology.platform.infrastructure.persistence.ActionDefinitionPO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ActionDefinitionConverter {

    public ActionDefinition toEntity(ActionDefinitionPO po) {
        if (po == null) return null;
        return ActionDefinition.builder()
                .id(po.getId())
                .ontologyId(po.getOntologyId())
                .entityId(po.getEntityId())
                .name(po.getName())
                .displayName(po.getDisplayName())
                .description(po.getDescription())
                .actionType(po.getActionType())
                .inputSchema(po.getInputSchema())
                .outputSchema(po.getOutputSchema())
                .preRules(po.getPreRules())
                .postRules(po.getPostRules())
                .domain(po.getDomain())
                .riskLevel(po.getRiskLevel())
                .isAsync(po.getIsAsync())
                .timeoutMs(po.getTimeoutMs())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .deleted(po.getDeleted())
                .build();
    }

    public ActionDefinitionPO toPO(ActionDefinition entity) {
        if (entity == null) return null;
        return ActionDefinitionPO.builder()
                .id(entity.getId())
                .ontologyId(entity.getOntologyId())
                .entityId(entity.getEntityId())
                .name(entity.getName())
                .displayName(entity.getDisplayName())
                .description(entity.getDescription())
                .actionType(entity.getActionType())
                .inputSchema(entity.getInputSchema())
                .outputSchema(entity.getOutputSchema())
                .preRules(entity.getPreRules())
                .postRules(entity.getPostRules())
                .domain(entity.getDomain())
                .riskLevel(entity.getRiskLevel())
                .isAsync(entity.getIsAsync())
                .timeoutMs(entity.getTimeoutMs())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .deleted(entity.getDeleted())
                .build();
    }

    public List<ActionDefinition> toEntityList(List<ActionDefinitionPO> poList) {
        if (poList == null) return List.of();
        return poList.stream().map(this::toEntity).collect(Collectors.toList());
    }
}
