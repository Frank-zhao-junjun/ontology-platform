package com.ontology.platform.infrastructure.converter;

import com.ontology.platform.domain.entity.ProbeDefinition;
import com.ontology.platform.infrastructure.persistence.ProbeDefinitionPO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProbeDefinitionConverter {

    public ProbeDefinition toEntity(ProbeDefinitionPO po) {
        if (po == null) return null;
        return ProbeDefinition.builder()
                .id(po.getId())
                .ontologyId(po.getOntologyId())
                .probeName(po.getProbeName())
                .description(po.getDescription())
                .target(po.getTarget())
                .probeType(po.getProbeType())
                .frequencySec(po.getFrequencySec())
                .timeoutMs(po.getTimeoutMs())
                .alertCondition(po.getAlertCondition())
                .alertSeverity(po.getAlertSeverity())
                .enabled(po.getEnabled())
                .config(po.getConfig())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .deleted(po.getDeleted())
                .build();
    }

    public ProbeDefinitionPO toPO(ProbeDefinition entity) {
        if (entity == null) return null;
        return ProbeDefinitionPO.builder()
                .id(entity.getId())
                .ontologyId(entity.getOntologyId())
                .probeName(entity.getProbeName())
                .description(entity.getDescription())
                .target(entity.getTarget())
                .probeType(entity.getProbeType())
                .frequencySec(entity.getFrequencySec())
                .timeoutMs(entity.getTimeoutMs())
                .alertCondition(entity.getAlertCondition())
                .alertSeverity(entity.getAlertSeverity())
                .enabled(entity.getEnabled())
                .config(entity.getConfig())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .deleted(entity.getDeleted())
                .build();
    }

    public List<ProbeDefinition> toEntityList(List<ProbeDefinitionPO> poList) {
        if (poList == null) return List.of();
        return poList.stream().map(this::toEntity).collect(Collectors.toList());
    }
}
