package com.ontology.platform.application.dto.manifest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "Manifest预览响应DTO，包含变更列表和差异对比")
public class ManifestPreviewResponse {
    private String importId;
    private List<ChangeItem> changes;
    private String diff;
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ChangeItem { private String elementType; private String id; private String change; }
}
