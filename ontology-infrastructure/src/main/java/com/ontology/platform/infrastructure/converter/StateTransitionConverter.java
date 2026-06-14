package com.ontology.platform.infrastructure.converter;

import com.ontology.platform.domain.entity.StateTransition;
import com.ontology.platform.infrastructure.persistence.StateTransitionPO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class StateTransitionConverter {

    public StateTransition toEntity(StateTransitionPO po) {
        if (po == null) return null;
        return StateTransition.builder()
                .id(po.getId())
                .stateMachineId(po.getStateMachineId())
                .fromState(po.getFromState())
                .toState(po.getToState())
                .trigger(po.getTriggerName())
                .guardCondition(po.getGuardCondition())
                .createdAt(po.getCreatedAt())
                .build();
    }

    public StateTransitionPO toPO(StateTransition entity) {
        if (entity == null) return null;
        return StateTransitionPO.builder()
                .id(entity.getId())
                .stateMachineId(entity.getStateMachineId())
                .fromState(entity.getFromState())
                .toState(entity.getToState())
                .triggerName(entity.getTrigger())
                .guardCondition(entity.getGuardCondition())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public List<StateTransition> toEntityList(List<StateTransitionPO> poList) {
        if (poList == null) return List.of();
        return poList.stream().map(this::toEntity).collect(Collectors.toList());
    }
}
