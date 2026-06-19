package com.ontology.platform.application.dto.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.Instant;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "EpcModelRef响应")
public class EpcModelRefResponse {
    @Schema(description = "ID") private String id;
    @Schema(description = "chain_id") private String chainId;
    @Schema(description = "model_type") private String modelType;
    @Schema(description = "model_id") private String modelId;
    @Schema(description = "ref_metadata") private String refMetadata;
    @Schema(description = "创建时间") private Instant createdAt;
    @Schema(description = "更新时间") private Instant updatedAt;
}
