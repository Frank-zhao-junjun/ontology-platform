package com.ontology.platform.application.dto.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "创建EpcEdge请求")
public class CreateEpcEdgeRequest {
    @Schema(description = "chain_id") private String chainId;
    @Schema(description = "source_node_id") private String sourceNodeId;
    @Schema(description = "target_node_id") private String targetNodeId;
    @Schema(description = "edge_type") private String edgeType;
    @Schema(description = "label") private String label;
    @Schema(description = "condition_expr") private String conditionExpr;
    @Schema(description = "metadata") private String metadata;
    @Schema(description = "sort_order") private Integer sortOrder;
}
