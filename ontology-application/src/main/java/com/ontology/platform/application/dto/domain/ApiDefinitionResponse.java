package com.ontology.platform.application.dto.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "ApiDefinition响应")
public class ApiDefinitionResponse {
    @Schema(description = "ID")
    private String id;
    @Schema(description = "API名称")
    private String apiName;
    @Schema(description = "描述")
    private String description;
    @Schema(description = "URL")
    private String url;
    @Schema(description = "HTTP方法")
    private String httpMethod;
    @Schema(description = "请求Schema")
    private String requestSchema;
    @Schema(description = "响应Schema")
    private String responseSchema;
    @Schema(description = "认证类型")
    private String authType;
    @Schema(description = "限流")
    private Integer rateLimit;
    @Schema(description = "超时(毫秒)")
    private Integer timeoutMs;
    @Schema(description = "是否启用")
    private Boolean enabled;
    @Schema(description = "创建时间")
    private Instant createdAt;
    @Schema(description = "更新时间")
    private Instant updatedAt;
}
