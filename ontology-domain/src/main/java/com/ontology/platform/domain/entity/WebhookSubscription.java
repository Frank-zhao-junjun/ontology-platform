package com.ontology.platform.domain.entity;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookSubscription {

    private UUID id;
    private String tenantId;
    private String agentId;
    private String callbackUrl;
    private String eventTypes; // JSONB stored as String
    private String secret;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
}
