package com.ontology.platform.infrastructure.persistence;

import com.ontology.platform.domain.entity.ValueObject;
import com.ontology.platform.domain.repository.ValueObjectRepository;
import com.ontology.platform.infrastructure.persistence.entity.ValueObjectEntity;
import com.ontology.platform.infrastructure.repository.ValueObjectJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JpaValueObjectRepository implements ValueObjectRepository {
    private final ValueObjectJpaRepository jpa;

    @Override
    public ValueObject save(ValueObject vo) {
        ValueObjectEntity entity = PersistenceMapper.toEntity(vo);
        ValueObjectEntity saved = jpa.save(entity);
        return PersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<ValueObject> findById(String id) {
        return jpa.findById(id).map(PersistenceMapper::toDomain);
    }

    @Override
    public List<ValueObject> findAll() {
        return jpa.findAllByOrderByCreatedAtAsc().stream()
                .map(PersistenceMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public boolean existsByCode(String code) {
        return jpa.existsByCode(code);
    }
}
