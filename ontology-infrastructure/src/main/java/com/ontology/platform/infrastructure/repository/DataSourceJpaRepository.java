package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.infrastructure.persistence.entity.DataSourceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DataSourceJpaRepository extends JpaRepository<DataSourceEntity, String> {
    boolean existsByCode(String code);
    List<DataSourceEntity> findBySourceType(String sourceType);
}
