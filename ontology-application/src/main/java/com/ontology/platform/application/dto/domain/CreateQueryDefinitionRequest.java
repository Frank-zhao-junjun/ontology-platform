package com.ontology.platform.application.dto.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建QueryDefinition请求")
public class CreateQueryDefinitionRequest {
    @Schema(description = "查询名称")
    private String queryName;
    @Schema(description = "描述")
    private String description;
    @Schema(description = "查询类型")
    private String queryType;
    @Schema(description = "查询模板")
    private String queryTemplate;
    @Schema(description = "参数")
    private String parameters;
    @Schema(description = "结果Schema")
    private String resultSchema;
    @Schema(description = "超时(毫秒)")
    private Integer timeoutMs;
    @Schema(description = "是否启用")
    private Boolean enabled;
}
