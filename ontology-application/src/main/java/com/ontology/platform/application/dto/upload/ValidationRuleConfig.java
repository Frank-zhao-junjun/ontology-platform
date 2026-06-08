package com.ontology.platform.application.dto.upload;

import lombok.*;

/**
 * 验证规则配置
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationRuleConfig {

    /**
     * 属性名
     */
    private String propertyName;

    /**
     * 规则类型：required, pattern, range, enum
     */
    private String ruleType;

    /**
     * 规则值
     */
    private String ruleValue;

    /**
     * 错误消息
     */
    private String errorMessage;
}
