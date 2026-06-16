package com.ontology.platform.infrastructure.converter;

import com.ontology.platform.domain.entity.ApiDefinition;
import com.ontology.platform.infrastructure.persistence.ApiDefinitionPO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ApiDefinitionConverter {

    public ApiDefinition toEntity(ApiDefinitionPO po) {
        if (po == null) return null;
        return ApiDefinition.builder()
                .id(po.getId())
                .ontologyId(po.getOntologyId())
                .apiName(po.getApiName())
                .description(po.getDescription())
                .url(po.getUrl())
                .httpMethod(po.getHttpMethod())
                .requestSchema(po.getRequestSchema())
                .responseSchema(po.getResponseSchema())
                .authType(po.getAuthType())
                .rateLimit(po.getRateLimit())
                .timeoutMs(po.getTimeoutMs())
                .enabled(po.getEnabled())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .deleted(po.getDeleted())
                .build();
    }

    public ApiDefinitionPO toPO(ApiDefinition entity) {
        if (entity == null) return null;
        return ApiDefinitionPO.builder()
                .id(entity.getId())
                .ontologyId(entity.getOntologyId())
                .apiName(entity.getApiName())
                .description(entity.getDescription())
                .url(entity.getUrl())
                .httpMethod(entity.getHttpMethod())
                .requestSchema(entity.getRequestSchema())
                .responseSchema(entity.getResponseSchema())
                .authType(entity.getAuthType())
                .rateLimit(entity.getRateLimit())
                .timeoutMs(entity.getTimeoutMs())
                .enabled(entity.getEnabled())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .deleted(entity.getDeleted())
                .build();
    }

    public List<ApiDefinition> toEntityList(List<ApiDefinitionPO> poList) {
        if (poList == null) return List.of();
        return poList.stream().map(this::toEntity).collect(Collectors.toList());
    }
}
