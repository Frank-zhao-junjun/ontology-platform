package com.ontology.platform.application.dto.manifest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "Manifest导入请求DTO，包含源格式和原始内容")
public class ImportManifestRequest {
    private String sourceFormat; // YAML or JSON
    private String rawContent;
    private String createdBy;
}
