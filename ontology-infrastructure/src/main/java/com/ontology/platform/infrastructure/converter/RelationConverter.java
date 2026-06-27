package com.ontology.platform.infrastructure.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private static final ObjectMapper objectMapper = new ObjectMapper();

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

        // 从 extended_data 反序列化属性列表
        List<RelationProperty> properties = deserializeProperties(po.getExtendedData());
        relation.setProperties(properties);
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
                .extendedData(serializeProperties(relation.getProperties()))
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
     * 将 RelationProperty 列表序列化为 JSON 字符串
     */
    private String serializeProperties(List<RelationProperty> properties) {
        if (properties == null || properties.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(properties);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize relation properties", e);
            return null;
        }
    }

    /**
     * 从 JSON 字符串反序列化为 RelationProperty 列表
     */
    private List<RelationProperty> deserializeProperties(String extendedData) {
        if (extendedData == null || extendedData.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(extendedData,
                    new TypeReference<List<RelationProperty>>() {});
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize relation properties from extended_data", e);
            return new ArrayList<>();
        }
    }
}
