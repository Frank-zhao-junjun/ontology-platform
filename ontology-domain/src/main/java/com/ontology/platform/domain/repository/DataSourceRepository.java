package com.ontology.platform.domain.repository;

import com.ontology.platform.domain.entity.DataSource;
import java.util.List;
import java.util.Optional;

public interface DataSourceRepository {
    void save(DataSource dataSource);
    Optional<DataSource> findById(String id);
    List<DataSource> findAll();
    boolean existsByCode(String code);
}
