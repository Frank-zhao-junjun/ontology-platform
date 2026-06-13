package com.ontology.platform.infrastructure.converter;

import com.ontology.platform.domain.entity.Relation;
import com.ontology.platform.domain.vo.RelationProperty;
import com.ontology.platform.infrastructure.persistence.RelationPO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 关系 PO <-> Entity 转换器
 * Relation PO Converter
 */
@Slf4j
@Component
public class RelationConverter {

    /**
     * PO 转换为 Entity
     */
    public Relation toEntity(RelationPO po) {
        if (po == null) {
            return null;
        }

        Relation relation = Relation.builder()
                .id(po.getId())
                .ontologyId(po.getOntologyId())
                .sourceTypeId(po.getSourceTypeId())
                .targetTypeId(po.getTargetTypeId())
                .name(po.getName())
                .displayName(po.getDisplayName())
                .description(po.getDescription())
                .cardinality(po.getCardinalityEnum())
                .reverseName(po.getReverseName())
                .reverseDisplayName(po.getReverseDisplayName())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();

        // 属性暂不持久化（与原实现保持一致）
        relation.setProperties(new ArrayList<>());
        return relation;
    }

    /**
     * Entity 转换为 PO
     */
    public RelationPO toPO(Relation relation) {
        if (relation == null) {
            return null;
        }

        return RelationPO.builder()
                .id(relation.getId())
                .ontologyId(relation.getOntologyId())
                .sourceTypeId(relation.getSourceTypeId())
                .targetTypeId(relation.getTargetTypeId())
                .name(relation.getName())
                .displayName(relation.getDisplayName())
                .description(relation.getDescription())
                .cardinality(relation.getCardinality() != null ? relation.getCardinality().getValue() : null)
                .reverseName(relation.getReverseName())
                .reverseDisplayName(relation.getReverseDisplayName())
                .createdAt(relation.getCreatedAt())
                .updatedAt(relation.getUpdatedAt())
                .build();
    }

    /**
     * PO 列表转换为 Entity 列表
     */
    public List<Relation> toEntityList(List<RelationPO> poList) {
        if (poList == null || poList.isEmpty()) {
            return List.of();
        }
        List<Relation> result = new ArrayList<>(poList.size());
        for (RelationPO po : poList) {
            result.add(toEntity(po));
        }
        return result;
    }

    /**
     * 提取 Entity 的属性列表（占位，关系属性暂未持久化）
     */
    @SuppressWarnings("unused")
    private List<RelationProperty> extractProperties(Relation relation) {
        return relation.getProperties() != null ? relation.getProperties() : new ArrayList<>();
    }
}
