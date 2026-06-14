package com.ontology.platform.infrastructure.converter;

import com.ontology.platform.domain.entity.governance.ApprovalRequest;
import com.ontology.platform.infrastructure.persistence.ApprovalRequestPO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ApprovalRequestConverter {

    public ApprovalRequest toEntity(ApprovalRequestPO po) {
        if (po == null) return null;
        return ApprovalRequest.builder()
                .id(po.getId())
                .agentId(po.getAgentId())
                .actionId(po.getActionId())
                .requestedOp(po.getRequestedOp())
                .status(po.getStatus())
                .reason(po.getReason())
                .requestedAt(po.getRequestedAt())
                .resolvedAt(po.getResolvedAt())
                .resolvedBy(po.getResolvedBy())
                .build();
    }

    public ApprovalRequestPO toPO(ApprovalRequest entity) {
        if (entity == null) return null;
        return ApprovalRequestPO.builder()
                .id(entity.getId())
                .agentId(entity.getAgentId())
                .actionId(entity.getActionId())
                .requestedOp(entity.getRequestedOp())
                .status(entity.getStatus())
                .reason(entity.getReason())
                .requestedAt(entity.getRequestedAt())
                .resolvedAt(entity.getResolvedAt())
                .resolvedBy(entity.getResolvedBy())
                .build();
    }

    public List<ApprovalRequest> toEntityList(List<ApprovalRequestPO> poList) {
        if (poList == null) return List.of();
        return poList.stream().map(this::toEntity).collect(Collectors.toList());
    }
}
