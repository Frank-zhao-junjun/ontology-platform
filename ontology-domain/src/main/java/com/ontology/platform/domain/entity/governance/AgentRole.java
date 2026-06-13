package com.ontology.platform.domain.entity.governance;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class AgentRole {
    private String id;
    private String tokenId;
    private String domain;
    private String role;
    private Instant grantedAt;

    public static AgentRole create(String tokenId, String domain, String role) {
        return AgentRole.builder()
                .id(UUID.randomUUID().toString())
                .tokenId(tokenId)
                .domain(domain)
                .role(role)
                .grantedAt(Instant.now())
                .build();
    }
}
