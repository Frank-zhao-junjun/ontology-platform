package com.ontology.platform.infrastructure.converter;

import com.ontology.platform.domain.entity.IndicatorDefinition;
import com.ontology.platform.infrastructure.persistence.IndicatorDefinitionPO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class IndicatorDefinitionConverter {

    public IndicatorDefinition toEntity(IndicatorDefinitionPO po) {
        if (po == null) return null;
        return IndicatorDefinition.builder()
                .id(po.getId())
                .ontologyId(po.getOntologyId())
                .indicatorName(po.getIndicatorName())
                .description(po.getDescription())
                .formula(po.getFormula())
                .targetValue(po.getTargetValue())
                .unit(po.getUnit())
                .warningThreshold(po.getWarningThreshold())
                .criticalThreshold(po.getCriticalThreshold())
                .aggregationType(po.getAggregationType())
                .frequencySec(po.getFrequencySec())
                .enabled(po.getEnabled())
                .extendedData(po.getExtendedData())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .deleted(po.getDeleted())
                .build();
    }

    public IndicatorDefinitionPO toPO(IndicatorDefinition entity) {
        if (entity == null) return null;
        return IndicatorDefinitionPO.builder()
                .id(entity.getId())
                .ontologyId(entity.getOntologyId())
                .indicatorName(entity.getIndicatorName())
                .description(entity.getDescription())
                .formula(entity.getFormula())
                .targetValue(entity.getTargetValue())
                .unit(entity.getUnit())
                .warningThreshold(entity.getWarningThreshold())
                .criticalThreshold(entity.getCriticalThreshold())
                .aggregationType(entity.getAggregationType())
                .frequencySec(entity.getFrequencySec())
                .enabled(entity.getEnabled())
                .extendedData(entity.getExtendedData())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .deleted(entity.getDeleted())
                .build();
    }

    public List<IndicatorDefinition> toEntityList(List<IndicatorDefinitionPO> poList) {
        if (poList == null) return List.of();
        return poList.stream().map(this::toEntity).collect(Collectors.toList());
    }
}
