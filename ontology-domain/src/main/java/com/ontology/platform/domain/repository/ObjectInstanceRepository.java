package com.ontology.platform.domain.repository;

import com.ontology.platform.domain.entity.ObjectInstance;

import java.util.List;
import java.util.Optional;

/**
 * 对象实例仓储接口
 * Object Instance Repository Interface
 */
public interface ObjectInstanceRepository {

    /**
     * 根据ID查询对象实例
     */
    Optional<ObjectInstance> findById(String id);

    /**
     * 根据对象类型ID分页查询对象实例
     *
     * @param objectTypeId 对象类型ID
     * @param offset       偏移量
     * @param limit        限制数
     * @return 实例列表
     */
    List<ObjectInstance> findByObjectTypeId(String objectTypeId, int offset, int limit);

    /**
     * 统计指定对象类型下的实例数量
     */
    long countByObjectTypeId(String objectTypeId);

    /**
     * 保存对象实例
     */
    ObjectInstance save(ObjectInstance instance);

    /**
     * 根据ID删除对象实例
     */
    void deleteById(String id);

    /**
     * 检查指定对象类型下是否存在指定主键值的实例
     */
    boolean existsByObjectTypeIdAndPrimaryKeyValue(String objectTypeId, String pkValue);
}
