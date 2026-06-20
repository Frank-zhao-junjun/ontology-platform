package com.ontology.platform.application.dto.agent;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "提交 Agent 任务请求")
public class SubmitAgentTaskRequest {

    @NotBlank(message = "agent 类型不能为空")
    @Pattern(regexp = "^(kimi|claude|codex)$", message = "agent 类型必须是 kimi / claude / codex")
    @Schema(description = "Agent 类型", example = "kimi", allowableValues = {"kimi", "claude", "codex"})
    private String agentType;

    @NotBlank(message = "prompt 不能为空")
    @Size(max = 50_000, message = "prompt 长度不能超过 50,000 字符")
    @Schema(description = "任务指令", example = "分析项目目录结构", maxLength = 50000)
    private String prompt;

    @Schema(description = "工作目录（默认项目根目录）", example = "D:\\AI\\ontology-platform")
    private String cwd;

    @Schema(description = "超时秒数（默认 300）", example = "300", minimum = "1", maximum = "1800")
    private Integer timeout;

    @Schema(description = "Claude 专用：最大对话轮次", example = "20")
    private Integer maxTurns;

    @Schema(description = "可选模型", example = "claude-sonnet-4")
    private String model;
}
