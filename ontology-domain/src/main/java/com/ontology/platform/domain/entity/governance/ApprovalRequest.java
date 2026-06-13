package com.ontology.platform.domain.entity.governance;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class ApprovalRequest {
    private String id;
    private String agentId;
    private String actionId;
    private String requestedOp;
    private String status;
    private String reason;
    private Instant requestedAt;
    private Instant resolvedAt;
    private String resolvedBy;

    public static ApprovalRequest submit(String agentId, String actionId, String requestedOp) {
        return ApprovalRequest.builder()
                .id(UUID.randomUUID().toString())
                .agentId(agentId)
                .actionId(actionId)
                .requestedOp(requestedOp)
                .status("PENDING")
                .requestedAt(Instant.now())
                .build();
    }

    public void approve(String resolvedBy) {
        this.status = "APPROVED";
        this.resolvedBy = resolvedBy;
        this.resolvedAt = Instant.now();
    }

    public void reject(String resolvedBy, String reason) {
        this.status = "REJECTED";
        this.resolvedBy = resolvedBy;
        this.reason = reason;
        this.resolvedAt = Instant.now();
    }

    public boolean isPending() { return "PENDING".equals(status); }
}
