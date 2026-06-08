package com.ontology.platform.application.service.graph;

import com.ontology.platform.domain.vo.traversal.GraphTraversalRequest;
import com.ontology.platform.domain.vo.traversal.TraversalResult;

/**
 * 图查询服务接口
 * Graph Query Service Interface
 */
public interface GraphQueryService {
    
    /**
     * 执行图遍历查询
     * @param ontologyId 本体ID
     * @param request 图遍历请求
     * @return 遍历结果
     */
    TraversalResult traverse(String ontologyId, GraphTraversalRequest request);
    
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
    
    /**
     * 验证本体是否存在
     * @param ontologyId 本体ID
     * @return 是否存在
     */
    boolean ontologyExists(String ontologyId);
    
    /**
     * 验证对象是否存在
     * @param ontologyId 本体ID
     * @param objectId 对象ID
     * @return 是否存在
     */
    boolean objectExists(String ontologyId, String objectId);
}
