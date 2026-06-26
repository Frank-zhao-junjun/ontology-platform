package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.domain.entity.BusinessScenario;
import com.ontology.platform.domain.repository.BusinessScenarioRepository;
import com.ontology.platform.infrastructure.converter.BusinessScenarioConverter;
import com.ontology.platform.infrastructure.persistence.BusinessScenarioPO;
import com.ontology.platform.infrastructure.persistence.BusinessScenarioPOMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * 业务场景仓储实现
 * BusinessScenario Repository Implementation
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class BusinessScenarioRepositoryImpl implements BusinessScenarioRepository {

    private final BusinessScenarioPOMapper mapper;
    private final BusinessScenarioConverter converter;

    @Override
    public Optional<BusinessScenario> findById(String id) {
        BusinessScenarioPO po = mapper.selectById(id);
        return Optional.ofNullable(converter.toEntity(po));
    }

    @Override
    public List<BusinessScenario> findByOntologyId(String ontologyId) {
        List<BusinessScenarioPO> poList = mapper.selectByOntologyId(ontologyId);
        return converter.toEntityList(poList);
    }

    @Override
    public BusinessScenario save(BusinessScenario businessScenario) {
        if (businessScenario.getCreatedAt() == null) {
            businessScenario.setCreatedAt(Instant.now());
        }
        if (businessScenario.getUpdatedAt() == null) {
            businessScenario.setUpdatedAt(Instant.now());
        }
        BusinessScenarioPO po = converter.toPO(businessScenario);
        mapper.insert(po);
        return businessScenario;
    }

    @Override
    public void deleteById(String id) {
        mapper.deleteById(id);
    }
}
