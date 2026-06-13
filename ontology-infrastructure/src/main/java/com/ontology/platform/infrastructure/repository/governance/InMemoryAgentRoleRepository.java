package com.ontology.platform.infrastructure.repository.governance;

import com.ontology.platform.domain.entity.governance.AgentRole;
import com.ontology.platform.domain.repository.governance.AgentRoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j @Repository
public class InMemoryAgentRoleRepository implements AgentRoleRepository {
    private final Map<String, AgentRole> store = new ConcurrentHashMap<>();
    public Optional<AgentRole> findById(String id) { return Optional.ofNullable(store.get(id)); }
    public List<AgentRole> findByTokenId(String tokenId) {
        return store.values().stream().filter(r -> r.getTokenId().equals(tokenId)).collect(Collectors.toList());
    }
    public List<AgentRole> findByDomain(String domain) {
        return store.values().stream().filter(r -> r.getDomain().equals(domain)).collect(Collectors.toList());
    }
    public AgentRole save(AgentRole role) { store.put(role.getId(), role); return role; }
}
