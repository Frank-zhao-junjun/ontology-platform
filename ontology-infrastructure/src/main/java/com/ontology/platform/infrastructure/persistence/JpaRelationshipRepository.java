package com.ontology.platform.infrastructure.persistence;

import com.ontology.platform.domain.entity.Relationship;
import com.ontology.platform.domain.repository.RelationshipRepository;
import com.ontology.platform.infrastructure.repository.RelationshipJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JpaRelationshipRepository implements RelationshipRepository {
    private final RelationshipJpaRepository jpa;

    @Override public void save(Relationship relationship) { jpa.save(PersistenceMapper.toEntity(relationship)); }
    @Override public List<Relationship> findByContextId(String contextId) {
        return jpa.findByContextId(contextId).stream().map(PersistenceMapper::toDomain).collect(Collectors.toList());
    }
}
