package com.ontology.platform.api.controller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OntologyImportRequest {
    private String rawContent;
    private String createdBy;
    /** 导入后自动发布到 V12-V14 领域表（默认 false） */
    @Builder.Default
    private Boolean autoPublish = false;
    /** 校验模式: strict | warn（默认 strict） */
    @Builder.Default
    private String validationMode = "strict";
}
