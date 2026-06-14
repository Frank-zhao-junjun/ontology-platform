package com.ontology.platform.application.dto.job;

import lombok.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobResponse {

    private UUID jobId;
    private String jobType;
    private String tenantId;
    private String agentId;
    private String status;
    private Map<String, Object> payload;
    private Map<String, Object> result;
    private String errorMessage;
    private int retryCount;
    private int maxRetries;
    private Instant createdAt;
    private Instant startedAt;
    private Instant completedAt;
}
