package com.ontology.platform.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RelationshipCreateRequest {
    @NotBlank
    @Schema(description = "源对象类型ID", example = "uuid-of-source-object")
    private String sourceObjectId;

    @NotBlank
    @Schema(description = "目标对象类型ID", example = "uuid-of-target-object")
    private String targetObjectId;

    @NotBlank
    @Schema(description = "关系名称", example = "包含")
    private String name;

    @NotBlank
    @Pattern(regexp = "^[a-z][a-z0-9_]*$")
    @Size(min = 1, max = 50)
    @Schema(description = "关系代码", example = "contains")
    private String code;

    @Schema(description = "基数: 1:1 / 1:N / N:M", example = "1:N", defaultValue = "1:N")
    private String cardinality = "1:N";

    @Schema(description = "关系类型: COMPOSITION(组合)/AGGREGATION(聚合)/REFERENCE(引用)/DEPENDENCY(依赖)", example = "COMPOSITION", defaultValue = "REFERENCE")
    private String relationKind = "REFERENCE";

    @Schema(description = "是否跨上下文关系", defaultValue = "false")
    private boolean crossContext = false;

    @Schema(description = "跨上下文时的目标上下文ID（crossContext=true 时必填）", example = "uuid-of-target-context")
    private String targetContextId;
}
