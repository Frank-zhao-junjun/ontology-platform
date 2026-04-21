package com.ontology.platform.domain.vo.traversal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 图遍历过滤器值对象
 * Traversal Filter Value Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TraversalFilter {
    
    /**
     * 应用深度
     */
    private int depth;
    
    /**
     * 目标对象类型（可选）
     */
    private String targetType;
    
    /**
     * 过滤条件列表
     */
    private List<TraversalFilterCondition> conditions;
    
    /**
     * 条件逻辑关系（默认AND）
     */
    @Builder.Default
    private String logic = "AND";
    
    /**
     * 验证过滤器是否有效
     */
    public boolean isValid() {
        return conditions != null && !conditions.isEmpty() && depth >= 0;
    }
    
    /**
     * 判断是否为AND逻辑
     */
    public boolean isAndLogic() {
        return "AND".equalsIgnoreCase(logic);
    }
    
    /**
     * 判断是否为OR逻辑
     */
    public boolean isOrLogic() {
        return "OR".equalsIgnoreCase(logic);
    }
}
