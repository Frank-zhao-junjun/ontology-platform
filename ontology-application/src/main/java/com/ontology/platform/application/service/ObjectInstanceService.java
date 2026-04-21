package com.ontology.platform.application.service;

import com.ontology.platform.application.dto.*;

import java.util.List;

/**
 * 对象实例服务接口
 * ObjectInstance Service Interface
 */
public interface ObjectInstanceService {

    // ==================== 基础CRUD ====================

    /**
     * 创建对象实例
     */
    InstanceResponse createInstance(String ontologyId, CreateInstanceRequest request, String userId);

    /**
     * 获取对象实例
     */
    InstanceResponse getInstance(String ontologyId, String id);

    /**
     * 获取对象实例（通过主键值）
     */
    InstanceResponse getInstanceByPrimaryKey(String ontologyId, String objectTypeId, String primaryKeyValue);

    /**
     * 查询对象实例列表
     */
    ObjectListResponse<InstanceResponse> listInstances(String ontologyId, InstanceQuery query);

    /**
     * 更新对象实例
     */
    InstanceResponse updateInstance(String ontologyId, String id, UpdateInstanceRequest request);

    /**
     * 删除对象实例
     */
    void deleteInstance(String ontologyId, String id);

    /**
     * 根据对象类型查询实例
     */
    ObjectListResponse<InstanceResponse> listInstancesByType(String ontologyId, String typeId);

    // ==================== 批量操作 ====================

    /**
     * 批量导入实例
     */
    BatchImportResponse batchImport(String ontologyId, BatchImportRequest request, String userId);

    /**
     * 批量删除实例
     */
    void batchDelete(String ontologyId, List<String> instanceIds);

    // ==================== 验证 ====================

    /**
     * 验证单个实例
     */
    InstanceValidationResponse validateInstance(String ontologyId, String id);

    /**
     * 批量验证实例
     */
    InstanceValidationResponse validateInstances(String ontologyId, List<String> instanceIds, boolean strictMode);

    // ==================== 状态管理 ====================

    /**
     * 激活实例
     */
    InstanceResponse activateInstance(String ontologyId, String id);

    /**
     * 停用实例
     */
    InstanceResponse deactivateInstance(String ontologyId, String id);
}
