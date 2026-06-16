package com.ontology.platform.domain.repository;

import com.ontology.platform.domain.entity.GuardrailRule;
import java.util.List;
import java.util.Optional;

/**
 * 护栏规则仓储接口
 * Guardrail Rule Repository Interface
 */
public interface GuardrailRuleRepository {

    /**
     * 根据ID查询护栏规则
     */
    Optional<GuardrailRule> findById(String id);

    /**
     * 根据本体ID查询所有护栏规则
     */
    List<GuardrailRule> findByOntologyId(String ontologyId);

    /**
     * 保存护栏规则
     */
    GuardrailRule save(GuardrailRule entity);

    /**
     * 根据ID删除护栏规则
     */
    void deleteById(String id);
}
