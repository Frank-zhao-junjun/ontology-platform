package com.ontology.platform.application.dto.job;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitJobRequest {

    @NotBlank(message = "jobType不能为空")
    private String jobType;

    private Map<String, Object> payload;

    private String idempotencyKey;
}
