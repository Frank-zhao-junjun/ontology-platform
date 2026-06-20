package com.ontology.platform.application.dto.manifest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.Instant;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "Manifest发布响应DTO，包含发布版本号和时间")
public class ManifestPublishResponse { private String version; private Instant publishedAt; }
