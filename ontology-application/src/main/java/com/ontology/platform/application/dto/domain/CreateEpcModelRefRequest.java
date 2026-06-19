package com.ontology.platform.application.dto.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "创建EpcModelRef请求")
public class CreateEpcModelRefRequest {
    @Schema(description = "chain_id") private String chainId;
    @Schema(description = "model_type") private String modelType;
    @Schema(description = "model_id") private String modelId;
    @Schema(description = "ref_metadata") private String refMetadata;
}
