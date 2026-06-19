package com.ontology.platform.application.dto.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.Instant;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "EpcChain响应")
public class EpcChainResponse {
    @Schema(description = "ID") private String id;
    @Schema(description = "name") private String name;
    @Schema(description = "aggregate_root_id") private String aggregateRootId;
    @Schema(description = "description") private String description;
    @Schema(description = "chain_type") private String chainType;
    @Schema(description = "is_active") private Boolean isActive;
    @Schema(description = "创建时间") private Instant createdAt;
    @Schema(description = "更新时间") private Instant updatedAt;
}
