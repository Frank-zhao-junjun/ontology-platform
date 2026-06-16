package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.domain.entity.PolicyRule;
import com.ontology.platform.domain.repository.PolicyRuleRepository;
import com.ontology.platform.infrastructure.converter.PolicyRuleConverter;
import com.ontology.platform.infrastructure.persistence.PolicyRulePO;
import com.ontology.platform.infrastructure.persistence.PolicyRulePOMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PolicyRuleRepositoryImpl implements PolicyRuleRepository {

    private final PolicyRulePOMapper policyRulePOMapper;
    private final PolicyRuleConverter policyRuleConverter;

    @Override
    public Optional<PolicyRule> findById(String id) {
        log.debug("Finding policy rule by id: {}", id);
        PolicyRulePO po = policyRulePOMapper.selectById(id);
        return Optional.ofNullable(policyRuleConverter.toEntity(po));
    }

    @Override
    public List<PolicyRule> findByOntologyId(String ontologyId) {
        log.debug("Finding policy rules by ontologyId: {}", ontologyId);
        List<PolicyRulePO> poList = policyRulePOMapper.selectByOntologyId(ontologyId);
        return policyRuleConverter.toEntityList(poList);
    }

    @Override
    public PolicyRule save(PolicyRule entity) {
        log.debug("Saving policy rule: {}", entity.getId());
        PolicyRulePO po = policyRuleConverter.toPO(entity);
        if (po.getCreatedAt() == null) {
            po.setCreatedAt(Instant.now());
        }
        po.setUpdatedAt(Instant.now());
        if (policyRulePOMapper.selectById(entity.getId()) != null) {
            policyRulePOMapper.updateById(po);
        } else {
            policyRulePOMapper.insert(po);
        }
        return entity;
    }

    @Override
    public void deleteById(String id) {
        log.debug("Soft-deleting policy rule: {}", id);
        PolicyRulePO po = policyRulePOMapper.selectById(id);
        if (po != null) {
            po.setDeleted(true);
            po.setUpdatedAt(Instant.now());
            policyRulePOMapper.updateById(po);
        }
    }
}
