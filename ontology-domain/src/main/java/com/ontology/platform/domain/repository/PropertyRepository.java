package com.ontology.platform.domain.repository;

import com.ontology.platform.domain.vo.Property;

import java.util.List;
import java.util.Optional;

/**
 * 属性仓储接口
 * Property Repository Interface
 */
public interface PropertyRepository {

    /**
     * 根据ID查询属性
     */
    Optional<Property> findById(String id);

    /**
     * 根据对象类型ID查询所有属性
     */
    List<Property> findByObjectTypeId(String objectTypeId);

    /**
     * 根据对象类型ID和属性名查询
     */
    Optional<Property> findByObjectTypeIdAndName(String objectTypeId, String name);

    /**
     * 保存属性
     */
    Property save(Property property);

    /**
     * 更新属性
     */
    Property update(Property property);

    /**
     * 删除属性
     */
    void deleteById(String id);

    /**
     * 检查属性是否存在
     */
    boolean existsByObjectTypeIdAndName(String objectTypeId, String name);

    /**
     * 检查属性是否存在（排除指定ID）
     */
    boolean existsByObjectTypeIdAndNameAndIdNot(String objectTypeId, String name, String excludeId);

    /**
     * 统计对象类型下的属性数量
     */
    long countByObjectTypeId(String objectTypeId);

    /**
     * 批量保存属性
     */
    List<Property> saveAll(List<Property> properties);

    /**
     * 根据对象类型ID删除所有属性
     */
    void deleteAllByObjectTypeId(String objectTypeId);
}
