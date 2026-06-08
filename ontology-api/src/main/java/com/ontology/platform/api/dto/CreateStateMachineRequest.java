package com.ontology.platform.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateStateMachineRequest {
    @NotBlank(message = "状态机名称不能为空")
    private String name;
    private String nameEn;
    @NotBlank(message = "对象类型ID不能为空")
    private String objectTypeId;
    private String statusField = "status";
    private String statesJson = "[]";
    private String transitionsJson = "[]";
}
