package com.ontology.platform.infrastructure.persistence;

import com.ontology.platform.domain.entity.DataAccessMethod;
import com.ontology.platform.domain.repository.DataAccessMethodRepository;
import com.ontology.platform.infrastructure.repository.DataAccessMethodJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JpaDataAccessMethodRepository implements DataAccessMethodRepository {
    private final DataAccessMethodJpaRepository jpa;

    @Override public void save(DataAccessMethod method) { jpa.save(PersistenceMapper.toEntity(method)); }
    @Override public boolean existsByObjectTypeAndSourceAndMethod(String objectTypeId, String dataSourceId, String methodType) {
        return jpa.existsByObjectTypeIdAndDataSourceIdAndMethodType(objectTypeId, dataSourceId, methodType);
    }
}
