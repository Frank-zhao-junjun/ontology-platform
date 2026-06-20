package com.ontology.platform.application.dto.manifest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "Manifest导出请求DTO，指定导出格式")
public class ManifestExportRequest { private String format; }
