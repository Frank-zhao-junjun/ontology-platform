package com.ontology.platform.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 对象类型持久化Mapper接口
 * ObjectType Persistence Mapper Interface
 */
@Mapper
public interface ObjectTypeMapper extends BaseMapper<ObjectTypePO> {

    /**
     * 根据本体ID查询所有对象类型
     */
    List<ObjectTypePO> selectByOntologyId(@Param("ontologyId") String ontologyId);

    /**
     * 根据本体ID和名称查询
     */
    ObjectTypePO selectByOntologyIdAndName(@Param("ontologyId") String ontologyId, @Param("name") String name);

    /**
     * 根据本体ID和名称查询（排除指定ID）
     */
    ObjectTypePO selectByOntologyIdAndNameExcludingId(
            @Param("ontologyId") String ontologyId,
            @Param("name") String name,
            @Param("excludeId") String excludeId);

    /**
     * 根据父类型ID查询子类型
     */
    List<ObjectTypePO> selectByParentId(@Param("parentId") String parentId);

    /**
     * 检查是否存在（本体ID+名称）
     */
    int countByOntologyIdAndName(@Param("ontologyId") String ontologyId, @Param("name") String name);

    /**
     * 检查是否存在（本体ID+名称，排除指定ID）
     */
    int countByOntologyIdAndNameExcludingId(
            @Param("ontologyId") String ontologyId,
            @Param("name") String name,
            @Param("excludeId") String excludeId);

    /**
     * 统计本体下的对象类型数量
     */
    long countByOntologyId(@Param("ontologyId") String ontologyId);
}
