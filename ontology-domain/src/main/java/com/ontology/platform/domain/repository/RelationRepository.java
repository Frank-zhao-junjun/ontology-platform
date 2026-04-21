package com.ontology.platform.domain.repository;

import com.ontology.platform.domain.entity.Relation;

import java.util.List;
import java.util.Optional;

/**
 * 关系仓储接口
 * Relation Repository Interface
 */
public interface RelationRepository {

    /**
     * 根据ID查询关系
     */
    Optional<Relation> findById(String id);

    /**
     * 根据本体ID查询所有关系
     */
    List<Relation> findByOntologyId(String ontologyId);

    /**
     * 根据本体ID和名称查询关系
     */
    Optional<Relation> findByOntologyIdAndName(String ontologyId, String name);

    /**
     * 根据源对象类型ID查询关系
     */
    List<Relation> findBySourceTypeId(String sourceTypeId);

    /**
     * 根据目标对象类型ID查询关系
     */
    List<Relation> findByTargetTypeId(String targetTypeId);

    /**
     * 查询与指定对象类型关联的所有关系
     */
    List<Relation> findBySourceTypeIdOrTargetTypeId(String sourceTypeId, String targetTypeId);

    /**
     * 保存关系
     */
    Relation save(Relation relation);

    /**
     * 更新关系
     */
    Relation update(Relation relation);

    /**
     * 删除关系
     */
    void deleteById(String id);

    /**
     * 检查关系是否存在
     */
    boolean existsByOntologyIdAndName(String ontologyId, String name);

    /**
     * 检查关系是否存在（排除指定ID）
     */
    boolean existsByOntologyIdAndNameAndIdNot(String ontologyId, String name, String excludeId);

    /**
     * 统计本体下的关系数量
     */
    long countByOntologyId(String ontologyId);
}
