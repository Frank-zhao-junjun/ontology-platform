package com.ontology.platform.domain.repository;

import com.ontology.platform.domain.entity.ProbeDefinition;
import java.util.List;
import java.util.Optional;

/**
 * 探测定义仓储接口
 * Probe Definition Repository Interface
 */
public interface ProbeDefinitionRepository {

    /**
     * 根据ID查询探测定义
     */
    Optional<ProbeDefinition> findById(String id);

    /**
     * 根据本体ID查询所有探测定义
     */
    List<ProbeDefinition> findByOntologyId(String ontologyId);

    /**
     * 保存探测定义
     */
    ProbeDefinition save(ProbeDefinition entity);

    /**
     * 根据ID删除探测定义
     */
    void deleteById(String id);
}
