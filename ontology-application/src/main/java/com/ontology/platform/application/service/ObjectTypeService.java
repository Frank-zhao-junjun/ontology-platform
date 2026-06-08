package com.ontology.platform.application.service;

import com.ontology.platform.application.dto.*;

import java.util.List;

/**
 * 对象类型服务接口
 * ObjectType Service Interface
 */
public interface ObjectTypeService {

    // ==================== 对象类型管理 ====================

    ObjectTypeResponse createObjectType(CreateObjectTypeRequest request);

    ObjectTypeDetailResponse getObjectTypeById(String id);

    List<ObjectTypeResponse> listObjectTypes(String ontologyId);

    ObjectTypeResponse updateObjectType(String id, UpdateObjectTypeRequest request);

    void deleteObjectType(String id);

    // ==================== 属性管理 ====================

    PropertyResponse createProperty(CreatePropertyRequest request);

    PropertyResponse updateProperty(String id, UpdatePropertyRequest request);

    void deleteProperty(String id);

    List<PropertyResponse> listProperties(String objectTypeId);

    // ==================== 批量操作 ====================

    List<PropertyResponse> batchCreateProperties(String objectTypeId, List<CreatePropertyRequest> requests);

    record PropertyUpdateItem(String id, UpdatePropertyRequest request) {}
}
