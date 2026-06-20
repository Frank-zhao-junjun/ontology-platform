package com.ontology.platform.application.dto.webhook;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Webhook配置响应DTO，包含回调URL、监听事件类型和状态")
public class WebhookResponse {

    private UUID id;
    private String tenantId;
    private String agentId;
    private String callbackUrl;
    private List<String> eventTypes;
    private boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
}
