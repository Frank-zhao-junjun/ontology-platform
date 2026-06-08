package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.infrastructure.persistence.entity.WorkflowStateLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkflowStateLogJpaRepository extends JpaRepository<WorkflowStateLogEntity, String> {
    List<WorkflowStateLogEntity> findByContextIdOrderByOperatedAtDesc(String contextId);
}
