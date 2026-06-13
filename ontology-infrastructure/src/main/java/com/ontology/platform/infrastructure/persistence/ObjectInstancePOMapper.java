package com.ontology.platform.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 对象实例持久化 Mapper 接口
 * Object Instance Persistence Mapper Interface
 */
@Mapper
public interface ObjectInstancePOMapper extends BaseMapper<ObjectInstancePO> {

    /**
     * 根据对象类型ID分页查询对象实例
     */
    List<ObjectInstancePO> selectByObjectTypeId(
            @Param("objectTypeId") String objectTypeId,
            @Param("offset") int offset,
            @Param("limit") int limit);

    /**
     * 统计指定对象类型下的实例数量
     */
    long countByObjectTypeId(@Param("objectTypeId") String objectTypeId);

    /**
     * 根据对象类型ID和主键值查询
     */
    ObjectInstancePO selectByObjectTypeIdAndPrimaryKeyValue(
            @Param("objectTypeId") String objectTypeId,
            @Param("primaryKeyValue") String primaryKeyValue);
}
