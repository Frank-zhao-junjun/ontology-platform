package com.ontology.platform.domain.repository;

import com.ontology.platform.domain.entity.ApiDefinition;
import java.util.List;
import java.util.Optional;

/**
 * API定义仓储接口
 * Api Definition Repository Interface
 */
public interface ApiDefinitionRepository {

    /**
     * 根据ID查询API定义
     */
    Optional<ApiDefinition> findById(String id);

    /**
     * 根据本体ID查询所有API定义
     */
    List<ApiDefinition> findByOntologyId(String ontologyId);

    /**
     * 保存API定义
     */
    ApiDefinition save(ApiDefinition entity);

    /**
     * 根据ID删除API定义
     */
    void deleteById(String id);
}
