package com.ontology.platform.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.Instant;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@TableName("epc_model_ref")
public class EpcModelRefPO {
    @TableId(type = IdType.INPUT)
    private String id;
    @TableField("chain_id")
    private String chainId;
    @TableField("model_type")
    private String modelType;
    @TableField("model_id")
    private String modelId;
    @TableField("ref_metadata")
    private String refMetadata;
    @TableField("created_at")
    private Instant createdAt;
    @TableField("updated_at")
    private Instant updatedAt;
}
