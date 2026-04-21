package com.ontology.platform.application.dto.upload;

import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * 导入请求
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportRequest {

    /**
     * 上传任务ID
     */
    private String uploadId;

    /**
     * 本体ID
     */
    private String ontologyId;

    /**
     * 对象类型名称
     */
    private String objectTypeName;

    /**
     * 列名到属性名的映射
     */
    private Map<String, String> columnMapping;

    /**
     * 是否跳过表头行
     */
    private Boolean skipHeader;

    /**
     * 编码格式
     */
    private String encoding;

    /**
     * 错误处理策略：SKIP/STOP
     */
    private String errorHandling;

    /**
     * 合并策略：INSERT/UPSERT/REPLACE
     */
    private String mergeStrategy;

    /**
     * 验证规则列表
     */
    private List<ValidationRuleConfig> validationRules;
}
