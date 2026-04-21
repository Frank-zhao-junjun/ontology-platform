package com.ontology.platform.application.dto;

import lombok.*;

import java.util.List;

/**
 * 批量导入实例请求DTO
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchImportRequest {

    /**
     * 对象类型ID
     */
    private String objectTypeId;

    /**
     * 实例数据列表
     */
    private List<InstanceData> instances;

    /**
     * 是否跳过已存在的记录
     */
    @Builder.Default
    private boolean skipExisting = false;

    /**
     * 实例数据
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InstanceData {
        /**
         * 主键值
         */
        private String primaryKeyValue;

        /**
         * 属性
         */
        private java.util.Map<String, Object> properties;
    }
}
