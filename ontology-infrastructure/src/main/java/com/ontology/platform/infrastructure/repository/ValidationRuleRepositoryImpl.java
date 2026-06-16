package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.domain.entity.ValidationRule;
import com.ontology.platform.domain.repository.ValidationRuleRepository;
import com.ontology.platform.infrastructure.converter.ValidationRuleConverter;
import com.ontology.platform.infrastructure.persistence.ValidationRulePO;
import com.ontology.platform.infrastructure.persistence.ValidationRulePOMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ValidationRuleRepositoryImpl implements ValidationRuleRepository {

    private final ValidationRulePOMapper validationRulePOMapper;
    private final ValidationRuleConverter validationRuleConverter;

    @Override
    public Optional<ValidationRule> findById(String id) {
        log.debug("Finding validation rule by id: {}", id);
        ValidationRulePO po = validationRulePOMapper.selectById(id);
        return Optional.ofNullable(validationRuleConverter.toEntity(po));
    }

    @Override
    public List<ValidationRule> findByOntologyId(String ontologyId) {
        log.debug("Finding validation rules by ontologyId: {}", ontologyId);
        List<ValidationRulePO> poList = validationRulePOMapper.selectByOntologyId(ontologyId);
        return validationRuleConverter.toEntityList(poList);
    }

    @Override
    public ValidationRule save(ValidationRule entity) {
        log.debug("Saving validation rule: {}", entity.getId());
        ValidationRulePO po = validationRuleConverter.toPO(entity);
        if (po.getCreatedAt() == null) {
            po.setCreatedAt(Instant.now());
        }
        po.setUpdatedAt(Instant.now());
        if (validationRulePOMapper.selectById(entity.getId()) != null) {
            validationRulePOMapper.updateById(po);
        } else {
            validationRulePOMapper.insert(po);
        }
        return entity;
    }

    @Override
    public void deleteById(String id) {
        log.debug("Soft-deleting validation rule: {}", id);
        ValidationRulePO po = validationRulePOMapper.selectById(id);
        if (po != null) {
            po.setDeleted(true);
            po.setUpdatedAt(Instant.now());
            validationRulePOMapper.updateById(po);
        }
    }
}
