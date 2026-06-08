package com.ontology.platform.domain.vo.traversal;

import com.ontology.platform.common.enums.ReturnFormat;
import com.ontology.platform.common.enums.TraversalDirection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 图遍历请求值对象
 * Graph Traversal Request Value Object
 * 
 * 采用结构化DSL设计，避免字符串拼接，从根本上杜绝注入风险
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraphTraversalRequest {
    
    // ==================== 起点信息（必填） ====================
    
    /**
     * 对象类型名称（白名单验证）
     */
    private String startObjectType;
    
    /**
     * 起点对象ID（UUID格式校验）
     */
    private String startObjectId;
    
    // ==================== 关系路径配置 ====================
    
    /**
     * 遍历路径，结构化定义
     */
    private List<TraversalPath> path;
    
    // ==================== 查询约束 ====================
    
    /**
     * 最大深度，默认3，最大5（硬限制）
     */
    @Builder.Default
    private int maxDepth = 3;
    
    /**
     * 遍历方向
     */
    @Builder.Default
    private TraversalDirection direction = TraversalDirection.OUTGOING;
    
    /**
     * 结果数量限制，默认100，最大1000
     */
    @Builder.Default
    private int limit = 100;
    
    // ==================== 过滤条件（结构化） ====================
    
    /**
     * 结构化过滤条件（非自由文本）
     */
    private List<TraversalFilter> filters;
    
    // ==================== 返回格式 ====================
    
    /**
     * 返回格式：GRAPH/TREE/FLAT
     */
    @Builder.Default
    private ReturnFormat returnFormat = ReturnFormat.GRAPH;
    
    // ==================== 属性选择 ====================
    
    /**
     * 仅返回指定属性
     */
    private List<String> includeProperties;
    
    /**
     * 排除指定属性
     */
    private List<String> excludeProperties;
    
    // ==================== 验证方法 ====================
    
    /**
     * 验证必填字段
     */
    public boolean isValid() {
        return startObjectType != null && !startObjectType.isBlank()
            && startObjectId != null && !startObjectId.isBlank()
            && maxDepth >= 0 && limit > 0;
    }
    
    /**
     * 获取有效深度（不超过最大值）
     */
    public int getEffectiveDepth() {
        return Math.min(maxDepth, 5);
    }
    
    /**
     * 获取有效限制（不超过最大值）
     */
    public int getEffectiveLimit() {
        return Math.min(limit, 1000);
    }
}
