package com.ontology.platform.application.dto.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "ProbeDefinition响应")
public class ProbeDefinitionResponse {
    @Schema(description = "ID")
    private String id;
    @Schema(description = "探针名称")
    private String probeName;
    @Schema(description = "描述")
    private String description;
    @Schema(description = "目标")
    private String target;
    @Schema(description = "探针类型")
    private String probeType;
    @Schema(description = "频率(秒)")
    private Integer frequencySec;
    @Schema(description = "超时(毫秒)")
    private Integer timeoutMs;
    @Schema(description = "告警条件")
    private String alertCondition;
    @Schema(description = "告警级别")
    private String alertSeverity;
    @Schema(description = "是否启用")
    private Boolean enabled;
    @Schema(description = "配置")
    private String config;
    @Schema(description = "创建时间")
    private Instant createdAt;
    @Schema(description = "更新时间")
    private Instant updatedAt;
}
