package com.ontology.platform.application.service.upload;

import com.ontology.platform.application.dto.upload.ExportRequest;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

/**
 * 数据导出服务接口
 */
public interface ExportService {

    /**
     * 执行数据导出
     * @param request 导出请求
     * @param userId 用户ID
     * @param tenantId 租户ID
     * @return 导出结果
     */
    ExportResult executeExport(ExportRequest request, String userId, String tenantId);

    /**
     * 异步执行导出
     * @param request 导出请求
     * @param userId 用户ID
     * @param tenantId 租户ID
     * @return 导出任务ID
     */
    String executeExportAsync(ExportRequest request, String userId, String tenantId);

    /**
     * 流式导出（用于大文件）
     * @param request 导出请求
     * @param userId 用户ID
     * @param tenantId 租户ID
     * @return StreamingResponseBody
     */
    StreamingResponseBody streamExport(ExportRequest request, String userId, String tenantId);

    /**
     * 获取导出文件
     * @param exportId 导出任务ID
     * @return 导出文件字节数组
     */
    byte[] getExportFile(String exportId);

    /**
     * 导出结果
     */
    record ExportResult(
            String exportId,
            String fileName,
            String format,
            byte[] data,
            int totalRows,
            int exportedRows
    ) {}
}
