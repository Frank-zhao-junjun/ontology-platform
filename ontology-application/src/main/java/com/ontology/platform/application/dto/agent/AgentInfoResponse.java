package com.ontology.platform.application.dto.agent;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Agent 信息")
public class AgentInfoResponse {

    @Schema(description = "Agent 类型", example = "kimi")
    private String agentType;

    @Schema(description = "是否可用", example = "true")
    private boolean available;

    @Schema(description = "描述", example = "Kimi Code CLI — 代码生成与审查")
    private String description;
}
