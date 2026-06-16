package com.ontology.platform.application.dto.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "ComputeDefinition响应")
public class ComputeDefinitionResponse {
    @Schema(description = "ID")
    private String id;
    @Schema(description = "计算名称")
    private String computeName;
    @Schema(description = "描述")
    private String description;
    @Schema(description = "输入Schema")
    private String inputSchema;
    @Schema(description = "公式")
    private String formula;
    @Schema(description = "输出类型")
    private String outputType;
    @Schema(description = "输出Schema")
    private String outputSchema;
    @Schema(description = "超时(毫秒)")
    private Integer timeoutMs;
    @Schema(description = "是否启用")
    private Boolean enabled;
    @Schema(description = "创建时间")
    private Instant createdAt;
    @Schema(description = "更新时间")
    private Instant updatedAt;
}
