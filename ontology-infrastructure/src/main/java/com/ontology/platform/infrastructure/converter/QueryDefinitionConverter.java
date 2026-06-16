package com.ontology.platform.infrastructure.converter;

import com.ontology.platform.domain.entity.QueryDefinition;
import com.ontology.platform.infrastructure.persistence.QueryDefinitionPO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class QueryDefinitionConverter {

    public QueryDefinition toEntity(QueryDefinitionPO po) {
        if (po == null) return null;
        return QueryDefinition.builder()
                .id(po.getId())
                .ontologyId(po.getOntologyId())
                .queryName(po.getQueryName())
                .description(po.getDescription())
                .queryType(po.getQueryType())
                .queryTemplate(po.getQueryTemplate())
                .parameters(po.getParameters())
                .resultSchema(po.getResultSchema())
                .timeoutMs(po.getTimeoutMs())
                .enabled(po.getEnabled())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .deleted(po.getDeleted())
                .build();
    }

    public QueryDefinitionPO toPO(QueryDefinition entity) {
        if (entity == null) return null;
        return QueryDefinitionPO.builder()
                .id(entity.getId())
                .ontologyId(entity.getOntologyId())
                .queryName(entity.getQueryName())
                .description(entity.getDescription())
                .queryType(entity.getQueryType())
                .queryTemplate(entity.getQueryTemplate())
                .parameters(entity.getParameters())
                .resultSchema(entity.getResultSchema())
                .timeoutMs(entity.getTimeoutMs())
                .enabled(entity.getEnabled())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .deleted(entity.getDeleted())
                .build();
    }

    public List<QueryDefinition> toEntityList(List<QueryDefinitionPO> poList) {
        if (poList == null) return List.of();
        return poList.stream().map(this::toEntity).collect(Collectors.toList());
    }
}
