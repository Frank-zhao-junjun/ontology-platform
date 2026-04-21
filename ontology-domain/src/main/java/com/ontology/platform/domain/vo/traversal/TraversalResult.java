package com.ontology.platform.domain.vo.traversal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 图遍历结果值对象
 * Graph Traversal Result Value Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TraversalResult {
    
    /**
     * 是否成功
     */
    private boolean success;
    
    /**
     * 结果数量
     */
    private int totalCount;
    
    /**
     * 路径列表
     */
    private List<PathInfo> paths;
    
    /**
     * 节点列表
     */
    private List<NodeInfo> nodes;
    
    /**
     * 边列表
     */
    private List<EdgeInfo> edges;
    
    /**
     * 执行时间（毫秒）
     */
    private long executionTimeMs;
    
    /**
     * 错误信息（如果有）
     */
    private String errorMessage;
    
    /**
     * 创建成功结果
     */
    public static TraversalResult success(List<PathInfo> paths, 
                                          List<NodeInfo> nodes, 
                                          List<EdgeInfo> edges,
                                          long executionTimeMs) {
        return TraversalResult.builder()
                .success(true)
                .totalCount(paths.size())
                .paths(paths)
                .nodes(nodes)
                .edges(edges)
                .executionTimeMs(executionTimeMs)
                .build();
    }
    
    /**
     * 创建失败结果
     */
    public static TraversalResult failure(String errorMessage) {
        return TraversalResult.builder()
                .success(false)
                .totalCount(0)
                .paths(List.of())
                .nodes(List.of())
                .edges(List.of())
                .executionTimeMs(0)
                .errorMessage(errorMessage)
                .build();
    }
    
    /**
     * 路径信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PathInfo {
        private String pathId;
        private List<String> nodeIds;
        private List<String> edgeIds;
        private int depth;
    }
    
    /**
     * 节点信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NodeInfo {
        private String id;
        private String objectType;
        private String objectId;
        private Map<String, Object> properties;
        private int depth;
    }
    
    /**
     * 边信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EdgeInfo {
        private String id;
        private String relationType;
        private String sourceId;
        private String targetId;
        private Map<String, Object> properties;
    }
}
