package com.ontology.platform.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("query_definition")
public class QueryDefinitionPO {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("ontology_id")
    private String ontologyId;
    @TableField("query_name")
    private String queryName;
    @TableField("description")
    private String description;
    @TableField("query_type")
    private String queryType;
    @TableField("query_template")
    private String queryTemplate;
    @TableField("parameters")
    private String parameters;
    @TableField("result_schema")
    private String resultSchema;
    @TableField("timeout_ms")
    private Integer timeoutMs;
    @TableField("enabled")
    private Boolean enabled;

    @TableField("created_at")
    private Instant createdAt;

    @TableField("updated_at")
    private Instant updatedAt;

    @TableField("deleted")
    private Boolean deleted;
}
