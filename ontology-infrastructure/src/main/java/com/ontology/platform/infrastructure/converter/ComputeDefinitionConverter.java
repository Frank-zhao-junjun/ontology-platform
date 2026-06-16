package com.ontology.platform.infrastructure.converter;

import com.ontology.platform.domain.entity.ComputeDefinition;
import com.ontology.platform.infrastructure.persistence.ComputeDefinitionPO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ComputeDefinitionConverter {

    public ComputeDefinition toEntity(ComputeDefinitionPO po) {
        if (po == null) return null;
        return ComputeDefinition.builder()
                .id(po.getId())
                .ontologyId(po.getOntologyId())
                .computeName(po.getComputeName())
                .description(po.getDescription())
                .inputSchema(po.getInputSchema())
                .formula(po.getFormula())
                .outputType(po.getOutputType())
                .outputSchema(po.getOutputSchema())
                .timeoutMs(po.getTimeoutMs())
                .enabled(po.getEnabled())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .deleted(po.getDeleted())
                .build();
    }

    public ComputeDefinitionPO toPO(ComputeDefinition entity) {
        if (entity == null) return null;
        return ComputeDefinitionPO.builder()
                .id(entity.getId())
                .ontologyId(entity.getOntologyId())
                .computeName(entity.getComputeName())
                .description(entity.getDescription())
                .inputSchema(entity.getInputSchema())
                .formula(entity.getFormula())
                .outputType(entity.getOutputType())
                .outputSchema(entity.getOutputSchema())
                .timeoutMs(entity.getTimeoutMs())
                .enabled(entity.getEnabled())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .deleted(entity.getDeleted())
                .build();
    }

    public List<ComputeDefinition> toEntityList(List<ComputeDefinitionPO> poList) {
        if (poList == null) return List.of();
        return poList.stream().map(this::toEntity).collect(Collectors.toList());
    }
}
