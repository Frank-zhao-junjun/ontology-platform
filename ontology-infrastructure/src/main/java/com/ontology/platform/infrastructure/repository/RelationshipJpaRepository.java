package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.infrastructure.persistence.entity.RelationshipEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RelationshipJpaRepository extends JpaRepository<RelationshipEntity, String> {
    List<RelationshipEntity> findByContextId(String contextId);
}
