package com.ontology.platform.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 本体持久化Mapper接口
 * Ontology Persistence Mapper Interface
 */
@Mapper
public interface OntologyPOMapper extends BaseMapper<OntologyPO> {

    /**
     * 根据租户ID和名称查询
     */
    OntologyPO selectByTenantIdAndName(@Param("tenantId") String tenantId, @Param("name") String name);

    /**
     * 根据租户ID和名称查询（排除指定ID）
     */
    OntologyPO selectByTenantIdAndNameExcludingId(
            @Param("tenantId") String tenantId, 
            @Param("name") String name,
            @Param("excludeId") String excludeId);

    /**
     * 根据租户ID分页查询
     */
    List<OntologyPO> selectByTenantIdWithPage(
            @Param("tenantId") String tenantId,
            @Param("offset") int offset,
            @Param("limit") int limit);

    /**
     * 根据租户ID统计数量
     */
    long countByTenantId(@Param("tenantId") String tenantId);

    /**
     * 根据租户ID和状态查询
     */
    List<OntologyPO> selectByTenantIdAndStatus(
            @Param("tenantId") String tenantId, 
            @Param("status") String status);

    /**
     * 检查是否存在（租户ID+名称）
     */
    int countByTenantIdAndName(@Param("tenantId") String tenantId, @Param("name") String name);

    /**
     * 检查是否存在（租户ID+名称，排除指定ID）
     */
    int countByTenantIdAndNameExcludingId(
            @Param("tenantId") String tenantId, 
            @Param("name") String name,
            @Param("excludeId") String excludeId);
}
