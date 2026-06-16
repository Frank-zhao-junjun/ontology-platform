package com.ontology.platform.domain.repository;

import com.ontology.platform.domain.entity.NotificationDefinition;
import java.util.List;
import java.util.Optional;

/**
 * 通知定义仓储接口
 * Notification Definition Repository Interface
 */
public interface NotificationDefinitionRepository {

    /**
     * 根据ID查询通知定义
     */
    Optional<NotificationDefinition> findById(String id);

    /**
     * 根据本体ID查询所有通知定义
     */
    List<NotificationDefinition> findByOntologyId(String ontologyId);

    /**
     * 保存通知定义
     */
    NotificationDefinition save(NotificationDefinition entity);

    /**
     * 根据ID删除通知定义
     */
    void deleteById(String id);
}
