package com.ontology.platform.api.dto.graph;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 图遍历响应DTO
 * Graph Traversal Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraphTraversalResponseDTO {
    
    private boolean success;
    private int totalCount;
    private List<PathDTO> paths;
    private List<NodeDTO> nodes;
    private List<EdgeDTO> edges;
    private long executionTimeMs;
    private String errorMessage;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PathDTO {
        private String pathId;
        private List<String> nodeIds;
        private List<String> edgeIds;
        private int depth;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NodeDTO {
        private String id;
        private String objectType;
        private String objectId;
        private Map<String, Object> properties;
        private int depth;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EdgeDTO {
        private String id;
        private String relationType;
        private String sourceId;
        private String targetId;
        private Map<String, Object> properties;
    }
}
