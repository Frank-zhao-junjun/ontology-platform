package com.ontology.platform.application.dto.manifest;
import com.ontology.platform.domain.vo.manifest.*;
import lombok.*;
import java.util.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ImportManifestResponse {
    private String draftId;
    private String externalId;
    private Map<String,Integer> importedCounts;
    private List<ValidationError> warnings;
    private boolean valid;
    private List<ValidationError> errors;
}
