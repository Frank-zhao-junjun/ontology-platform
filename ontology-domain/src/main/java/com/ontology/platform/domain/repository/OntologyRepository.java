package com.ontology.platform.domain.repository;

import com.ontology.platform.common.enums.OntologyStatus;
import com.ontology.platform.domain.entity.Ontology;

import java.util.List;
import java.util.Optional;

/**
 * 本体仓储接口
 * Ontology Repository Interface
 */
public interface OntologyRepository {

    /**
     * 根据ID查询本体
     */
    Optional<Ontology> findById(String id);

    /**
     * 根据租户ID和名称查询本体
     */
    Optional<Ontology> findByTenantIdAndName(String tenantId, String name);

    /**
     * 查询租户下的所有本体
     */
    List<Ontology> findByTenantId(String tenantId);

    /**
     * 根据状态查询租户下的本体
     */
    List<Ontology> findByTenantIdAndStatus(String tenantId, OntologyStatus status);

    /**
     * 保存本体
     */
    Ontology save(Ontology ontology);

    /**
     * 更新本体
     */
    Ontology update(Ontology ontology);

    /**
     * 删除本体
     */
    void deleteById(String id);

    /**
     * 检查本体是否存在
     */
    boolean existsByTenantIdAndName(String tenantId, String name);

    /**
     * 检查本体是否存在（排除指定ID）
     */
    boolean existsByTenantIdAndNameAndIdNot(String tenantId, String name, String excludeId);

    /**
     * 统计租户下的本体数量
     */
    long countByTenantId(String tenantId);

    /**
     * 分页查询本体
     */
    List<Ontology> findByTenantIdWithPage(String tenantId, int page, int pageSize);
}
