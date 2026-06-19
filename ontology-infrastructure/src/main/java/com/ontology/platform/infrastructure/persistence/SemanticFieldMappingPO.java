package com.ontology.platform.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.Instant;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@TableName("semantic_field_mapping")
public class SemanticFieldMappingPO {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    @TableField("entity_id")
    private String entityId;
    @TableField("field_name_en")
    private String fieldNameEn;
    @TableField("business_term_id")
    private String businessTermId;
    @TableField("mapping_type")
    private String mappingType;
    @TableField("transform_rule")
    private String transformRule;
    @TableField("created_at")
    private Instant createdAt;

}
