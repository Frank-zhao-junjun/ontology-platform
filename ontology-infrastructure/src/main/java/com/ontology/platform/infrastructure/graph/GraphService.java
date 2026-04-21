package com.ontology.platform.infrastructure.graph;

import com.ontology.platform.domain.entity.ObjectInstance;

/**
 * 图数据库服务接口
 * Graph Database Service Interface
 * 用于与Apache AGE图数据库交互
 */
public interface GraphService {

    /**
     * 创建图顶点
     */
    void createVertex(ObjectInstance instance);

    /**
     * 更新图顶点
     */
    void updateVertex(ObjectInstance instance);

    /**
     * 删除图顶点
     */
    void deleteVertex(ObjectInstance instance);

    /**
     * 创建关系边
     */
    void createEdge(String sourceInstanceId, String targetInstanceId, String relationName);

    /**
     * 删除关系边
     */
    void deleteEdge(String sourceInstanceId, String targetInstanceId, String relationName);

    /**
     * 查找顶点的所有关系
     */
    Object findVertexRelations(String instanceId);
}
