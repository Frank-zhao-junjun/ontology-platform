package com.ontology.platform.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "图遍历查询响应")
public class GraphQueryResponse {

    @Builder.Default
    @Schema(description = "节点列表")
    private List<Node> nodes = new ArrayList<>();

    @Builder.Default
    @Schema(description = "边列表")
    private List<Edge> edges = new ArrayList<>();

    @Schema(description = "图元数据")
    private GraphMetadata metadata;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "图节点")
    public static class Node {
        @Schema(description = "节点ID")
        private String id;
        @Schema(description = "节点类型")
        private String type;
        @Schema(description = "节点数据")
        private Map<String, Object> data;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "图边")
    public static class Edge {
        @Schema(description = "源节点ID")
        private String source;
        @Schema(description = "目标节点ID")
        private String target;
        @Schema(description = "关系类型")
        private String relation;
        @Schema(description = "关系属性")
        private Map<String, Object> properties;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "图元数据")
    public static class GraphMetadata {
        @Schema(description = "节点总数")
        private int totalNodes;
        @Schema(description = "边总数")
        private int totalEdges;
        @Schema(description = "最大达到深度")
        private int maxDepthReached;
        @Schema(description = "查询耗时(ms)")
        private long queryTimeMs;
    }
}
