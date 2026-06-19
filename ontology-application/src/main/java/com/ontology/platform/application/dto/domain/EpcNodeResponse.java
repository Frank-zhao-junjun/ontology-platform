package com.ontology.platform.application.dto.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.Instant;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "EpcNode响应")
public class EpcNodeResponse {
    @Schema(description = "ID") private String id;
    @Schema(description = "chain_id") private String chainId;
    @Schema(description = "node_type") private String nodeType;
    @Schema(description = "name") private String name;
    @Schema(description = "description") private String description;
    @Schema(description = "ref_type") private String refType;
    @Schema(description = "ref_id") private String refId;
    @Schema(description = "metadata") private String metadata;
    @Schema(description = "sort_order") private Integer sortOrder;
    @Schema(description = "创建时间") private Instant createdAt;
    @Schema(description = "更新时间") private Instant updatedAt;
}
