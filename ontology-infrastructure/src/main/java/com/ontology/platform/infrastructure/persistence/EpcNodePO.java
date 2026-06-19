package com.ontology.platform.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.Instant;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@TableName("epc_node")
public class EpcNodePO {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    @TableField("chain_id")
    private String chainId;
    @TableField("node_type")
    private String nodeType;
    @TableField("name")
    private String name;
    @TableField("description")
    private String description;
    @TableField("ref_type")
    private String refType;
    @TableField("ref_id")
    private String refId;
    @TableField("metadata")
    private String metadata;
    @TableField("sort_order")
    private Integer sortOrder;
    @TableField("created_at")
    private Instant createdAt;
    @TableField("updated_at")
    private Instant updatedAt;
}
