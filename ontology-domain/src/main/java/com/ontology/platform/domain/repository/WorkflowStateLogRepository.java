package com.ontology.platform.domain.repository;

import com.ontology.platform.domain.entity.WorkflowStateLog;

import java.util.List;

public interface WorkflowStateLogRepository {
    void save(WorkflowStateLog log);
    List<WorkflowStateLog> findByContextId(String contextId);
}
