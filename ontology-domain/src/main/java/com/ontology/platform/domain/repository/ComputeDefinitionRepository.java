package com.ontology.platform.domain.repository;

import com.ontology.platform.domain.entity.ComputeDefinition;
import java.util.List;
import java.util.Optional;

/**
 * 计算定义仓储接口
 * Compute Definition Repository Interface
 */
public interface ComputeDefinitionRepository {

    /**
     * 根据ID查询计算定义
     */
    Optional<ComputeDefinition> findById(String id);

    /**
     * 根据本体ID查询所有计算定义
     */
    List<ComputeDefinition> findByOntologyId(String ontologyId);

    /**
     * 保存计算定义
     */
    ComputeDefinition save(ComputeDefinition entity);

    /**
     * 根据ID删除计算定义
     */
    void deleteById(String id);
}
