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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("upload_task")
public class UploadTaskPO {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("original_file_name")
    private String originalFileName;

    @TableField("file_size")
    private Long fileSize;

    @TableField("file_type")
    private String fileType;

    @TableField("chunk_size")
    private Integer chunkSize;

    @TableField("total_chunks")
    private Integer totalChunks;

    @TableField("target_type")
    private String targetType;

    @TableField("ontology_id")
    private String ontologyId;

    @TableField("object_type_name")
    private String objectTypeName;

    @TableField("user_id")
    private String userId;

    @TableField("tenant_id")
    private String tenantId;

    @TableField("status")
    private String status;

    @TableField("uploaded_chunks")
    private String uploadedChunks;

    @TableField("stored_file_path")
    private String storedFilePath;

    @TableField("file_md5")
    private String fileMd5;

    @TableField("expires_at")
    private Instant expiresAt;

    @TableField("created_at")
    private Instant createdAt;

    @TableField("updated_at")
    private Instant updatedAt;

    public Set<Integer> getUploadedChunksSet() {
        if (uploadedChunks == null || uploadedChunks.isEmpty()) {
            return new HashSet<>();
        }
        try {
            return MAPPER.readValue(uploadedChunks, new TypeReference<Set<Integer>>() {});
        } catch (JsonProcessingException e) {
            return new HashSet<>();
        }
    }

    public void setUploadedChunksSet(Set<Integer> chunks) {
        try {
            this.uploadedChunks = MAPPER.writeValueAsString(chunks != null ? chunks : Collections.emptySet());
        } catch (JsonProcessingException e) {
            this.uploadedChunks = "[]";
        }
    }
}
