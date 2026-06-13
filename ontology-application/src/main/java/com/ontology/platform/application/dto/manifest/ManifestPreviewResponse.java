package com.ontology.platform.application.dto.manifest;
import lombok.*;
import java.util.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ManifestPreviewResponse {
    private String importId;
    private List<ChangeItem> changes;
    private String diff;
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ChangeItem { private String elementType; private String id; private String change; }
}
