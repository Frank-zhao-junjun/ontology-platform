package com.ontology.platform.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AggregateRootCreateRequest {
    @NotBlank @Schema(description = "聚合根名称", example = "生产订单") private String name;
    @NotBlank @Schema(description = "聚合根代码", example = "ProductionOrder") private String code;
    @Schema(description = "描述") private String description;
}
