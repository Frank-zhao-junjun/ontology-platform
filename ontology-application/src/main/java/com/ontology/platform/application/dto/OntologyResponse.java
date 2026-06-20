package com.ontology.platform.application.dto;

import com.ontology.platform.common.enums.OntologyStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "本体响应")
public class OntologyResponse {

    @Schema(description = "本体ID")
    private String id;
    @Schema(description = "本体名称")
    private String name;
    @Schema(description = "显示名称")
    private String displayName;
    @Schema(description = "描述")
    private String description;
    @Schema(description = "版本号")
    private String version;
    @Schema(description = "状态")
    @Schema(description = "状态")
    private OntologyStatus status;
    @Schema(description = "发布时间")
    private Instant publishedAt;
    @Schema(description = "对象类型数量")
    private int objectTypeCount;
    @Schema(description = "操作类型数量")
    private int actionTypeCount;
    @Schema(description = "创建时间")
    private Instant createdAt;
    @Schema(description = "更新时间")
    private Instant updatedAt;
}
