package com.ontology.platform.application.dto.job;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "提交Job响应DTO，包含任务ID和初始状态")
public class SubmitJobResponse {

    private String jobId;
    private String jobType;
    private String status;
    private Instant createdAt;
}
