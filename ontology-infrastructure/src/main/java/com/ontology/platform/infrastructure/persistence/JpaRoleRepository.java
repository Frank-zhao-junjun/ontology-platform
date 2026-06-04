package com.ontology.platform.infrastructure.persistence;

import com.ontology.platform.domain.entity.Role;
import com.ontology.platform.domain.repository.RoleRepository;
import com.ontology.platform.infrastructure.repository.RoleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JpaRoleRepository implements RoleRepository {
    private final RoleJpaRepository jpa;

    @Override public void save(Role role) { jpa.save(PersistenceMapper.toEntity(role)); }
    @Override public Optional<Role> findById(String id) { return jpa.findById(id).map(PersistenceMapper::toDomain); }
    @Override public List<Role> findAll() { return jpa.findAll().stream().map(PersistenceMapper::toDomain).collect(Collectors.toList()); }
    @Override public boolean existsByContextIdAndCode(String contextId, String code) { return jpa.existsByContextIdAndCode(contextId, code); }
}
