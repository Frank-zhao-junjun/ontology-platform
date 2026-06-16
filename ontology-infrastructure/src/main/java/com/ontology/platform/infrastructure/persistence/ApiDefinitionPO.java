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
@TableName("api_definition")
public class ApiDefinitionPO {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("ontology_id")
    private String ontologyId;
    @TableField("api_name")
    private String apiName;
    @TableField("description")
    private String description;
    @TableField("url")
    private String url;
    @TableField("http_method")
    private String httpMethod;
    @TableField("request_schema")
    private String requestSchema;
    @TableField("response_schema")
    private String responseSchema;
    @TableField("auth_type")
    private String authType;
    @TableField("rate_limit")
    private Integer rateLimit;
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
