package com.ontology.platform.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "约束定义")
public class ConstraintDefinition {

    @NotBlank(message = "约束类型不能为空")
    @Schema(description = "约束类型", example = "minLength")
    private String type;

    @NotNull(message = "约束值不能为空")
    @Schema(description = "约束值")
    private Object value;

    @Schema(description = "校验失败时的错误消息")
    private String errorMessage;
}
