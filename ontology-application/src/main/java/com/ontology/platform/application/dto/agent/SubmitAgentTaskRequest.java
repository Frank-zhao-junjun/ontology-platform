package com.ontology.platform.application.dto.agent;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitAgentTaskRequest {

    @NotBlank(message = "agent 类型不能为空")
    private String agentType;   // kimi / claude / codex

    @NotBlank(message = "prompt 不能为空")
    private String prompt;

    private String cwd;         // 工作目录，默认当前项目
    private Integer timeout;    // 超时秒数
    private Integer maxTurns;   // Claude 专用
    private String model;       // 可选模型
}
