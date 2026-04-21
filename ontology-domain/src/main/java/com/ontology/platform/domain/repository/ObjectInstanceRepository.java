package com.ontology.platform.domain.repository;

import com.ontology.platform.domain.entity.ObjectInstance;

import java.util.List;
import java.util.Optional;

/**
 * 对象实例仓储接口
 * ObjectInstance Repository Interface
 */
public interface ObjectInstanceRepository {

    /**
     * 根据ID查询对象实例
     */
    Optional<ObjectInstance> findById(String id);

    /**
     * 根据本体ID和主键值查询对象实例
     */
    Optional<ObjectInstance> findByOntologyIdAndPrimaryKeyValue(String ontologyId, String primaryKeyValue);

    /**
     * 根据本体ID和对象类型ID查询所有实例
     */
    List<ObjectInstance> findByOntologyIdAndObjectTypeId(String ontologyId, String objectTypeId);

    /**
     * 根据本体ID查询所有实例
     */
    List<ObjectInstance> findByOntologyId(String ontologyId);

    /**
     * 根据对象类型ID查询所有实例
     */
    List<ObjectInstance> findByObjectTypeId(String objectTypeId);

    /**
     * 保存对象实例
     */
    ObjectInstance save(ObjectInstance instance);

    /**
     * 更新对象实例
     */
    ObjectInstance update(ObjectInstance instance);

    /**
     * 删除对象实例（物理删除）
     */
    void deleteById(String id);

    /**
     * 逻辑删除对象实例
     */
    void markAsDeleted(String id);

    /**
     * 检查实例是否存在
     */
    boolean existsByOntologyIdAndObjectTypeIdAndPrimaryKeyValue(String ontologyId, String objectTypeId, String primaryKeyValue);

    /**
     * 统计本体中指定类型的实例数量
     */
    long countByOntologyIdAndObjectTypeId(String ontologyId, String objectTypeId);

    /**
     * 统计本体的实例总数
     */
    long countByOntologyId(String ontologyId);

    /**
     * 批量保存实例
     */
    List<ObjectInstance> saveAll(List<ObjectInstance> instances);

    /**
     * 批量删除实例
     */
    void deleteAllByIds(List<String> ids);
}
