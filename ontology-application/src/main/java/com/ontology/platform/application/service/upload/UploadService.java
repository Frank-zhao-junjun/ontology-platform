package com.ontology.platform.application.service.upload;

import com.ontology.platform.application.dto.upload.InitUploadRequest;
import com.ontology.platform.application.dto.upload.UploadTaskResponse;

/**
 * 文件上传服务接口
 */
public interface UploadService {

    /**
     * 初始化上传任务
     * @param request 上传初始化请求
     * @param userId 用户ID
     * @param tenantId 租户ID
     * @return 上传任务响应
     */
    UploadTaskResponse initUpload(InitUploadRequest request, String userId, String tenantId);

    /**
     * 上传分片
     * @param uploadId 上传任务ID
     * @param chunkNumber 分片编号（从1开始）
     * @param chunkData 分片数据
     * @param contentMd5 分片MD5（可选）
     * @return 上传任务响应
     */
    UploadTaskResponse uploadChunk(String uploadId, int chunkNumber, byte[] chunkData, String contentMd5);

    /**
     * 查询上传状态
     * @param uploadId 上传任务ID
     * @return 上传任务响应
     */
    UploadTaskResponse getUploadStatus(String uploadId);

    /**
     * 完成上传
     * @param uploadId 上传任务ID
     * @param finalMd5 整个文件的MD5（可选）
     * @return 上传任务响应
     */
    UploadTaskResponse completeUpload(String uploadId, String finalMd5);

    /**
     * 取消上传
     * @param uploadId 上传任务ID
     * @return 删除的分片数量
     */
    int cancelUpload(String uploadId);

    /**
     * 获取导入模板
     * @param ontologyId 本体ID
     * @param objectTypeName 对象类型名称
     * @param format 模板格式：csv/xlsx
     * @return 模板字节数组
     */
    byte[] getImportTemplate(String ontologyId, String objectTypeName, String format);
}
