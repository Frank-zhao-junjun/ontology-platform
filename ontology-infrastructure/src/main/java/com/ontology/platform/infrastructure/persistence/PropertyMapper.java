package com.ontology.platform.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 属性定义持久化Mapper接口
 * Property Persistence Mapper Interface
 */
@Mapper
public interface PropertyMapper extends BaseMapper<PropertyPO> {

    /**
     * 根据对象类型ID查询所有属性
     */
    List<PropertyPO> selectByObjectTypeId(@Param("objectTypeId") String objectTypeId);

    /**
     * 根据对象类型ID和属性名查询
     */
    PropertyPO selectByObjectTypeIdAndName(@Param("objectTypeId") String objectTypeId, @Param("name") String name);

    /**
     * 根据对象类型ID和属性名查询（排除指定ID）
     */
    PropertyPO selectByObjectTypeIdAndNameExcludingId(
            @Param("objectTypeId") String objectTypeId,
            @Param("name") String name,
            @Param("excludeId") String excludeId);

    /**
     * 检查是否存在（对象类型ID+名称）
     */
    int countByObjectTypeIdAndName(@Param("objectTypeId") String objectTypeId, @Param("name") String name);

    /**
     * 检查是否存在（对象类型ID+名称，排除指定ID）
     */
    int countByObjectTypeIdAndNameExcludingId(
            @Param("objectTypeId") String objectTypeId,
            @Param("name") String name,
            @Param("excludeId") String excludeId);

    /**
     * 统计对象类型下的属性数量
     */
    long countByObjectTypeId(@Param("objectTypeId") String objectTypeId);

    /**
     * 根据对象类型ID删除所有属性
     */
    int deleteAllByObjectTypeId(@Param("objectTypeId") String objectTypeId);
}
