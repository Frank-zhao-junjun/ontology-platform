package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.infrastructure.persistence.entity.AgentSandboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentSandboxJpaRepository extends JpaRepository<AgentSandboxEntity, String> {
}
