package com.ontology.platform.infrastructure.converter;

import com.ontology.platform.common.enums.OntologyStatus;
import com.ontology.platform.domain.entity.Ontology;
import com.ontology.platform.infrastructure.persistence.OntologyPO;
import org.springframework.stereotype.Component;

/**
 * 本体持久化对象转换器
 * Ontology PO <-> Entity Converter
 */
@Component
public class OntologyConverter {

    /**
     * PO转换为Entity
     */
    public Ontology toEntity(OntologyPO po) {
        if (po == null) {
            return null;
        }
        return Ontology.builder()
                .id(po.getId())
                .tenantId(po.getTenantId())
                .name(po.getName())
                .displayName(po.getDisplayName())
                .description(po.getDescription())
                .version(po.getVersion())
                .status(OntologyStatus.fromValue(po.getStatus()))
                .publishedAt(po.getPublishedAt())
                .objectTypeCount(po.getObjectTypeCount() != null ? po.getObjectTypeCount() : 0)
                .actionTypeCount(po.getActionTypeCount() != null ? po.getActionTypeCount() : 0)
                .createdBy(po.getCreatedBy())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .deleted(false)
                .build();
    }

    /**
     * Entity转换为PO
     */
    public OntologyPO toPO(Ontology entity) {
        if (entity == null) {
            return null;
        }
        return OntologyPO.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .name(entity.getName())
                .displayName(entity.getDisplayName())
                .description(entity.getDescription())
                .version(entity.getVersion())
                .status(entity.getStatus() != null ? entity.getStatus().getValue() : OntologyStatus.DRAFT.getValue())
                .publishedAt(entity.getPublishedAt())
                .objectTypeCount(entity.getObjectTypeCount())
                .actionTypeCount(entity.getActionTypeCount())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * PO列表转换为Entity列表
     */
    public java.util.List<Ontology> toEntityList(java.util.List<OntologyPO> poList) {
        if (poList == null) {
            return java.util.List.of();
        }
        return poList.stream()
                .map(this::toEntity)
                .collect(java.util.stream.Collectors.toList());
    }
}
