package com.ontology.platform.application.dto.upload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "上传任务响应DTO，包含任务状态和文件信息")
public class UploadTaskResponse {

    /**
     * 上传任务ID
     */
    private String uploadId;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件大小（字节）
     */
    private long fileSize;

    /**
     * 分片大小（字节）
     */
    private int chunkSize;

    /**
     * 总分片数
     */
    private int chunkCount;

    /**
     * 状态
     */
    private String status;

    /**
     * 已上传的分片列表
     */
    private Set<Integer> uploadedChunks;

    /**
     * 缺失的分片列表
     */
    private Set<Integer> missingChunks;

    /**
     * 上传进度百分比
     */
    private int progressPercent;

    /**
     * 分片上传URL（初始化时返回）
     */
    private Map<String, String> uploadUrls;

    /**
     * 过期时间
     */
    private Instant expiresAt;

    /**
     * 创建时间
     */
    private Instant createdAt;

    /**
     * 文件MD5（完成上传后返回）
     */
    private String fileMd5;

    /**
     * 文件ID（完成上传后返回）
     */
    private String fileId;

    /**
     * 文件URL（完成上传后返回）
     */
    private String fileUrl;

    /**
     * 预计完成时间
     */
    private Instant estimatedCompletionTime;

    /**
     * 消息
     */
    private String message;
}
