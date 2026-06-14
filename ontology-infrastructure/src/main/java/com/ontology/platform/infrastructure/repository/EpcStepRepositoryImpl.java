package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.domain.entity.EpcStep;
import com.ontology.platform.domain.repository.EpcStepRepository;
import com.ontology.platform.infrastructure.converter.EpcStepConverter;
import com.ontology.platform.infrastructure.persistence.EpcStepPO;
import com.ontology.platform.infrastructure.persistence.EpcStepPOMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class EpcStepRepositoryImpl implements EpcStepRepository {

    private final EpcStepPOMapper epcStepPOMapper;
    private final EpcStepConverter epcStepConverter;

    @Override
    public Optional<EpcStep> findById(String id) {
        log.debug("Finding epc step by id: {}", id);
        EpcStepPO po = epcStepPOMapper.selectById(id);
        return Optional.ofNullable(epcStepConverter.toEntity(po));
    }

    @Override
    public List<EpcStep> findByOntologyId(String ontologyId) {
        log.debug("Finding epc steps by ontologyId: {}", ontologyId);
        List<EpcStepPO> poList = epcStepPOMapper.selectByOntologyId(ontologyId);
        return epcStepConverter.toEntityList(poList);
    }

    @Override
    public List<EpcStep> findByOntologyIdAndFlowName(String ontologyId, String flowName) {
        log.debug("Finding epc steps by ontologyId+flowName: {}, {}", ontologyId, flowName);
        List<EpcStepPO> poList = epcStepPOMapper.selectByOntologyIdAndFlowName(ontologyId, flowName);
        return epcStepConverter.toEntityList(poList);
    }

    @Override
    public List<EpcStep> findByFlowNameOrderByStepOrder(String flowName) {
        log.debug("Finding epc steps by flowName: {}", flowName);
        List<EpcStepPO> poList = epcStepPOMapper.selectByFlowNameOrderByStepOrder(flowName);
        return epcStepConverter.toEntityList(poList);
    }

    @Override
    public EpcStep save(EpcStep entity) {
        log.debug("Saving epc step: {}", entity.getId());
        EpcStepPO po = epcStepConverter.toPO(entity);
        if (po.getCreatedAt() == null) {
            po.setCreatedAt(Instant.now());
        }
        po.setUpdatedAt(Instant.now());
        if (epcStepPOMapper.selectById(entity.getId()) != null) {
            epcStepPOMapper.updateById(po);
        } else {
            epcStepPOMapper.insert(po);
        }
        return entity;
    }

    @Override
    public void deleteById(String id) {
        log.debug("Deleting epc step: {}", id);
        epcStepPOMapper.deleteById(id);
    }
}
