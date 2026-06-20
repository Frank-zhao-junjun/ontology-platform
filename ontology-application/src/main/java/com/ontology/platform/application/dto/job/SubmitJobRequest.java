package com.ontology.platform.application.dto.job;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "提交Job请求DTO")
public class SubmitJobRequest {

    @NotBlank(message = "jobType不能为空")
    private String jobType;

    private Map<String, Object> payload;

    private String idempotencyKey;
}
