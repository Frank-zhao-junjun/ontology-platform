package com.ontology.platform.application.dto.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "创建EpcChain请求")
public class CreateEpcChainRequest {
    @Schema(description = "name") private String name;
    @Schema(description = "aggregate_root_id") private String aggregateRootId;
    @Schema(description = "description") private String description;
    @Schema(description = "chain_type") private String chainType;
    @Schema(description = "is_active") private Boolean isActive;
}
