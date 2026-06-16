package com.ontology.platform.domain.entity;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobRecord {

    private UUID id;
    private String jobType;
    private String tenantId;
    private String agentId;
    private String idempotencyKey;
    private String status;
    private String payload;     // JSONB stored as String
    private String result;      // JSONB stored as String
    private String errorMessage;
    private Integer retryCount;
    private Integer maxRetries;
    private Instant nextRetryAt;
    private Instant createdAt;
    private Instant startedAt;
    private Instant completedAt;
}
