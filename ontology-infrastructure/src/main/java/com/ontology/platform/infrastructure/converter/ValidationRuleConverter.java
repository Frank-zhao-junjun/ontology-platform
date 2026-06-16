package com.ontology.platform.infrastructure.converter;

import com.ontology.platform.domain.entity.ValidationRule;
import com.ontology.platform.infrastructure.persistence.ValidationRulePO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ValidationRuleConverter {

    public ValidationRule toEntity(ValidationRulePO po) {
        if (po == null) return null;
        return ValidationRule.builder()
                .id(po.getId())
                .ontologyId(po.getOntologyId())
                .entityId(po.getEntityId())
                .fieldName(po.getFieldName())
                .ruleType(po.getRuleType())
                .ruleName(po.getRuleName())
                .description(po.getDescription())
                .severity(po.getSeverity())
                .expression(po.getExpression())
                .errorMessage(po.getErrorMessage())
                .enabled(po.getEnabled())
                .sortOrder(po.getSortOrder())
                .extendedData(po.getExtendedData())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .deleted(po.getDeleted())
                .build();
    }

    public ValidationRulePO toPO(ValidationRule entity) {
        if (entity == null) return null;
        return ValidationRulePO.builder()
                .id(entity.getId())
                .ontologyId(entity.getOntologyId())
                .entityId(entity.getEntityId())
                .fieldName(entity.getFieldName())
                .ruleType(entity.getRuleType())
                .ruleName(entity.getRuleName())
                .description(entity.getDescription())
                .severity(entity.getSeverity())
                .expression(entity.getExpression())
                .errorMessage(entity.getErrorMessage())
                .enabled(entity.getEnabled())
                .sortOrder(entity.getSortOrder())
                .extendedData(entity.getExtendedData())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .deleted(entity.getDeleted())
                .build();
    }

    public List<ValidationRule> toEntityList(List<ValidationRulePO> poList) {
        if (poList == null) return List.of();
        return poList.stream().map(this::toEntity).collect(Collectors.toList());
    }
}
