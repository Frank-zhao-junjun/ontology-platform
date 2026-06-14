package com.ontology.platform.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("role_permission")
public class RolePermissionPO {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("role_id")
    private String roleId;

    @TableField("resource")
    private String resource;

    @TableField("operations")
    private String operations;

    @TableField("domain")
    private String domain;

    @TableField("created_at")
    private Instant createdAt;

    public List<String> getOperationsList() {
        if (operations == null || operations.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return MAPPER.readValue(operations, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }

    public void setOperationsList(List<String> ops) {
        try {
            this.operations = MAPPER.writeValueAsString(ops != null ? ops : Collections.emptyList());
        } catch (JsonProcessingException e) {
            this.operations = "[]";
        }
    }
}
