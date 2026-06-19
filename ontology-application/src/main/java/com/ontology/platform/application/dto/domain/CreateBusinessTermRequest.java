package com.ontology.platform.application.dto.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "创建BusinessTerm请求")
public class CreateBusinessTermRequest {
    @Schema(description = "name")
    private String name;
    @Schema(description = "name_en")
    private String nameEn;
    @Schema(description = "definition")
    private String definition;
    @Schema(description = "synonyms")
    private String synonyms;
    @Schema(description = "ontology_id")
    private String ontologyId;
}
