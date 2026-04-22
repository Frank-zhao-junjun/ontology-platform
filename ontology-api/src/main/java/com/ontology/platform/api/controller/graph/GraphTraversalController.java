package com.ontology.platform.api.controller.graph;

import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.api.dto.graph.*;
import com.ontology.platform.application.service.graph.GraphQueryService;
import com.ontology.platform.domain.vo.traversal.*;
import com.ontology.platform.common.enums.FilterOperator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 图遍历控制器
 * Graph Traversal Controller
 * 
 * 提供图遍历查询API：
 * - POST /v1/ontologies/{ontologyId}/graph/traverse - 执行图遍历
 * - GET /v1/ontologies/{ontologyId}/graph/paths - 查询最短路径
 * - GET /v1/ontologies/{ontologyId}/graph/subgraph - 提取子图
 */
@Slf4j
@RestController
@RequestMapping("/v1/ontologies/{ontologyId}/graph")
@RequiredArgsConstructor
@Tag(name = "Graph Traversal", description = "图遍历查询API")
public class GraphTraversalController {
    
    private final GraphQueryService graphQueryService;
    
    /**
     * 执行图遍历查询
        * POST /v1/ontologies/{ontologyId}/graph/traverse
     */
    @PostMapping("/traverse")
    @Operation(summary = "执行图遍历查询", 
               description = "从指定起点开始，按照路径和过滤条件遍历图")
    public ResponseEntity<ApiResponse<GraphTraversalResponseDTO>> traverse(
            @Parameter(description = "本体ID") @PathVariable String ontologyId,
            @Valid @RequestBody GraphTraversalRequestDTO requestDTO) {
        
        log.info("REST: Graph traverse, ontologyId={}, startObjectId={}", 
            ontologyId, requestDTO.getStartObjectId());
        
        GraphTraversalRequest request = convertToDomain(requestDTO);
        TraversalResult result = graphQueryService.traverse(ontologyId, request);
        GraphTraversalResponseDTO responseDTO = convertToResponseDTO(result);
        
        return ResponseEntity.ok(ApiResponse.success(responseDTO));
    }
    
    /**
     * 查询最短路径
        * GET /v1/ontologies/{ontologyId}/graph/paths
     */
    @GetMapping("/paths")
    @Operation(summary = "查询最短路径", 
               description = "查询两个节点之间的最短路径")
    public ResponseEntity<ApiResponse<GraphTraversalResponseDTO>> findShortestPath(
            @Parameter(description = "本体ID") @PathVariable String ontologyId,
            @Parameter(description = "起始节点ID") @RequestParam String from,
            @Parameter(description = "目标节点ID") @RequestParam String to,
            @Parameter(description = "最大深度") @RequestParam(defaultValue = "5") int maxDepth) {
        
        log.info("REST: Find shortest path, ontologyId={}, from={}, to={}, maxDepth={}", 
            ontologyId, from, to, maxDepth);
        
        TraversalResult result = graphQueryService.findShortestPath(ontologyId, from, to, maxDepth);
        return ResponseEntity.ok(ApiResponse.success(convertToResponseDTO(result)));
    }
    
    /**
     * 提取子图
        * GET /v1/ontologies/{ontologyId}/graph/subgraph
     */
    @GetMapping("/subgraph")
    @Operation(summary = "提取子图", 
               description = "从指定根节点提取指定深度的子图")
    public ResponseEntity<ApiResponse<GraphTraversalResponseDTO>> extractSubgraph(
            @Parameter(description = "本体ID") @PathVariable String ontologyId,
            @Parameter(description = "根节点ID") @RequestParam String root,
            @Parameter(description = "深度") @RequestParam(defaultValue = "3") int depth) {
        
        log.info("REST: Extract subgraph, ontologyId={}, root={}, depth={}", 
            ontologyId, root, depth);
        
        TraversalResult result = graphQueryService.extractSubgraph(ontologyId, root, depth);
        return ResponseEntity.ok(ApiResponse.success(convertToResponseDTO(result)));
    }
    
    // ==================== DTO转换方法 ====================
    
    private GraphTraversalRequest convertToDomain(GraphTraversalRequestDTO dto) {
        return GraphTraversalRequest.builder()
                .startObjectType(dto.getStartObjectType())
                .startObjectId(dto.getStartObjectId())
                .path(convertPaths(dto.getPath()))
                .maxDepth(dto.getMaxDepth())
                .direction(dto.getDirection())
                .limit(dto.getLimit())
                .filters(convertFilters(dto.getFilters()))
                .returnFormat(dto.getReturnFormat())
                .includeProperties(dto.getIncludeProperties())
                .excludeProperties(dto.getExcludeProperties())
                .build();
    }
    
    private List<TraversalPath> convertPaths(List<GraphTraversalRequestDTO.TraversalPathDTO> paths) {
        if (paths == null || paths.isEmpty()) {
            return List.of();
        }
        return paths.stream()
                .map(p -> TraversalPath.builder()
                        .relationType(p.getRelationType())
                        .targetObjectType(p.getTargetObjectType())
                        .depth(p.getDepth())
                        .build())
                .collect(Collectors.toList());
    }
    
    private List<TraversalFilter> convertFilters(List<GraphTraversalRequestDTO.TraversalFilterDTO> filters) {
        if (filters == null || filters.isEmpty()) {
            return List.of();
        }
        return filters.stream()
                .map(f -> TraversalFilter.builder()
                        .depth(f.getDepth())
                        .targetType(f.getTargetType())
                        .conditions(convertConditions(f.getConditions()))
                        .logic(f.getLogic())
                        .build())
                .collect(Collectors.toList());
    }
    
    private List<TraversalFilterCondition> convertConditions(
            List<GraphTraversalRequestDTO.FilterConditionDTO> conditions) {
        if (conditions == null || conditions.isEmpty()) {
            return List.of();
        }
        return conditions.stream()
                .map(c -> TraversalFilterCondition.builder()
                        .field(c.getField())
                        .operator(parseOperator(c.getOperator()))
                        .value(c.getValue())
                        .build())
                .collect(Collectors.toList());
    }
    
    private FilterOperator parseOperator(String operator) {
        if (operator == null) {
            return FilterOperator.eq;
        }
        try {
            return FilterOperator.valueOf(operator.toLowerCase());
        } catch (IllegalArgumentException e) {
            return FilterOperator.eq;
        }
    }
    
    private GraphTraversalResponseDTO convertToResponseDTO(TraversalResult result) {
        return GraphTraversalResponseDTO.builder()
                .success(result.isSuccess())
                .totalCount(result.getTotalCount())
                .paths(convertPaths(result.getPaths()))
                .nodes(convertNodes(result.getNodes()))
                .edges(convertEdges(result.getEdges()))
                .executionTimeMs(result.getExecutionTimeMs())
                .errorMessage(result.getErrorMessage())
                .build();
    }
    
    private List<GraphTraversalResponseDTO.PathDTO> convertPaths(List<TraversalResult.PathInfo> paths) {
        if (paths == null) return List.of();
        return paths.stream()
                .map(p -> GraphTraversalResponseDTO.PathDTO.builder()
                        .pathId(p.getPathId())
                        .nodeIds(p.getNodeIds())
                        .edgeIds(p.getEdgeIds())
                        .depth(p.getDepth())
                        .build())
                .collect(Collectors.toList());
    }
    
    private List<GraphTraversalResponseDTO.NodeDTO> convertNodes(List<TraversalResult.NodeInfo> nodes) {
        if (nodes == null) return List.of();
        return nodes.stream()
                .map(n -> GraphTraversalResponseDTO.NodeDTO.builder()
                        .id(n.getId())
                        .objectType(n.getObjectType())
                        .objectId(n.getObjectId())
                        .properties(n.getProperties())
                        .depth(n.getDepth())
                        .build())
                .collect(Collectors.toList());
    }
    
    private List<GraphTraversalResponseDTO.EdgeDTO> convertEdges(List<TraversalResult.EdgeInfo> edges) {
        if (edges == null) return List.of();
        return edges.stream()
                .map(e -> GraphTraversalResponseDTO.EdgeDTO.builder()
                        .id(e.getId())
                        .relationType(e.getRelationType())
                        .sourceId(e.getSourceId())
                        .targetId(e.getTargetId())
                        .properties(e.getProperties())
                        .build())
                .collect(Collectors.toList());
    }
}
