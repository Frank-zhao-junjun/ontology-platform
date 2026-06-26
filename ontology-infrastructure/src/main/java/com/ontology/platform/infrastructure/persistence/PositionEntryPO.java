package com.ontology.platform.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import java.time.Instant;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@TableName("position_entry")
public class PositionEntryPO {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    @TableField("ontology_id")
    private String ontologyId;
    @TableField("name")
    private String name;
    @TableField("name_en")
    private String nameEn;
    @TableField("description")
    private String description;
    @TableField("department_id")
    private String departmentId;
    @TableField("role_id")
    private String roleId;
    @TableField("responsibilities")
    private String responsibilities;
    @TableField("created_at")
    private Instant createdAt;
    @TableField("updated_at")
    private Instant updatedAt;
}
