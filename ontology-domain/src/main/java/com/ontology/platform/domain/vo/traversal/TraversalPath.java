package com.ontology.platform.domain.vo.traversal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 图遍历路径段值对象
 * Traversal Path Segment Value Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TraversalPath {
    
    /**
     * 关系类型名称（必须通过白名单验证）
     */
    private String relationType;
    
    /**
     * 目标对象类型（可选，用于校验）
     */
    private String targetObjectType;
    
    /**
     * 该路径段重复次数（默认1）
     */
    @Builder.Default
    private int depth = 1;
    
    /**
     * 验证路径段是否有效
     */
    public boolean isValid() {
        return relationType != null && !relationType.isBlank() && depth > 0;
    }
}
