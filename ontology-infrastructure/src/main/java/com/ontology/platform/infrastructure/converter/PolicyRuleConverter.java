package com.ontology.platform.infrastructure.converter;

import com.ontology.platform.domain.entity.PolicyRule;
import com.ontology.platform.infrastructure.persistence.PolicyRulePO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PolicyRuleConverter {

    public PolicyRule toEntity(PolicyRulePO po) {
        if (po == null) return null;
        return PolicyRule.builder()
                .id(po.getId())
                .ontologyId(po.getOntologyId())
                .policyName(po.getPolicyName())
                .description(po.getDescription())
                .policyType(po.getPolicyType())
                .rules(po.getRules())
                .effect(po.getEffect())
                .priority(po.getPriority())
                .enabled(po.getEnabled())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .deleted(po.getDeleted())
                .build();
    }

    public PolicyRulePO toPO(PolicyRule entity) {
        if (entity == null) return null;
        return PolicyRulePO.builder()
                .id(entity.getId())
                .ontologyId(entity.getOntologyId())
                .policyName(entity.getPolicyName())
                .description(entity.getDescription())
                .policyType(entity.getPolicyType())
                .rules(entity.getRules())
                .effect(entity.getEffect())
                .priority(entity.getPriority())
                .enabled(entity.getEnabled())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .deleted(entity.getDeleted())
                .build();
    }

    public List<PolicyRule> toEntityList(List<PolicyRulePO> poList) {
        if (poList == null) return List.of();
        return poList.stream().map(this::toEntity).collect(Collectors.toList());
    }
}
