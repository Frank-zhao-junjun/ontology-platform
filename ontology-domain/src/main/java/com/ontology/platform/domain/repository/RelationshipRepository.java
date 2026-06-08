package com.ontology.platform.domain.repository;

import com.ontology.platform.domain.entity.Relationship;
import java.util.List;

public interface RelationshipRepository {
    void save(Relationship relationship);
    List<Relationship> findByContextId(String contextId);
    List<Relationship> findAllCrossContext();
    List<Relationship> findBySourceContextId(String contextId);
}