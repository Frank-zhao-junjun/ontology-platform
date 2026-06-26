package com.ontology.platform.infrastructure.converter;

import com.ontology.platform.domain.entity.BusinessScenario;
import com.ontology.platform.infrastructure.persistence.BusinessScenarioPO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 业务场景持久化对象转换器
 * BusinessScenario PO <-> Entity Converter
 */
@Component
public class BusinessScenarioConverter {

    public BusinessScenario toEntity(BusinessScenarioPO po) {
        if (po == null) return null;
        return BusinessScenario.builder()
                .id(po.getId())
                .ontologyId(po.getOntologyId())
                .name(po.getName())
                .nameEn(po.getNameEn())
                .description(po.getDescription())
                .projectId(po.getProjectId())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }

    public BusinessScenarioPO toPO(BusinessScenario entity) {
        if (entity == null) return null;
        return BusinessScenarioPO.builder()
                .id(entity.getId())
                .ontologyId(entity.getOntologyId())
                .name(entity.getName())
                .nameEn(entity.getNameEn())
                .description(entity.getDescription())
                .projectId(entity.getProjectId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public List<BusinessScenario> toEntityList(List<BusinessScenarioPO> poList) {
        if (poList == null) return List.of();
        return poList.stream().map(this::toEntity).collect(Collectors.toList());
    }
}
