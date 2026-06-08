package com.ontology.platform.infrastructure.converter;

import com.ontology.platform.domain.entity.ObjectType;
import com.ontology.platform.domain.vo.Property;
import com.ontology.platform.infrastructure.persistence.ObjectTypePO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 对象类型持久化对象转换器
 * ObjectType PO <-> Entity Converter
 */
@Component
public class ObjectTypeConverter {

    /**
     * PO转换为Entity
     */
    public ObjectType toEntity(ObjectTypePO po) {
        if (po == null) {
            return null;
        }
        return ObjectType.builder()
                .id(po.getId())
                .ontologyId(po.getOntologyId())
                .name(po.getName())
                .displayName(po.getDisplayName())
                .description(po.getDescription())
                .primaryKey(po.getPrimaryKey())
                .parentId(po.getParentId())
                .interfaceNames(po.getInterfaceNamesList())
                .instanceCount(po.getInstanceCount() != null ? po.getInstanceCount() : 0)
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .properties(new ArrayList<>())
                .relations(new ArrayList<>())
                .build();
    }

    /**
     * PO转换为Entity（包含属性列表）
     */
    public ObjectType toEntity(ObjectTypePO po, List<Property> properties) {
        if (po == null) {
            return null;
        }
        ObjectType entity = toEntity(po);
        entity.setProperties(properties != null ? properties : new ArrayList<>());
        return entity;
    }

    /**
     * Entity转换为PO
     */
    public ObjectTypePO toPO(ObjectType entity) {
        if (entity == null) {
            return null;
        }
        ObjectTypePO po = ObjectTypePO.builder()
                .id(entity.getId())
                .ontologyId(entity.getOntologyId())
                .name(entity.getName())
                .displayName(entity.getDisplayName())
                .description(entity.getDescription())
                .primaryKey(entity.getPrimaryKey())
                .parentId(entity.getParentId())
                .instanceCount(entity.getInstanceCount())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
        po.setInterfaceNamesList(entity.getInterfaceNames());
        return po;
    }

    /**
     * PO列表转换为Entity列表
     */
    public List<ObjectType> toEntityList(List<ObjectTypePO> poList) {
        if (poList == null) {
            return List.of();
        }
        return poList.stream()
                .map(this::toEntity)
                .collect(java.util.stream.Collectors.toList());
    }
}
