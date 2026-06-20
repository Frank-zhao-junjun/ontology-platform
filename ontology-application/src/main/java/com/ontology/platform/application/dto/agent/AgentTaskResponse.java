package com.ontology.platform.application.dto.agent;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Agent 任务执行结果")
public class AgentTaskResponse {

    @Schema(description = "Agent 类型", example = "kimi")
    private String agentType;

    @Schema(description = "执行状态", example = "SUCCESS", allowableValues = {"SUCCESS", "FAILURE", "TIMEOUT"})
    private String status;

    @Schema(description = "标准输出")
    private String output;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "执行耗时（毫秒）", example = "1500")
    private Long durationMs;
}
