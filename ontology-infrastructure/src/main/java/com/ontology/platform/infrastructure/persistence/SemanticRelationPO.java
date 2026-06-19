package com.ontology.platform.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.Instant;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@TableName("semantic_relation")
public class SemanticRelationPO {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    @TableField("source_term_id")
    private String sourceTermId;
    @TableField("target_term_id")
    private String targetTermId;
    @TableField("relation_type")
    private String relationType;
    @TableField("description")
    private String description;
    @TableField("created_at")
    private Instant createdAt;

}
