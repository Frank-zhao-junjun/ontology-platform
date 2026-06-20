package com.ontology.platform.application.dto.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "状态转换响应DTO，描述状态机中的状态迁移规则")
public class StateTransitionResponse {
    private String id;
    private String fromState;
    private String toState;
    private String trigger;
    private String guardCondition;
}
