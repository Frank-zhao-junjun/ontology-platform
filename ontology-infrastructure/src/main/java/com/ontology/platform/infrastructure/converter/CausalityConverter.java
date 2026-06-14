package com.ontology.platform.infrastructure.converter;

import com.ontology.platform.domain.entity.Causality;
import com.ontology.platform.infrastructure.persistence.CausalityPO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CausalityConverter {

    public Causality toEntity(CausalityPO po) {
        if (po == null) return null;
        return Causality.builder()
                .id(po.getId())
                .ontologyId(po.getOntologyId())
                .causeEventId(po.getCauseEventId())
                .effectEventId(po.getEffectEventId())
                .description(po.getDescription())
                .delayMs(po.getDelayMs())
                .condition(po.getCondition())
                .createdAt(po.getCreatedAt())
                .build();
    }

    public CausalityPO toPO(Causality entity) {
        if (entity == null) return null;
        return CausalityPO.builder()
                .id(entity.getId())
                .ontologyId(entity.getOntologyId())
                .causeEventId(entity.getCauseEventId())
                .effectEventId(entity.getEffectEventId())
                .description(entity.getDescription())
                .delayMs(entity.getDelayMs())
                .condition(entity.getCondition())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public List<Causality> toEntityList(List<CausalityPO> poList) {
        if (poList == null) return List.of();
        return poList.stream().map(this::toEntity).collect(Collectors.toList());
    }
}
