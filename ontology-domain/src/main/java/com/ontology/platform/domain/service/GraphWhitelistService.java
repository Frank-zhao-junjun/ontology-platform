package com.ontology.platform.domain.service;

import java.util.List;
import java.util.Set;

/**
 * 图遍历白名单服务接口
 * Graph Traversal Whitelist Service Interface
 * 
 * 负责管理图遍历相关的白名单配置
 */
public interface GraphWhitelistService {
    
    /**
     * 检查关系类型是否在白名单中
     * @param relationType 关系类型名称
     * @return 是否允许
     */
    boolean isRelationTypeAllowed(String relationType);
    
    /**
     * 检查对象类型是否在白名单中
     * @param objectType 对象类型名称
     * @return 是否允许
     */
    boolean isObjectTypeAllowed(String objectType);
    
    /**
     * 检查属性字段是否在白名单中
     * @param objectType 对象类型名称
     * @param fieldName 属性字段名
     * @return 是否允许
     */
    boolean isPropertyAllowed(String objectType, String fieldName);
    
    /**
     * 获取对象类型的所有允许属性
     * @param objectType 对象类型名称
     * @return 属性集合
     */
    Set<String> getAllowedProperties(String objectType);
    
    /**
     * 获取本体允许的关系类型列表
     * @param ontologyId 本体ID
     * @return 关系类型集合
     */
    Set<String> getAllowedRelationTypes(String ontologyId);
    
    /**
     * 获取本体允许的对象类型列表
     * @param ontologyId 本体ID
     * @return 对象类型集合
     */
    Set<String> getAllowedObjectTypes(String ontologyId);
    
    /**
     * 验证并获取规范化的关系类型（转大写）
     * @param relationType 关系类型
     * @return 规范化后的关系类型
     */
    String normalizeRelationType(String relationType);
    
    /**
     * 验证并获取规范化的对象类型（转小写）
     * @param objectType 对象类型
     * @return 规范化后的对象类型
     */
    String normalizeObjectType(String objectType);
}
