package com.ontology.platform.application.service;

import com.ontology.platform.application.dto.*;

import java.util.List;

/**
 * 关系服务接口
 * Relation Service Interface
 */
public interface RelationService {

    /**
     * 创建关系
     * 
     * @param request 创建关系请求
     * @return 关系响应
     */
    RelationResponse createRelation(CreateRelationRequest request);

    /**
     * 获取关系详情
     * 
     * @param id 关系ID
     * @return 关系响应
     */
    RelationResponse getRelationById(String id);

    /**
     * 获取本体下的所有关系
     * 
     * @param ontologyId 本体ID
     * @return 关系列表
     */
    List<RelationResponse> listRelations(String ontologyId);

    /**
     * 更新关系
     * 
     * @param id 关系ID
     * @param request 更新关系请求
     * @return 关系响应
     */
    RelationResponse updateRelation(String id, UpdateRelationRequest request);

    /**
     * 删除关系
     * 
     * @param id 关系ID
     */
    void deleteRelation(String id);

    /**
     * 按源对象类型查询关系
     * 
     * @param sourceTypeId 源对象类型ID
     * @return 关系列表
     */
    List<RelationResponse> findBySourceTypeId(String sourceTypeId);

    /**
     * 按目标对象类型查询关系
     * 
     * @param targetTypeId 目标对象类型ID
     * @return 关系列表
     */
    List<RelationResponse> findByTargetTypeId(String targetTypeId);

    /**
     * 查询指定关系关联的所有对象类型
     * 
     * @param relationId 关系ID
     * @return 对象类型列表
     */
    List<ObjectTypeResponse> findRelatedObjectTypes(String relationId);
}
