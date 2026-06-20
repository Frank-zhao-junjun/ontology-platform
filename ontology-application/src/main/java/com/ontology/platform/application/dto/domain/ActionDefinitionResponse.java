package com.ontology.platform.application.dto.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "动作定义响应DTO，包含动作类型、风险等级和执行模式")
public class ActionDefinitionResponse {
    private String id;
    private String name;
    private String displayName;
    private String description;
    private String actionType;
    private String domain;
    private String riskLevel;
    private Boolean isAsync;
    private Integer timeoutMs;
    private String entityId;
    private String inputSchema;
    private String outputSchema;
    private String preRules;
    private String postRules;
    private List<StateMachineResponse> stateMachines;
}
