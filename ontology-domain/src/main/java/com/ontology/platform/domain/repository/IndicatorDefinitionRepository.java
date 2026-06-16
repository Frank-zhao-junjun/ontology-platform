package com.ontology.platform.domain.repository;

import com.ontology.platform.domain.entity.IndicatorDefinition;
import java.util.List;
import java.util.Optional;

/**
 * 指标定义仓储接口
 * Indicator Definition Repository Interface
 */
public interface IndicatorDefinitionRepository {

    /**
     * 根据ID查询指标定义
     */
    Optional<IndicatorDefinition> findById(String id);

    /**
     * 根据本体ID查询所有指标定义
     */
    List<IndicatorDefinition> findByOntologyId(String ontologyId);

    /**
     * 保存指标定义
     */
    IndicatorDefinition save(IndicatorDefinition entity);

    /**
     * 根据ID删除指标定义
     */
    void deleteById(String id);
}
