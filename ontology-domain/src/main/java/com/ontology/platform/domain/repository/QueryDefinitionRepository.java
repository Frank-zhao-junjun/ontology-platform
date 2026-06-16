package com.ontology.platform.domain.repository;

import com.ontology.platform.domain.entity.QueryDefinition;
import java.util.List;
import java.util.Optional;

/**
 * 查询定义仓储接口
 * Query Definition Repository Interface
 */
public interface QueryDefinitionRepository {

    /**
     * 根据ID查询查询定义
     */
    Optional<QueryDefinition> findById(String id);

    /**
     * 根据本体ID查询所有查询定义
     */
    List<QueryDefinition> findByOntologyId(String ontologyId);

    /**
     * 保存查询定义
     */
    QueryDefinition save(QueryDefinition entity);

    /**
     * 根据ID删除查询定义
     */
    void deleteById(String id);
}
