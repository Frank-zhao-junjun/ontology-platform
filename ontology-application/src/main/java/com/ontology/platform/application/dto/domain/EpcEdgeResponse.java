package com.ontology.platform.application.dto.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.Instant;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "EpcEdge响应")
public class EpcEdgeResponse {
    @Schema(description = "ID") private String id;
    @Schema(description = "chain_id") private String chainId;
    @Schema(description = "source_node_id") private String sourceNodeId;
    @Schema(description = "target_node_id") private String targetNodeId;
    @Schema(description = "edge_type") private String edgeType;
    @Schema(description = "label") private String label;
    @Schema(description = "condition_expr") private String conditionExpr;
    @Schema(description = "metadata") private String metadata;
    @Schema(description = "sort_order") private Integer sortOrder;
    @Schema(description = "创建时间") private Instant createdAt;
    @Schema(description = "更新时间") private Instant updatedAt;
}
