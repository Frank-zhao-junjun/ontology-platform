package com.ontology.platform.application.dto.manifest;
import com.ontology.platform.domain.vo.manifest.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "Manifest导入响应DTO，包含草稿ID、导入计数和验证结果")
public class ImportManifestResponse {
    private String draftId;
    private String externalId;
    private Map<String,Integer> importedCounts;
    private List<ValidationError> warnings;
    private boolean valid;
    private List<ValidationError> errors;
}
