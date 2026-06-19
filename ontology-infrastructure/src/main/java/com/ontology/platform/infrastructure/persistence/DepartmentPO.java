package com.ontology.platform.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import java.time.Instant;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@TableName("department")
public class DepartmentPO {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    @TableField("name")
    private String name;
    @TableField("name_en")
    private String nameEn;
    @TableField("description")
    private String description;
    @TableField("parent_department_id")
    private String parentDepartmentId;
    @TableField("created_at")
    private Instant createdAt;
    @TableField("updated_at")
    private Instant updatedAt;
}
