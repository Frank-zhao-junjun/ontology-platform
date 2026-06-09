package com.ontology.platform.infrastructure.graph.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.common.enums.ErrorCode;
import com.ontology.platform.common.exception.BusinessException;
import com.ontology.platform.domain.vo.traversal.CypherQuery;
import com.ontology.platform.domain.vo.traversal.TraversalResult;
import com.ontology.platform.infrastructure.graph.AgeQueryExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

/**
 * Apache AGE查询执行器实现
 * Apache AGE Query Executor Implementation
 * 
 * 特性：
 * 1. Cypher查询执行
 * 2. 结果映射
 * 3. 性能优化（索引提示）
 * 4. 查询超时保护
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AgeQueryExecutorImpl implements AgeQueryExecutor {
    
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${ontology.graph.query-timeout:30}")
    private int queryTimeoutSeconds;
    
    @Value("${ontology.graph.max-result-size:10000}")
    private int maxResultSize;
    
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    
    @Override
    public TraversalResult executeTraversal(String ontologyId, CypherQuery cypherQuery) {
        log.debug("Executing AGE traversal: ontology={}, cypher={}", ontologyId, cypherQuery.cypher());
        
        Instant startTime = Instant.now();
        
        try {
            String graphName = "ontology_" + ontologyId.replace("-", "_");
            
            Future<TraversalResult> future = executorService.submit(() -> 
                executeWithTimeout(graphName, cypherQuery)
            );
            
            try {
                return future.get(queryTimeoutSeconds, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                future.cancel(true);
                log.warn("Query timeout: ontology={}, timeout={}s", ontologyId, queryTimeoutSeconds);
                throw new BusinessException(ErrorCode.QUERY_TIMEOUT, 
                    "Query execution timeout after " + queryTimeoutSeconds + " seconds");
            }
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("AGE query execution error: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.GRAPH_TRAVERSAL_ERROR, 
                "Graph traversal failed: " + e.getMessage());
        }
    }
    
    private TraversalResult executeWithTimeout(String graphName, CypherQuery cypherQuery) {
        try {
            setCurrentGraph(graphName);
            
            List<Map<String, Object>> rawResults = jdbcTemplate.queryForList(
                cypherQuery.cypher(), 
                cypherQuery.params().values().toArray()
            );
            
            if (rawResults.size() > maxResultSize) {
                log.warn("Result size {} exceeds max {}, truncating", rawResults.size(), maxResultSize);
                rawResults = rawResults.subList(0, maxResultSize);
            }
            
            return mapToTraversalResult(rawResults);
            
        } finally {
            clearCurrentGraph(graphName);
        }
    }
    
    @Override
    public Map<String, Object> executeQuery(String ontologyId, String cypher, Map<String, Object> params) {
        String graphName = "ontology_" + ontologyId.replace("-", "_");
        
        try {
            setCurrentGraph(graphName);
            
            List<Map<String, Object>> results = jdbcTemplate.queryForList(
                cypher, 
                params.values().toArray()
            );
            
            if (results.isEmpty()) {
                return Map.of();
            }
            
            return results.get(0);
        } finally {
            clearCurrentGraph(graphName);
        }
    }
    
    @Override
    public TraversalResult findShortestPath(String ontologyId, String fromObjectId, 
            String toObjectId, int maxDepth) {
        
        String graphName = "ontology_" + ontologyId.replace("-", "_");
        
        String cypher = """
            MATCH (start:Object {id: $fromId}),
                  (end:Object {id: $toId})
            CALL ag_catalog.ag_shortest_path(graph_name, start, end, $maxDepth)
            YIELD path
            RETURN path, nodes(path) as nodes, relationships(path) as edges
            ORDER BY length(path)
            LIMIT 1
            """;
        
        Map<String, Object> params = Map.of(
            "fromId", fromObjectId,
            "toId", toObjectId,
            "maxDepth", maxDepth
        );
        
        try {
            setCurrentGraph(graphName);
            
            List<Map<String, Object>> results = jdbcTemplate.queryForList(cypher, params.values().toArray());
            
            if (results.isEmpty()) {
                return TraversalResult.builder()
                        .success(true)
                        .totalCount(0)
                        .paths(List.of())
                        .nodes(List.of())
                        .edges(List.of())
                        .build();
            }
            
            return mapToTraversalResult(results);
        } finally {
            clearCurrentGraph(graphName);
        }
    }
    
    private void setCurrentGraph(String graphName) {
        try {
            jdbcTemplate.execute("SET graph_path = " + graphName);
        } catch (Exception e) {
            log.warn("Failed to set graph path: {}", e.getMessage());
        }
    }
    
    private void clearCurrentGraph(String graphName) {
        try {
            jdbcTemplate.execute("RESET graph_path");
        } catch (Exception e) {
            log.warn("Failed to reset graph path: {}", e.getMessage());
        }
    }
    
    private TraversalResult mapToTraversalResult(List<Map<String, Object>> rawResults) {
        List<TraversalResult.PathInfo> paths = new ArrayList<>();
        List<TraversalResult.NodeInfo> nodes = new ArrayList<>();
        List<TraversalResult.EdgeInfo> edges = new ArrayList<>();
        
        Set<String> processedNodeIds = new HashSet<>();
        Set<String> processedEdgeIds = new HashSet<>();
        
        for (int i = 0; i < rawResults.size(); i++) {
            Map<String, Object> row = rawResults.get(i);
            
            String pathId = "path_" + i;
            List<String> pathNodeIds = extractNodeIds(row.get("nodes"));
            List<String> pathEdgeIds = extractEdgeIds(row.get("edges"));
            int depth = pathNodeIds.size() - 1;
            
            paths.add(TraversalResult.PathInfo.builder()
                    .pathId(pathId)
                    .nodeIds(pathNodeIds)
                    .edgeIds(pathEdgeIds)
                    .depth(depth)
                    .build());
            
            for (int j = 0; j < pathNodeIds.size(); j++) {
                String nodeId = pathNodeIds.get(j);
                if (!processedNodeIds.contains(nodeId)) {
                    processedNodeIds.add(nodeId);
                    nodes.add(TraversalResult.NodeInfo.builder()
                            .id(nodeId)
                            .objectType(extractObjectType(row.get("nodes"), j))
                            .objectId(nodeId)
                            .properties(extractNodeProperties(row.get("nodes"), j))
                            .depth(j)
                            .build());
                }
            }
            
            for (String edgeId : pathEdgeIds) {
                if (!processedEdgeIds.contains(edgeId)) {
                    processedEdgeIds.add(edgeId);
                    edges.add(TraversalResult.EdgeInfo.builder()
                            .id(edgeId)
                            .relationType(extractRelationType(row.get("edges"), edgeId))
                            .sourceId(extractSourceId(row.get("edges"), edgeId))
                            .targetId(extractTargetId(row.get("edges"), edgeId))
                            .properties(extractEdgeProperties(row.get("edges"), edgeId))
                            .build());
                }
            }
        }
        
        return TraversalResult.builder()
                .success(true)
                .totalCount(paths.size())
                .paths(paths)
                .nodes(nodes)
                .edges(edges)
                .build();
    }
    
    @SuppressWarnings("unchecked")
    private List<String> extractNodeIds(Object nodesObj) {
        if (nodesObj == null) return List.of();
        if (nodesObj instanceof List) {
            List<?> nodes = (List<?>) nodesObj;
            List<String> ids = new ArrayList<>();
            for (Object node : nodes) {
                if (node instanceof Map) {
                    Map<String, Object> nodeMap = (Map<String, Object>) node;
                    if (nodeMap.containsKey("id")) {
                        ids.add(String.valueOf(nodeMap.get("id")));
                    }
                } else if (node instanceof String) {
                    ids.add((String) node);
                }
            }
            return ids;
        }
        return List.of();
    }
    
    @SuppressWarnings("unchecked")
    private List<String> extractEdgeIds(Object edgesObj) {
        if (edgesObj == null) return List.of();
        if (edgesObj instanceof List) {
            List<?> edges = (List<?>) edgesObj;
            List<String> ids = new ArrayList<>();
            for (Object edge : edges) {
                if (edge instanceof Map) {
                    Map<String, Object> edgeMap = (Map<String, Object>) edge;
                    if (edgeMap.containsKey("id")) {
                        ids.add(String.valueOf(edgeMap.get("id")));
                    }
                } else if (edge instanceof String) {
                    ids.add((String) edge);
                }
            }
            return ids;
        }
        return List.of();
    }
    
    @SuppressWarnings("unchecked")
    private String extractObjectType(Object nodesObj, int index) {
        if (nodesObj instanceof List) {
            List<?> nodes = (List<?>) nodesObj;
            if (index < nodes.size() && nodes.get(index) instanceof Map) {
                Map<String, Object> node = (Map<String, Object>) nodes.get(index);
                Object type = node.get("objectType");
                return type != null ? String.valueOf(type) : "Object";
            }
        }
        return "Object";
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> extractNodeProperties(Object nodesObj, int index) {
        if (nodesObj instanceof List) {
            List<?> nodes = (List<?>) nodesObj;
            if (index < nodes.size() && nodes.get(index) instanceof Map) {
                Map<String, Object> node = (Map<String, Object>) nodes.get(index);
                Map<String, Object> props = new HashMap<>();
                node.forEach((key, value) -> {
                    if (!key.equals("id") && !key.equals("objectType")) {
                        props.put(key, value);
                    }
                });
                return props;
            }
        }
        return Map.of();
    }
    
    @SuppressWarnings("unchecked")
    private String extractRelationType(Object edgesObj, String edgeId) {
        if (edgesObj instanceof List) {
            List<?> edges = (List<?>) edgesObj;
            for (Object edge : edges) {
                if (edge instanceof Map) {
                    Map<String, Object> edgeMap = (Map<String, Object>) edge;
                    if (String.valueOf(edgeMap.get("id")).equals(edgeId)) {
                        Object type = edgeMap.get("relationType");
                        return type != null ? String.valueOf(type) : "RELATED_TO";
                    }
                }
            }
        }
        return "RELATED_TO";
    }
    
    @SuppressWarnings("unchecked")
    private String extractSourceId(Object edgesObj, String edgeId) {
        if (edgesObj instanceof List) {
            List<?> edges = (List<?>) edgesObj;
            for (Object edge : edges) {
                if (edge instanceof Map) {
                    Map<String, Object> edgeMap = (Map<String, Object>) edge;
                    if (String.valueOf(edgeMap.get("id")).equals(edgeId)) {
                        Object sourceId = edgeMap.get("start_id");
                        return sourceId != null ? String.valueOf(sourceId) : null;
                    }
                }
            }
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    private String extractTargetId(Object edgesObj, String edgeId) {
        if (edgesObj instanceof List) {
            List<?> edges = (List<?>) edgesObj;
            for (Object edge : edges) {
                if (edge instanceof Map) {
                    Map<String, Object> edgeMap = (Map<String, Object>) edge;
                    if (String.valueOf(edgeMap.get("id")).equals(edgeId)) {
                        Object targetId = edgeMap.get("end_id");
                        return targetId != null ? String.valueOf(targetId) : null;
                    }
                }
            }
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> extractEdgeProperties(Object edgesObj, String edgeId) {
        if (edgesObj instanceof List) {
            List<?> edges = (List<?>) edgesObj;
            for (Object edge : edges) {
                if (edge instanceof Map) {
                    Map<String, Object> edgeMap = (Map<String, Object>) edge;
                    if (String.valueOf(edgeMap.get("id")).equals(edgeId)) {
                        Map<String, Object> props = new HashMap<>();
                        edgeMap.forEach((key, value) -> {
                            if (!key.equals("id") && !key.equals("relationType") 
                                && !key.equals("start_id") && !key.equals("end_id")) {
                                props.put(key, value);
                            }
                        });
                        return props;
                    }
                }
            }
        }
        return Map.of();
    }
}
