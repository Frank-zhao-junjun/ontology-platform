package com.ontology.platform.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 关系定义持久化Mapper接口
 * Relation Persistence Mapper Interface
 */
@Mapper
public interface RelationPOMapper extends BaseMapper<RelationPO> {

    /**
     * 根据本体ID查询所有关系
     */
    List<RelationPO> selectByOntologyId(@Param("ontologyId") String ontologyId);

    /**
     * 根据本体ID和关系名查询
     */
    RelationPO selectByOntologyIdAndName(
            @Param("ontologyId") String ontologyId,
            @Param("name") String name);

    /**
     * 根据源对象类型ID查询
     */
    List<RelationPO> selectBySourceTypeId(@Param("sourceTypeId") String sourceTypeId);

    /**
     * 根据目标对象类型ID查询
     */
    List<RelationPO> selectByTargetTypeId(@Param("targetTypeId") String targetTypeId);

    /**
     * 查询与指定对象类型关联的所有关系（作为源或目标）
     */
    List<RelationPO> selectBySourceTypeIdOrTargetTypeId(
            @Param("sourceTypeId") String sourceTypeId,
            @Param("targetTypeId") String targetTypeId);

    /**
     * 检查是否存在（本体ID+名称）
     */
    int countByOntologyIdAndName(
            @Param("ontologyId") String ontologyId,
            @Param("name") String name);

    /**
     * 检查是否存在（本体ID+名称，排除指定ID）
     */
    int countByOntologyIdAndNameExcludingId(
            @Param("ontologyId") String ontologyId,
            @Param("name") String name,
            @Param("excludeId") String excludeId);

    /**
     * 统计本体下的关系数量
     */
    long countByOntologyId(@Param("ontologyId") String ontologyId);
}
