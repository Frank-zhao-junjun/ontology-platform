package com.ontology.platform.application.service;

import com.ontology.platform.application.dto.*;

import java.util.List;

/**
 * 本体服务接口
 * Ontology Service Interface
 */
public interface OntologyService {

    // ==================== 本体管理 ====================

    /**
     * 创建本体
     */
    OntologyResponse createOntology(CreateOntologyRequest request, String userId);

    /**
     * 获取本体详情
     */
    OntologyDetailResponse getOntologyById(String id);

    /**
     * 获取本体列表
     */
    List<OntologyResponse> listOntologies(String tenantId, int page, int pageSize);

    /**
     * 更新本体
     */
    OntologyResponse updateOntology(String id, UpdateOntologyRequest request);

    /**
     * 删除本体
     */
    void deleteOntology(String id);

    /**
     * 发布本体
     */
    OntologyResponse publishOntology(String id);

    /**
     * 归档本体
     */
    OntologyResponse archiveOntology(String id);

    /**
     * 验证本体
     */
    ValidationResultResponse validateOntology(String id);

    // ==================== 对象类型管理 ====================

    /**
     * 创建对象类型
     */
    ObjectTypeResponse createObjectType(CreateObjectTypeRequest request);

    /**
     * 获取对象类型详情
     */
    ObjectTypeDetailResponse getObjectTypeById(String id);

    /**
     * 获取对象类型列表
     */
    List<ObjectTypeResponse> listObjectTypes(String ontologyId);

    /**
     * 更新对象类型
     */
    ObjectTypeResponse updateObjectType(String id, UpdateObjectTypeRequest request);

    /**
     * 删除对象类型
     */
    void deleteObjectType(String id);

    // ==================== 属性管理 ====================

    /**
     * 创建属性
     */
    PropertyResponse createProperty(CreatePropertyRequest request);

    /**
     * 更新属性
     */
    PropertyResponse updateProperty(String id, UpdatePropertyRequest request);

    /**
     * 删除属性
     */
    void deleteProperty(String id);

    // ==================== 关系管理 ====================

    /**
     * 创建关系
     */
    RelationResponse createRelation(CreateRelationRequest request);

    /**
     * 更新关系
     */
    RelationResponse updateRelation(String id, UpdateRelationRequest request);

    /**
     * 删除关系
     */
    void deleteRelation(String id);

    // ==================== 查询 ====================

    /**
     * 图遍历查询
     */
    GraphQueryResponse graphTraversal(GraphQueryRequest request);

    /**
     * 对象列表查询
     */
    ObjectListResponse queryObjects(ObjectQueryRequest request);
}
