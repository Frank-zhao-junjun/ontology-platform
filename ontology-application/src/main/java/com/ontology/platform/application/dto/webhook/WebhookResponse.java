package com.ontology.platform.application.dto.webhook;

import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookResponse {

    private UUID id;
    private String tenantId;
    private String agentId;
    private String callbackUrl;
    private List<String> eventTypes;
    private boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
}
