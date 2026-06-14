package com.ontology.platform.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("import_task")
public class ImportTaskPO {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("upload_id")
    private String uploadId;

    @TableField("ontology_id")
    private String ontologyId;

    @TableField("object_type_name")
    private String objectTypeName;

    @TableField("object_type_id")
    private String objectTypeId;

    @TableField("merge_strategy")
    private String mergeStrategy;

    @TableField("error_handling")
    private String errorHandling;

    @TableField("user_id")
    private String userId;

    @TableField("tenant_id")
    private String tenantId;

    @TableField("status")
    private String status;

    @TableField("total_rows")
    private Long totalRows;

    @TableField("processed_rows")
    private Long processedRows;

    @TableField("success_rows")
    private Long successRows;

    @TableField("failed_rows")
    private Long failedRows;

    @TableField("errors")
    private String errors;

    @TableField("created_at")
    private Instant createdAt;

    @TableField("completed_at")
    private Instant completedAt;

    @TableField("estimated_completion")
    private Instant estimatedCompletion;

    public List<Map<String, Object>> getErrorsList() {
        if (errors == null || errors.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return MAPPER.readValue(errors, new TypeReference<List<Map<String, Object>>>() {});
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }

    public void setErrorsList(List<Map<String, Object>> errorList) {
        try {
            this.errors = MAPPER.writeValueAsString(errorList != null ? errorList : Collections.emptyList());
        } catch (JsonProcessingException e) {
            this.errors = "[]";
        }
    }
}
