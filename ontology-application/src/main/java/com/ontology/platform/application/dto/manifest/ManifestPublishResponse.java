package com.ontology.platform.application.dto.manifest;
import lombok.*;
import java.time.Instant;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ManifestPublishResponse { private String version; private Instant publishedAt; }
