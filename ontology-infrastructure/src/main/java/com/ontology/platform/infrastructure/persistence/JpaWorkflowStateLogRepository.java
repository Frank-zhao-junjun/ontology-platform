package com.ontology.platform.infrastructure.persistence;

import com.ontology.platform.domain.entity.WorkflowStateLog;
import com.ontology.platform.domain.repository.WorkflowStateLogRepository;
import com.ontology.platform.infrastructure.repository.WorkflowStateLogJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JpaWorkflowStateLogRepository implements WorkflowStateLogRepository {
    private final WorkflowStateLogJpaRepository jpa;

    @Override
    public void save(WorkflowStateLog log) {
        jpa.save(PersistenceMapper.toEntity(log));
    }

    @Override
    public List<WorkflowStateLog> findByContextId(String contextId) {
        return jpa.findByContextIdOrderByOperatedAtDesc(contextId).stream()
                .map(PersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }
}
