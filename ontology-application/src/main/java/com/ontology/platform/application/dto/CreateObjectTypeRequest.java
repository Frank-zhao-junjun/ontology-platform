package com.ontology.platform.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建对象类型请求")
public class CreateObjectTypeRequest {

    @NotBlank(message = "本体ID不能为空")
    @Schema(description = "所属本体ID")
    private String ontologyId;

    @NotBlank(message = "对象类型名称不能为空")
    @Pattern(regexp = "^[a-z][a-z0-9_]*$", message = "名称必须以小写字母开头，只能包含小写字母、数字和下划线")
    @Size(min = 1, max = 100, message = "名称长度必须在1-100之间")
    @Schema(description = "对象类型名称", example = "customer")
    private String name;

    @NotBlank(message = "显示名称不能为空")
    @Size(min = 1, max = 200, message = "显示名称长度必须在1-200之间")
    @Schema(description = "显示名称", example = "客户")
    private String displayName;

    @Size(max = 1000, message = "描述长度不能超过1000")
    @Schema(description = "描述", example = "客户基本信息")
    private String description;

    @NotBlank(message = "主键属性名不能为空")
    @Schema(description = "主键属性名", example = "id")
    private String primaryKey;

    @Schema(description = "父对象类型ID")
    private String parentId;

    @Schema(description = "实体角色: aggregate_root | child_entity", example = "aggregate_root")
    private String entityRole;

    @Schema(description = "父聚合根 ID (child_entity 时必填)")
    private String parentAggregateId;

    @Schema(description = "所属业务场景 ID")
    private String businessScenarioId;

    @Schema(description = "子领域")
    private String subDomain;

    @Builder.Default
    @Schema(description = "实现的接口列表")
    private List<String> interfaceNames = new ArrayList<>();
}
