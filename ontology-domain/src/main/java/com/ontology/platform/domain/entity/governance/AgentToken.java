package com.ontology.platform.domain.entity.governance;

import com.ontology.platform.common.enums.TokenStatus;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentToken {

    private String id;
    private String agentId;
    private String tokenHash;
    private String tenantId;
    private String displayName;
    private TokenStatus status;
    private Instant issuedAt;
    private Instant expiresAt;
    private Instant lastUsedAt;
    private String createdBy;
    private Instant createdAt;

    public static AgentToken create(String agentId, String tokenHash, String tenantId,
                                     String displayName, String createdBy, long ttlDays) {
        return AgentToken.builder()
                .id(UUID.randomUUID().toString())
                .agentId(agentId)
                .tokenHash(tokenHash)
                .tenantId(tenantId)
                .displayName(displayName)
                .status(TokenStatus.ACTIVE)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(ttlDays * 86400))
                .createdBy(createdBy)
                .createdAt(Instant.now())
                .build();
    }

    public boolean isActive() {
        return status == TokenStatus.ACTIVE && Instant.now().isBefore(expiresAt);
    }

    public void markUsed() {
        this.lastUsedAt = Instant.now();
    }

    public void suspend() {
        this.status = TokenStatus.SUSPENDED;
    }

    public void revoke() {
        this.status = TokenStatus.REVOKED;
    }
}
