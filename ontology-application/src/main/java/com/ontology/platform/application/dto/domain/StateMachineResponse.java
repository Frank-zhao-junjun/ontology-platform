package com.ontology.platform.application.dto.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "状态机响应DTO，包含状态定义和转换规则")
public class StateMachineResponse {
    private String id;
    private String name;
    private String entityId;
    private String initialState;
    private String states;
    private List<StateTransitionResponse> transitions;
}
