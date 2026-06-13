package com.ontology.platform.domain.repository.governance;

import com.ontology.platform.domain.entity.governance.AgentToken;
import java.util.List;
import java.util.Optional;

public interface AgentTokenRepository {
    Optional<AgentToken> findById(String id);
    Optional<AgentToken> findByAgentId(String agentId);
    List<AgentToken> findByTenantId(String tenantId);
    AgentToken save(AgentToken token);
    void deleteById(String id);
}
