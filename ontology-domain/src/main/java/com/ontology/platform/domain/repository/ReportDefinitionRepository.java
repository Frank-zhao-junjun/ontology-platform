package com.ontology.platform.domain.repository;

import com.ontology.platform.domain.entity.ReportDefinition;
import java.util.List;
import java.util.Optional;

/**
 * 报表定义仓储接口
 * Report Definition Repository Interface
 */
public interface ReportDefinitionRepository {

    /**
     * 根据ID查询报表定义
     */
    Optional<ReportDefinition> findById(String id);

    /**
     * 根据本体ID查询所有报表定义
     */
    List<ReportDefinition> findByOntologyId(String ontologyId);

    /**
     * 保存报表定义
     */
    ReportDefinition save(ReportDefinition entity);

    /**
     * 根据ID删除报表定义
     */
    void deleteById(String id);
}
