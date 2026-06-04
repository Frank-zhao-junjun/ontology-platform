package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.infrastructure.persistence.entity.DataAccessMethodEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataAccessMethodJpaRepository extends JpaRepository<DataAccessMethodEntity, String> {
    boolean existsByObjectTypeIdAndDataSourceIdAndMethodType(String objectTypeId, String dataSourceId, String methodType);
}
