package com.ontology.platform.domain.repository;

import com.ontology.platform.domain.entity.ValueObject;

import java.util.List;
import java.util.Optional;

public interface ValueObjectRepository {
    ValueObject save(ValueObject vo);
    Optional<ValueObject> findById(String id);
    List<ValueObject> findAll();
    boolean existsByCode(String code);
}
