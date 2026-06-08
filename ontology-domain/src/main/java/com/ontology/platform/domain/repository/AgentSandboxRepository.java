package com.ontology.platform.domain.repository;

import com.ontology.platform.domain.entity.AgentSandbox;
import java.util.List;

public interface AgentSandboxRepository {
    void save(AgentSandbox sandbox);
    List<AgentSandbox> findAll();
}
