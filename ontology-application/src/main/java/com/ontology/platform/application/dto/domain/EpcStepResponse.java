package com.ontology.platform.application.dto.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "EPC步骤响应DTO，描述事件驱动流程中的步骤定义")
public class EpcStepResponse {
    private String id;
    private String flowName;
    private Integer stepOrder;
    private String triggerEventId;
    private String triggerEventName;
    private String actionId;
    private String actionName;
    private String conditions;
    private String guards;
    private Integer timeoutMs;
}
