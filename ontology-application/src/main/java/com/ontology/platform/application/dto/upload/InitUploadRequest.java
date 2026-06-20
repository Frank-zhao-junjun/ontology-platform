package com.ontology.platform.application.dto.upload;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "初始化上传请求DTO")
public class InitUploadRequest {

    /**
     * 文件名（含扩展名）
     */
    @NotBlank(message = "File name is required")
    private String fileName;

    /**
     * 文件大小（字节）
     */
    @NotNull(message = "File size is required")
    @Positive(message = "File size must be positive")
    private Long fileSize;

    /**
     * 文件类型：csv/xlsx/json
     */
    @NotBlank(message = "File type is required")
    private String fileType;

    /**
     * 分片大小（字节），默认5MB
     */
    private Integer chunkSize;

    /**
     * 目标类型：object_import/ontology_import/datasource_file
     */
    @NotBlank(message = "Target type is required")
    private String targetType;

    /**
     * 本体ID（target_type为object_import时必填）
     */
    private String ontologyId;

    /**
     * 对象类型名称（target_type为object_import时必填）
     */
    private String objectTypeName;

    /**
     * 导入选项
     */
    private Map<String, Object> options;
}
