package com.ontology.platform.application.dto.agent;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentTaskResponse {

    private String agentType;
    private String status;       // SUCCESS / FAILURE / TIMEOUT
    private String output;       // 标准输出
    private String errorMessage; // 错误信息
    private Long durationMs;     // 执行耗时
}
