package com.ontology.platform.application.service.upload;

import com.ontology.platform.application.dto.upload.ImportRequest;
import com.ontology.platform.application.dto.upload.ImportTaskResponse;

import java.util.List;
import java.util.Map;

/**
 * 数据导入服务接口
 */
public interface ImportService {

    /**
     * 执行数据导入
     * @param request 导入请求
     * @param userId 用户ID
     * @param tenantId 租户ID
     * @return 导入任务响应
     */
    ImportTaskResponse executeImport(ImportRequest request, String userId, String tenantId);

    /**
     * 异步执行导入
     * @param request 导入请求
     * @param userId 用户ID
     * @param tenantId 租户ID
     * @return 导入任务ID
     */
    String executeImportAsync(ImportRequest request, String userId, String tenantId);

    /**
     * 查询导入任务状态
     * @param importId 导入任务ID
     * @return 导入任务响应
     */
    ImportTaskResponse getImportStatus(String importId);

    /**
     * 取消导入任务
     * @param importId 导入任务ID
     */
    void cancelImport(String importId);

    /**
     * 解析数据文件
     * @param filePath 文件路径
     * @param fileType 文件类型
     * @param config 解析配置
     * @return 解析后的数据
     */
    Map<String, Object> parseDataFile(String filePath, String fileType, Map<String, Object> config);

    /**
     * 批量验证数据
     * @param dataList 数据列表
     * @param objectTypeId 对象类型ID
     * @return 验证结果
     */
    ValidationResult batchValidate(List<Map<String, String>> dataList, String objectTypeId);

    /**
     * 验证结果
     */
    record ValidationResult(
            boolean valid,
            int totalRows,
            int successRows,
            int failedRows,
            List<ValidationError> errors
    ) {
        public record ValidationError(
                int row,
                String field,
                String message
        ) {}
    }
}
