package com.ontology.platform.infrastructure.repository.governance;

import com.ontology.platform.domain.entity.governance.AgentToken;
import com.ontology.platform.domain.repository.governance.AgentTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class InMemoryAgentTokenRepository implements AgentTokenRepository {

    // TODO: Phase 2 replace with MyBatis-Plus or JPA
    private final Map<String, AgentToken> store = new ConcurrentHashMap<>();

    @Override
    public Optional<AgentToken> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<AgentToken> findByAgentId(String agentId) {
        return store.values().stream()
                .filter(t -> t.getAgentId().equals(agentId))
                .findFirst();
    }

    @Override
    public List<AgentToken> findByTenantId(String tenantId) {
        return store.values().stream()
                .filter(t -> t.getTenantId().equals(tenantId))
                .collect(Collectors.toList());
    }

    @Override
    public AgentToken save(AgentToken token) {
        store.put(token.getId(), token);
        return token;
    }

    @Override
    public void deleteById(String id) {
        store.remove(id);
    }
}
