package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.domain.entity.GuardrailRule;
import com.ontology.platform.domain.repository.GuardrailRuleRepository;
import com.ontology.platform.infrastructure.converter.GuardrailRuleConverter;
import com.ontology.platform.infrastructure.persistence.GuardrailRulePO;
import com.ontology.platform.infrastructure.persistence.GuardrailRulePOMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class GuardrailRuleRepositoryImpl implements GuardrailRuleRepository {

    private final GuardrailRulePOMapper guardrailRulePOMapper;
    private final GuardrailRuleConverter guardrailRuleConverter;

    @Override
    public Optional<GuardrailRule> findById(String id) {
        log.debug("Finding guardrail rule by id: {}", id);
        GuardrailRulePO po = guardrailRulePOMapper.selectById(id);
        return Optional.ofNullable(guardrailRuleConverter.toEntity(po));
    }

    @Override
    public List<GuardrailRule> findByOntologyId(String ontologyId) {
        log.debug("Finding guardrail rules by ontologyId: {}", ontologyId);
        List<GuardrailRulePO> poList = guardrailRulePOMapper.selectByOntologyId(ontologyId);
        return guardrailRuleConverter.toEntityList(poList);
    }

    @Override
    public GuardrailRule save(GuardrailRule entity) {
        log.debug("Saving guardrail rule: {}", entity.getId());
        GuardrailRulePO po = guardrailRuleConverter.toPO(entity);
        if (po.getCreatedAt() == null) {
            po.setCreatedAt(Instant.now());
        }
        po.setUpdatedAt(Instant.now());
        if (guardrailRulePOMapper.selectById(entity.getId()) != null) {
            guardrailRulePOMapper.updateById(po);
        } else {
            guardrailRulePOMapper.insert(po);
        }
        return entity;
    }

    @Override
    public void deleteById(String id) {
        log.debug("Soft-deleting guardrail rule: {}", id);
        GuardrailRulePO po = guardrailRulePOMapper.selectById(id);
        if (po != null) {
            po.setDeleted(true);
            po.setUpdatedAt(Instant.now());
            guardrailRulePOMapper.updateById(po);
        }
    }
}
