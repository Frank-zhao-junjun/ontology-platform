package com.ontology.platform.infrastructure.repository.governance;

import com.ontology.platform.domain.entity.governance.ApprovalRequest;
import com.ontology.platform.domain.repository.governance.ApprovalRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j @Repository
public class InMemoryApprovalRepository implements ApprovalRepository {
    private final Map<String, ApprovalRequest> store = new ConcurrentHashMap<>();
    public Optional<ApprovalRequest> findById(String id) { return Optional.ofNullable(store.get(id)); }
    public List<ApprovalRequest> findByAgentId(String agentId) {
        return store.values().stream().filter(a -> a.getAgentId().equals(agentId)).collect(Collectors.toList());
    }
    public List<ApprovalRequest> findPending() {
        return store.values().stream().filter(ApprovalRequest::isPending).collect(Collectors.toList());
    }
    public ApprovalRequest save(ApprovalRequest approval) { store.put(approval.getId(), approval); return approval; }
}
