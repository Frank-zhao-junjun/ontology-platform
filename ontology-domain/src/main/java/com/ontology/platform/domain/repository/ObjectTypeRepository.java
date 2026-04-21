package com.ontology.platform.domain.repository;

import com.ontology.platform.domain.entity.ObjectType;

import java.util.List;
import java.util.Optional;

/**
 * 对象类型仓储接口
 * ObjectType Repository Interface
 */
public interface ObjectTypeRepository {

    /**
     * 根据ID查询对象类型
     */
    Optional<ObjectType> findById(String id);

    /**
     * 根据本体ID查询所有对象类型
     */
    List<ObjectType> findByOntologyId(String ontologyId);

    /**
     * 根据本体ID和名称查询对象类型
     */
    Optional<ObjectType> findByOntologyIdAndName(String ontologyId, String name);

    /**
     * 根据父类型ID查询子类型
     */
    List<ObjectType> findByParentId(String parentId);

    /**
     * 保存对象类型
     */
    ObjectType save(ObjectType objectType);

    /**
     * 更新对象类型
     */
    ObjectType update(ObjectType objectType);

    /**
     * 删除对象类型
     */
    void deleteById(String id);

    /**
     * 检查对象类型是否存在
     */
    boolean existsByOntologyIdAndName(String ontologyId, String name);

    /**
     * 检查对象类型是否存在（排除指定ID）
     */
    boolean existsByOntologyIdAndNameAndIdNot(String ontologyId, String name, String excludeId);

    /**
     * 统计本体下的对象类型数量
     */
    long countByOntologyId(String ontologyId);
}
