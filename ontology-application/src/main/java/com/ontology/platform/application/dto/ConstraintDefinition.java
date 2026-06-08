package com.ontology.platform.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * 约束定义DTO
 * Constraint Definition DTO
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConstraintDefinition {

    @NotBlank(message = "约束类型不能为空")
    private String type;

    @NotNull(message = "约束值不能为空")
    private Object value;

    private String errorMessage;
}
