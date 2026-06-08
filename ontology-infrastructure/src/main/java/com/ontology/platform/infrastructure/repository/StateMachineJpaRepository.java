package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.infrastructure.persistence.entity.StateMachineEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StateMachineJpaRepository extends JpaRepository<StateMachineEntity, String> {
    List<StateMachineEntity> findByContextIdOrderByCreatedAtAsc(String contextId);
    List<StateMachineEntity> findByObjectTypeId(String objectTypeId);
    boolean existsByContextIdAndObjectTypeId(String contextId, String objectTypeId);
}
