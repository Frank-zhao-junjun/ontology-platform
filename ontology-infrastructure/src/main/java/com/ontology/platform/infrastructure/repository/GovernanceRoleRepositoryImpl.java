package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.domain.entity.GovernanceRole;
import com.ontology.platform.domain.repository.GovernanceRoleRepository;
import com.ontology.platform.infrastructure.converter.GovernanceRoleConverter;
import com.ontology.platform.infrastructure.persistence.GovernanceRolePOMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GovernanceRoleRepositoryImpl implements GovernanceRoleRepository {
    private final GovernanceRolePOMapper mapper;
    private final GovernanceRoleConverter converter;

    @Override public GovernanceRole save(GovernanceRole role) {
        var po = converter.toPO(role);
        if (mapper.selectById(po.getId()) != null) mapper.updateById(po);
        else mapper.insert(po);
        return converter.toDomain(mapper.selectById(po.getId()));
    }

    @Override public Optional<GovernanceRole> findById(String id) {
        return Optional.ofNullable(converter.toDomain(mapper.selectById(id)));
    }

    @Override public List<GovernanceRole> findByOntologyId(String ontologyId) {
        return mapper.selectByOntologyId(ontologyId).stream()
                .map(converter::toDomain).toList();
    }

    @Override public void deleteById(String id) { mapper.deleteById(id); }
}
