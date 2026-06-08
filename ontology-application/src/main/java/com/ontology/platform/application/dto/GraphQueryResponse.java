package com.ontology.platform.application.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 图遍历查询响应DTO
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraphQueryResponse {

    @Builder.Default
    private List<Node> nodes = new ArrayList<>();

    @Builder.Default
    private List<Edge> edges = new ArrayList<>();

    private GraphMetadata metadata;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Node {
        private String id;
        private String type;
        private Map<String, Object> data;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Edge {
        private String source;
        private String target;
        private String relation;
        private Map<String, Object> properties;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GraphMetadata {
        private int totalNodes;
        private int totalEdges;
        private int maxDepthReached;
        private long queryTimeMs;
    }
}
