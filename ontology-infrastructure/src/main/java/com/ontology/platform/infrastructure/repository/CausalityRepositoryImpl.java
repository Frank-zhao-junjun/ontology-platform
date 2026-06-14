package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.domain.entity.Causality;
import com.ontology.platform.domain.repository.CausalityRepository;
import com.ontology.platform.infrastructure.converter.CausalityConverter;
import com.ontology.platform.infrastructure.persistence.CausalityPO;
import com.ontology.platform.infrastructure.persistence.CausalityPOMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CausalityRepositoryImpl implements CausalityRepository {

    private final CausalityPOMapper causalityPOMapper;
    private final CausalityConverter causalityConverter;

    @Override
    public Optional<Causality> findById(String id) {
        log.debug("Finding causality by id: {}", id);
        CausalityPO po = causalityPOMapper.selectById(id);
        return Optional.ofNullable(causalityConverter.toEntity(po));
    }

    @Override
    public List<Causality> findByOntologyId(String ontologyId) {
        log.debug("Finding causalities by ontologyId: {}", ontologyId);
        List<CausalityPO> poList = causalityPOMapper.selectByOntologyId(ontologyId);
        return causalityConverter.toEntityList(poList);
    }

    @Override
    public List<Causality> findByCauseEventId(String causeEventId) {
        log.debug("Finding causalities by causeEventId: {}", causeEventId);
        List<CausalityPO> poList = causalityPOMapper.selectByCauseEventId(causeEventId);
        return causalityConverter.toEntityList(poList);
    }

    @Override
    public List<Causality> findByEffectEventId(String effectEventId) {
        log.debug("Finding causalities by effectEventId: {}", effectEventId);
        List<CausalityPO> poList = causalityPOMapper.selectByEffectEventId(effectEventId);
        return causalityConverter.toEntityList(poList);
    }

    @Override
    public Causality save(Causality entity) {
        log.debug("Saving causality: {}", entity.getId());
        CausalityPO po = causalityConverter.toPO(entity);
        if (causalityPOMapper.selectById(entity.getId()) != null) {
            causalityPOMapper.updateById(po);
        } else {
            causalityPOMapper.insert(po);
        }
        return entity;
    }

    @Override
    public void deleteById(String id) {
        log.debug("Deleting causality: {}", id);
        causalityPOMapper.deleteById(id);
    }
}
