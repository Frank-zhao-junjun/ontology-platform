package com.ontology.platform.application.dto.job;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Job执行结果响应DTO，包含任务状态和输出")
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
