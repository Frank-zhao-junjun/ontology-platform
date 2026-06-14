package com.ontology.platform.infrastructure.repository.governance;

import com.ontology.platform.domain.entity.governance.ApprovalRequest;
import com.ontology.platform.domain.repository.governance.ApprovalRepository;
import com.ontology.platform.infrastructure.converter.ApprovalRequestConverter;
import com.ontology.platform.infrastructure.persistence.ApprovalRequestPO;
import com.ontology.platform.infrastructure.persistence.ApprovalRequestPOMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ApprovalRepositoryImpl implements ApprovalRepository {

    private final ApprovalRequestPOMapper approvalRequestPOMapper;
    private final ApprovalRequestConverter approvalRequestConverter;

    @Override
    public Optional<ApprovalRequest> findById(String id) {
        log.debug("Finding approval request by id: {}", id);
        ApprovalRequestPO po = approvalRequestPOMapper.selectById(id);
        return Optional.ofNullable(approvalRequestConverter.toEntity(po));
    }

    @Override
    public List<ApprovalRequest> findByAgentId(String agentId) {
        log.debug("Finding approval requests by agentId: {}", agentId);
        List<ApprovalRequestPO> poList = approvalRequestPOMapper.selectByAgentId(agentId);
        return approvalRequestConverter.toEntityList(poList);
    }

    @Override
    public List<ApprovalRequest> findPending() {
        log.debug("Finding pending approval requests");
        List<ApprovalRequestPO> poList = approvalRequestPOMapper.selectPending();
        return approvalRequestConverter.toEntityList(poList);
    }

    @Override
    public ApprovalRequest save(ApprovalRequest approval) {
        log.debug("Saving approval request: {}", approval.getId());
        ApprovalRequestPO po = approvalRequestConverter.toPO(approval);
        // Upsert: delete existing then insert, or use insert/update
        if (approvalRequestPOMapper.selectById(approval.getId()) != null) {
            approvalRequestPOMapper.updateById(po);
        } else {
            approvalRequestPOMapper.insert(po);
        }
        return approval;
    }
}
