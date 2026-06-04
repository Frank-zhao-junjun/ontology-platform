package com.ontology.platform.infrastructure.persistence;

import com.ontology.platform.domain.entity.ObjectTypeV2;
import com.ontology.platform.domain.repository.ObjectTypeRepository;
import com.ontology.platform.infrastructure.repository.ObjectTypeJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JpaObjectTypeRepository implements ObjectTypeRepository {
    private final ObjectTypeJpaRepository jpa;

    @Override public void save(ObjectTypeV2 objectType) { jpa.save(PersistenceMapper.toEntity(objectType)); }
    @Override public Optional<ObjectTypeV2> findById(String id) { return jpa.findById(id).map(PersistenceMapper::toDomain); }
    @Override public List<ObjectTypeV2> findByContextId(String contextId) {
        return jpa.findByContextId(contextId).stream().map(PersistenceMapper::toDomain).collect(Collectors.toList());
    }
}
