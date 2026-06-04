package com.ontology.platform.infrastructure.persistence;

import com.ontology.platform.domain.entity.DataSource;
import com.ontology.platform.domain.repository.DataSourceRepository;
import com.ontology.platform.infrastructure.repository.DataSourceJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JpaDataSourceRepository implements DataSourceRepository {
    private final DataSourceJpaRepository jpa;

    @Override public void save(DataSource dataSource) { jpa.save(PersistenceMapper.toEntity(dataSource)); }
    @Override public Optional<DataSource> findById(String id) { return jpa.findById(id).map(PersistenceMapper::toDomain); }
    @Override public List<DataSource> findAll() { return jpa.findAll().stream().map(PersistenceMapper::toDomain).collect(Collectors.toList()); }
    @Override public boolean existsByCode(String code) { return jpa.existsByCode(code); }
}
