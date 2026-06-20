package com.ontology.platform.application.service.agent;

import lombok.Builder;
import lombok.Getter;

/**
 * Agent 桥接端口 — 定义应用层与基础设施层之间的契约。
 * 基础设施层的 {@code AgentBridgeService} 实现此端口。
 */
public interface AgentBridgePort {

    AgentResult executeKimi(String prompt, String cwd, Long timeoutSec);

    AgentResult executeClaude(String prompt, String cwd, Long timeoutSec,
                              Integer maxTurns, String model);

    AgentResult executeCodex(String prompt, String cwd, Long timeoutSec);

    /**
     * Agent 执行结果值对象。
     */
    @Getter
    @Builder
    final class AgentResult {
        private final String status;       // SUCCESS / FAILURE / TIMEOUT
        private final String output;
        private final String errorMessage;
        private final Long durationMs;
    }
}
