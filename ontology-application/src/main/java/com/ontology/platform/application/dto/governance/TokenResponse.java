package com.ontology.platform.application.dto.governance;

import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class TokenResponse {
    private String id;
    private String agentId;
    private String token;
    private String tenantId;
    private String displayName;
    private String status;
    private List<String> domains;
    private Instant issuedAt;
    private Instant expiresAt;
    private Instant lastUsedAt;
}
