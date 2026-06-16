package com.ontology.platform.domain.repository;

import com.ontology.platform.domain.entity.PolicyRule;
import java.util.List;
import java.util.Optional;

/**
 * 策略规则仓储接口
 * Policy Rule Repository Interface
 */
public interface PolicyRuleRepository {

    /**
     * 根据ID查询策略规则
     */
    Optional<PolicyRule> findById(String id);

    /**
     * 根据本体ID查询所有策略规则
     */
    List<PolicyRule> findByOntologyId(String ontologyId);

    /**
     * 保存策略规则
     */
    PolicyRule save(PolicyRule entity);

    /**
     * 根据ID删除策略规则
     */
    void deleteById(String id);
}
