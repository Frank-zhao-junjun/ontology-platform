package com.ontology.platform.domain.repository.governance;
import com.ontology.platform.domain.entity.governance.AgentRole;
import java.util.List;
import java.util.Optional;

public interface AgentRoleRepository {
    Optional<AgentRole> findById(String id);
    List<AgentRole> findByTokenId(String tokenId);
    List<AgentRole> findByDomain(String domain);
    AgentRole save(AgentRole role);
}
