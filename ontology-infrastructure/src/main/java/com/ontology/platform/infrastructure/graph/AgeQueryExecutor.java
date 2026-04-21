package com.ontology.platform.infrastructure.graph;

import com.ontology.platform.application.security.GraphTraversalDSLParser;
import com.ontology.platform.domain.vo.traversal.TraversalResult;

import java.util.Map;

/**
 * Apache AGE查询执行器接口
 * Apache AGE Query Executor Interface
 */
public interface AgeQueryExecutor {
    
    /**
     * 执行图遍历查询
     * @param ontologyId 本体ID
     * @param cypherQuery Cypher查询
     * @return 遍历结果
     */
    TraversalResult executeTraversal(String ontologyId, GraphTraversalDSLParser.CypherQuery cypherQuery);
    
    /**
     * 执行Cypher查询
     * @param ontologyId 本体ID
     * @param cypher Cypher语句
     * @param params 参数
     * @return 查询结果
     */
    Map<String, Object> executeQuery(String ontologyId, String cypher, Map<String, Object> params);
    
    /**
     * 执行路径查询
     * @param ontologyId 本体ID
     * @param fromObjectId 起始节点
     * @param toObjectId 目标节点
     * @param maxDepth 最大深度
     * @return 路径结果
     */
    TraversalResult findShortestPath(String ontologyId, String fromObjectId, 
            String toObjectId, int maxDepth);
}
