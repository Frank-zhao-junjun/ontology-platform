package com.ontology.platform.infrastructure.converter;

import com.ontology.platform.domain.entity.EpcStep;
import com.ontology.platform.infrastructure.persistence.EpcStepPO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class EpcStepConverter {

    public EpcStep toEntity(EpcStepPO po) {
        if (po == null) return null;
        return EpcStep.builder()
                .id(po.getId())
                .ontologyId(po.getOntologyId())
                .flowName(po.getFlowName())
                .stepOrder(po.getStepOrder())
                .triggerEventId(po.getTriggerEventId())
                .actionId(po.getActionId())
                .conditions(po.getConditions())
                .guards(po.getGuards())
                .timeoutMs(po.getTimeoutMs())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }

    public EpcStepPO toPO(EpcStep entity) {
        if (entity == null) return null;
        return EpcStepPO.builder()
                .id(entity.getId())
                .ontologyId(entity.getOntologyId())
                .flowName(entity.getFlowName())
                .stepOrder(entity.getStepOrder())
                .triggerEventId(entity.getTriggerEventId())
                .actionId(entity.getActionId())
                .conditions(entity.getConditions())
                .guards(entity.getGuards())
                .timeoutMs(entity.getTimeoutMs())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public List<EpcStep> toEntityList(List<EpcStepPO> poList) {
        if (poList == null) return List.of();
        return poList.stream().map(this::toEntity).collect(Collectors.toList());
    }
}
