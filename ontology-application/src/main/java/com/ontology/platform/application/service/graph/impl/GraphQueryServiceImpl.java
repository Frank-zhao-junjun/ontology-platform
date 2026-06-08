package com.ontology.platform.application.service.graph.impl;

import com.ontology.platform.application.security.FilterSecurityValidator;
import com.ontology.platform.application.security.GraphTraversalDSLParser;
import com.ontology.platform.application.service.graph.GraphQueryService;
import com.ontology.platform.common.enums.ErrorCode;
import com.ontology.platform.common.exception.BusinessException;
import com.ontology.platform.common.exception.ResourceNotFoundException;
import com.ontology.platform.domain.repository.OntologyRepository;
import com.ontology.platform.domain.service.GraphWhitelistService;
import com.ontology.platform.domain.vo.traversal.*;
import com.ontology.platform.infrastructure.graph.AgeQueryExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 图查询服务实现
 * Graph Query Service Implementation
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GraphQueryServiceImpl implements GraphQueryService {
    
    private final AgeQueryExecutor ageQueryExecutor;
    private final GraphTraversalDSLParser dslParser;
    private final FilterSecurityValidator filterValidator;
    private final GraphWhitelistService whitelistService;
    private final OntologyRepository ontologyRepository;
    
    @Override
    public TraversalResult traverse(String ontologyId, GraphTraversalRequest request) {
        long startTime = System.currentTimeMillis();
        
        log.info("Executing graph traversal: ontologyId={}, startObjectId={}, maxDepth={}", 
            ontologyId, request.getStartObjectId(), request.getEffectiveDepth());
        
        try {
            // 1. 验证本体存在
            validateOntologyExists(ontologyId);
            
            // 2. 验证起点对象存在
            validateObjectExists(ontologyId, request.getStartObjectId());
            
            // 3. 解析请求为Cypher查询
            GraphTraversalDSLParser.CypherQuery cypherQuery = dslParser.parse(request);
            
            // 4. 执行查询
            TraversalResult result = ageQueryExecutor.executeTraversal(ontologyId, cypherQuery);
            
            // 5. 后处理：应用属性选择器
            result = applyPropertySelector(result, request);
            
            long executionTime = System.currentTimeMillis() - startTime;
            result.setExecutionTimeMs(executionTime);
            
            log.info("Graph traversal completed: totalCount={}, executionTime={}ms", 
                result.getTotalCount(), executionTime);
            
            return result;
            
        } catch (BusinessException e) {
            log.error("Graph traversal business error: {}", e.getMessage());
            return TraversalResult.failure(e.getMessage());
        } catch (Exception e) {
            log.error("Graph traversal error: {}", e.getMessage(), e);
            return TraversalResult.failure("Graph traversal failed: " + e.getMessage());
        }
    }
    
    @Override
    public TraversalResult findShortestPath(String ontologyId, String fromObjectId, 
            String toObjectId, int maxDepth) {
        
        log.info("Finding shortest path: ontologyId={}, from={}, to={}, maxDepth={}", 
            ontologyId, fromObjectId, toObjectId, maxDepth);
        
        if (fromObjectId == null || toObjectId == null) {
            throw new BusinessException(ErrorCode.INVALID_TRAVERSAL_REQUEST, 
                "fromObjectId and toObjectId are required");
        }
        
        int effectiveDepth = Math.min(maxDepth, 5);
        
        GraphTraversalRequest request = GraphTraversalRequest.builder()
                .startObjectType("Object")
                .startObjectId(fromObjectId)
                .maxDepth(effectiveDepth)
                .limit(1)
                .build();
        
        List<TraversalFilter> filters = new ArrayList<>();
        filters.add(TraversalFilter.builder()
                .depth(effectiveDepth)
                .conditions(List.of(
                    TraversalFilterCondition.builder()
                            .field("id")
                            .operator(com.ontology.platform.common.enums.FilterOperator.eq)
                            .value(toObjectId)
                            .build()
                ))
                .logic("AND")
                .build());
        request.setFilters(filters);
        
        return traverse(ontologyId, request);
    }
    
    @Override
    public TraversalResult extractSubgraph(String ontologyId, String rootObjectId, int depth) {
        log.info("Extracting subgraph: ontologyId={}, rootObjectId={}, depth={}", 
            ontologyId, rootObjectId, depth);
        
        validateOntologyExists(ontologyId);
        validateObjectExists(ontologyId, rootObjectId);
        
        GraphTraversalRequest request = GraphTraversalRequest.builder()
                .startObjectId(rootObjectId)
                .maxDepth(Math.min(depth, 5))
                .limit(1000)
                .build();
        
        return traverse(ontologyId, request);
    }
    
    @Override
    public boolean ontologyExists(String ontologyId) {
        if (ontologyId == null || ontologyId.isBlank()) {
            return false;
        }
        return ontologyRepository.findById(ontologyId).isPresent();
    }
    
    @Override
    public boolean objectExists(String ontologyId, String objectId) {
        if (ontologyId == null || objectId == null) {
            return false;
        }
        return true;
    }
    
    private void validateOntologyExists(String ontologyId) {
        if (!ontologyExists(ontologyId)) {
            throw new ResourceNotFoundException("Ontology", ontologyId);
        }
    }
    
    private void validateObjectExists(String ontologyId, String objectId) {
        if (!objectExists(ontologyId, objectId)) {
            throw new ResourceNotFoundException("Object", objectId);
        }
    }
    
    private TraversalResult applyPropertySelector(TraversalResult result, GraphTraversalRequest request) {
        List<String> include = request.getIncludeProperties();
        List<String> exclude = request.getExcludeProperties();
        
        if ((include == null || include.isEmpty()) && (exclude == null || exclude.isEmpty())) {
            return result;
        }
        
        List<TraversalResult.NodeInfo> filteredNodes = new ArrayList<>();
        for (TraversalResult.NodeInfo node : result.getNodes()) {
            Map<String, Object> filteredProps = filterProperties(node.getProperties(), include, exclude);
            filteredNodes.add(TraversalResult.NodeInfo.builder()
                    .id(node.getId())
                    .objectType(node.getObjectType())
                    .objectId(node.getObjectId())
                    .properties(filteredProps)
                    .depth(node.getDepth())
                    .build());
        }
        
        List<TraversalResult.EdgeInfo> filteredEdges = new ArrayList<>();
        for (TraversalResult.EdgeInfo edge : result.getEdges()) {
            Map<String, Object> filteredProps = filterProperties(edge.getProperties(), include, exclude);
            filteredEdges.add(TraversalResult.EdgeInfo.builder()
                    .id(edge.getId())
                    .relationType(edge.getRelationType())
                    .sourceId(edge.getSourceId())
                    .targetId(edge.getTargetId())
                    .properties(filteredProps)
                    .build());
        }
        
        return TraversalResult.builder()
                .success(result.isSuccess())
                .totalCount(result.getTotalCount())
                .paths(result.getPaths())
                .nodes(filteredNodes)
                .edges(filteredEdges)
                .executionTimeMs(result.getExecutionTimeMs())
                .errorMessage(result.getErrorMessage())
                .build();
    }
    
    private Map<String, Object> filterProperties(Map<String, Object> properties, 
            List<String> include, List<String> exclude) {
        
        if (properties == null || properties.isEmpty()) {
            return Map.of();
        }
        
        Map<String, Object> result = new HashMap<>();
        
        if (include != null && !include.isEmpty()) {
            for (String key : include) {
                if (properties.containsKey(key)) {
                    result.put(key, properties.get(key));
                }
            }
        } else {
            result.putAll(properties);
            if (exclude != null) {
                for (String key : exclude) {
                    result.remove(key);
                }
            }
        }
        
        return result;
    }
}
