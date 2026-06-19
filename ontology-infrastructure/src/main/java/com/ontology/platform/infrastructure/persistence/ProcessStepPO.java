package com.ontology.platform.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import java.time.Instant;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@TableName("process_step")
public class ProcessStepPO {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    @TableField("orchestration_id")
    private String orchestrationId;
    @TableField("name")
    private String name;
    @TableField("step_type")
    private String stepType;
    @TableField("description")
    private String description;
    @TableField("sort_order")
    private Integer sortOrder;
    @TableField("config")
    private String config;
    @TableField("created_at")
    private Instant createdAt;
    @TableField("updated_at")
    private Instant updatedAt;
}
