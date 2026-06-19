package com.ontology.platform.application.dto.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "创建MetadataTemplate请求")
public class CreateMetadataTemplateRequest {
    @Schema(description = "name")
    private String name;
    @Schema(description = "name_en")
    private String nameEn;
    @Schema(description = "description")
    private String description;
    @Schema(description = "domain")
    private String domain;
    @Schema(description = "template_type")
    private String templateType;
}
