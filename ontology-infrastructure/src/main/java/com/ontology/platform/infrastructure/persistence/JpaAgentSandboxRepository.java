package com.ontology.platform.infrastructure.persistence;

import com.ontology.platform.domain.entity.AgentSandbox;
import com.ontology.platform.domain.repository.AgentSandboxRepository;
import com.ontology.platform.infrastructure.repository.AgentSandboxJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JpaAgentSandboxRepository implements AgentSandboxRepository {
    private final AgentSandboxJpaRepository jpa;

    @Override public void save(AgentSandbox sandbox) { jpa.save(PersistenceMapper.toEntity(sandbox)); }
    @Override public List<AgentSandbox> findAll() {
        return jpa.findAll().stream().map(PersistenceMapper::toDomain).collect(Collectors.toList());
    }
}
