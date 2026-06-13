package com.ontology.platform.infrastructure.converter;

import com.ontology.platform.domain.entity.ObjectInstance;
import com.ontology.platform.infrastructure.persistence.ObjectInstancePO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 对象实例 PO <-> Entity 转换器
 * Object Instance Converter
 */
@Slf4j
@Component
public class ObjectInstanceConverter {

    /**
     * PO 转换为 Entity
     */
    public ObjectInstance toEntity(ObjectInstancePO po) {
        if (po == null) {
            return null;
        }

        Map<String, Object> coreData = po.getCoreDataMap();
        Map<String, Object> extendedData = po.getExtendedDataMap();

        return ObjectInstance.builder()
                .id(po.getId())
                .ontologyId(po.getOntologyId())
                .objectTypeId(po.getObjectTypeId())
                .primaryKeyValue(po.getPrimaryKeyValue())
                .coreData(coreData != null ? coreData : new java.util.HashMap<>())
                .extendedData(extendedData != null ? extendedData : new java.util.HashMap<>())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }

    /**
     * Entity 转换为 PO
     */
    public ObjectInstancePO toPO(ObjectInstance instance) {
        if (instance == null) {
            return null;
        }

        ObjectInstancePO po = ObjectInstancePO.builder()
                .id(instance.getId())
                .ontologyId(instance.getOntologyId())
                .objectTypeId(instance.getObjectTypeId())
                .primaryKeyValue(instance.getPrimaryKeyValue())
                .createdAt(instance.getCreatedAt())
                .updatedAt(instance.getUpdatedAt())
                .build();

        po.setCoreDataMap(instance.getCoreData());
        po.setExtendedDataMap(instance.getExtendedData());
        return po;
    }

    /**
     * PO 列表转换为 Entity 列表
     */
    public List<ObjectInstance> toEntityList(List<ObjectInstancePO> poList) {
        if (poList == null || poList.isEmpty()) {
            return List.of();
        }
        List<ObjectInstance> result = new ArrayList<>(poList.size());
        for (ObjectInstancePO po : poList) {
            result.add(toEntity(po));
        }
        return result;
    }
}
