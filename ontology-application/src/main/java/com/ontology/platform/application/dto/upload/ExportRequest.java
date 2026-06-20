package com.ontology.platform.application.dto.upload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "导出请求DTO")
public class ExportRequest {

    /**
     * 本体ID
     */
    private String ontologyId;

    /**
     * 对象类型名称
     */
    private String objectTypeName;

    /**
     * 导出格式：csv/xlsx
     */
    private String format;

    /**
     * 编码格式（CSV时使用）
     */
    private String encoding;

    /**
     * 筛选条件
     */
    private String filter;

    /**
     * 排序字段
     */
    private String sortBy;

    /**
     * 排序方向：ASC/DESC
     */
    private String sortOrder;

    /**
     * 最大导出行数
     */
    private Integer limit;
}
