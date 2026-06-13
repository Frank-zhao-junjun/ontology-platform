package com.ontology.platform.domain.repository.governance;
import com.ontology.platform.domain.entity.governance.ApprovalRequest;
import java.util.List;
import java.util.Optional;

public interface ApprovalRepository {
    Optional<ApprovalRequest> findById(String id);
    List<ApprovalRequest> findByAgentId(String agentId);
    List<ApprovalRequest> findPending();
    ApprovalRequest save(ApprovalRequest approval);
}
