package com.ontology.platform.application.dto.agent;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentInfoResponse {

    private String agentType;    // kimi / claude / codex
    private boolean available;   // 是否可用
    private String description;
}
