package com.ontology.platform.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.Instant;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@TableName("epc_edge")
public class EpcEdgePO {
    @TableId(type = IdType.INPUT)
    private String id;
    @TableField("chain_id")
    private String chainId;
    @TableField("source_node_id")
    private String sourceNodeId;
    @TableField("target_node_id")
    private String targetNodeId;
    @TableField("edge_type")
    private String edgeType;
    @TableField("label")
    private String label;
    @TableField("condition_expr")
    private String conditionExpr;
    @TableField("metadata")
    private String metadata;
    @TableField("sort_order")
    private Integer sortOrder;
    @TableField("created_at")
    private Instant createdAt;
    @TableField("updated_at")
    private Instant updatedAt;
}
