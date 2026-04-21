package com.ontology.platform.domain.service;

import com.ontology.platform.domain.vo.traversal.GraphTraversalRequest;
import com.ontology.platform.domain.vo.traversal.TraversalResult;

/**
 * 图遍历服务接口
 * Graph Traversal Service Interface
 */
public interface GraphTraversalService {
    
    /**
     * 执行图遍历查询
     * @param request 图遍历请求
     * @return 图遍历结果
     */
    TraversalResult traverse(GraphTraversalRequest request);
    
    /**
     * 查询两个节点之间的最短路径
     * @param ontologyId 本体ID
     * @param fromObjectId 起始节点ID
     * @param toObjectId 目标节点ID
     * @param maxDepth 最大深度
     * @return 遍历结果
     */
    TraversalResult findShortestPath(String ontologyId, String fromObjectId, String toObjectId, int maxDepth);
    
    /**
     * 提取子图
     * @param ontologyId 本体ID
     * @param rootObjectId 根节点ID
     * @param depth 深度
     * @return 遍历结果
     */
    TraversalResult extractSubgraph(String ontologyId, String rootObjectId, int depth);
}
