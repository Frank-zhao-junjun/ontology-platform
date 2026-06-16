package com.ontology.platform.application.dto.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "NotificationDefinition响应")
public class NotificationDefinitionResponse {
    @Schema(description = "ID")
    private String id;
    @Schema(description = "通知名称")
    private String notifName;
    @Schema(description = "描述")
    private String description;
    @Schema(description = "渠道")
    private String channel;
    @Schema(description = "模板")
    private String template;
    @Schema(description = "收件人")
    private String recipients;
    @Schema(description = "触发事件")
    private String triggerEvent;
    @Schema(description = "是否启用")
    private Boolean enabled;
    @Schema(description = "配置")
    private String config;
    @Schema(description = "创建时间")
    private Instant createdAt;
    @Schema(description = "更新时间")
    private Instant updatedAt;
}
