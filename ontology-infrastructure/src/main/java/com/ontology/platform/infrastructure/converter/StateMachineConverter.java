package com.ontology.platform.infrastructure.converter;

import com.ontology.platform.domain.entity.StateMachine;
import com.ontology.platform.infrastructure.persistence.StateMachinePO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class StateMachineConverter {

    public StateMachine toEntity(StateMachinePO po) {
        if (po == null) return null;
        return StateMachine.builder()
                .id(po.getId())
                .ontologyId(po.getOntologyId())
                .entityId(po.getEntityId())
                .name(po.getName())
                .initialState(po.getInitialState())
                .states(po.getStates())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .deleted(po.getDeleted())
                .build();
    }

    public StateMachinePO toPO(StateMachine entity) {
        if (entity == null) return null;
        return StateMachinePO.builder()
                .id(entity.getId())
                .ontologyId(entity.getOntologyId())
                .entityId(entity.getEntityId())
                .name(entity.getName())
                .initialState(entity.getInitialState())
                .states(entity.getStates())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .deleted(entity.getDeleted())
                .build();
    }

    public List<StateMachine> toEntityList(List<StateMachinePO> poList) {
        if (poList == null) return List.of();
        return poList.stream().map(this::toEntity).collect(Collectors.toList());
    }
}
