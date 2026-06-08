package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.infrastructure.persistence.entity.RelationshipEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RelationshipJpaRepository extends JpaRepository<RelationshipEntity, String> {
    List<RelationshipEntity> findByContextId(String contextId);
    List<RelationshipEntity> findByIsCrossContextTrue();
    @Query("SELECT DISTINCT r.contextId FROM RelationshipEntity r WHERE r.isCrossContext = true")
    List<String> findDistinctContextIdsByIsCrossContextTrue();
}