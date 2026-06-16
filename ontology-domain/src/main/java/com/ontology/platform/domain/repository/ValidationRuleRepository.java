package com.ontology.platform.domain.repository;

import com.ontology.platform.domain.entity.ValidationRule;
import java.util.List;
import java.util.Optional;

/**
 * 校验规则仓储接口
 * Validation Rule Repository Interface
 */
public interface ValidationRuleRepository {

    /**
     * 根据ID查询校验规则
     */
    Optional<ValidationRule> findById(String id);

    /**
     * 根据本体ID查询所有校验规则
     */
    List<ValidationRule> findByOntologyId(String ontologyId);

    /**
     * 保存校验规则
     */
    ValidationRule save(ValidationRule entity);

    /**
     * 根据ID删除校验规则
     */
    void deleteById(String id);
}
