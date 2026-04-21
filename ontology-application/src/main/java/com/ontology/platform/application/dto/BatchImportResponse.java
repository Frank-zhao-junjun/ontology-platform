package com.ontology.platform.application.dto;

import lombok.*;

import java.util.List;

/**
 * 批量导入实例响应DTO
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchImportResponse {

    /**
     * 总数
     */
    private int total;

    /**
     * 成功数
     */
    private int successCount;

    /**
     * 失败数
     */
    private int failureCount;

    /**
     * 跳过数
     */
    private int skippedCount;

    /**
     * 失败的记录
     */
    private List<FailedRecord> failedRecords;

    /**
     * 导入的实例列表
     */
    private List<InstanceResponse> importedInstances;

    /**
     * 失败的记录
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FailedRecord {
        /**
         * 主键值
         */
        private String primaryKeyValue;

        /**
         * 错误原因
         */
        private String errorMessage;
    }
}
