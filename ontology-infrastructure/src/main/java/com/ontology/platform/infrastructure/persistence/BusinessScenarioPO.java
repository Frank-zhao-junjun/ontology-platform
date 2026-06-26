package com.ontology.platform.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.time.Instant;

/**
 * 业务场景持久化对象
 * BusinessScenario Persistence Object
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("business_scenario")
public class BusinessScenarioPO {

    @TableId(type = IdType.INPUT)
    private String id;

    @TableField("ontology_id")
    private String ontologyId;

    @TableField("name")
    private String name;

    @TableField("name_en")
    private String nameEn;

    @TableField("description")
    private String description;

    @TableField("project_id")
    private String projectId;

    @TableField("created_at")
    private Instant createdAt;

    @TableField("updated_at")
    private Instant updatedAt;
}
