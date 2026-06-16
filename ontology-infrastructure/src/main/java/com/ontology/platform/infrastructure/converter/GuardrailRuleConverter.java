package com.ontology.platform.infrastructure.converter;

import com.ontology.platform.domain.entity.GuardrailRule;
import com.ontology.platform.infrastructure.persistence.GuardrailRulePO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GuardrailRuleConverter {

    public GuardrailRule toEntity(GuardrailRulePO po) {
        if (po == null) return null;
        return GuardrailRule.builder()
                .id(po.getId())
                .ontologyId(po.getOntologyId())
                .ruleName(po.getRuleName())
                .description(po.getDescription())
                .conditionExpr(po.getConditionExpr())
                .actionType(po.getActionType())
                .actionConfig(po.getActionConfig())
                .priority(po.getPriority())
                .enabled(po.getEnabled())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .deleted(po.getDeleted())
                .build();
    }

    public GuardrailRulePO toPO(GuardrailRule entity) {
        if (entity == null) return null;
        return GuardrailRulePO.builder()
                .id(entity.getId())
                .ontologyId(entity.getOntologyId())
                .ruleName(entity.getRuleName())
                .description(entity.getDescription())
                .conditionExpr(entity.getConditionExpr())
                .actionType(entity.getActionType())
                .actionConfig(entity.getActionConfig())
                .priority(entity.getPriority())
                .enabled(entity.getEnabled())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .deleted(entity.getDeleted())
                .build();
    }

    public List<GuardrailRule> toEntityList(List<GuardrailRulePO> poList) {
        if (poList == null) return List.of();
        return poList.stream().map(this::toEntity).collect(Collectors.toList());
    }
}
