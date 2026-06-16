package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.domain.entity.ReportDefinition;
import com.ontology.platform.domain.repository.ReportDefinitionRepository;
import com.ontology.platform.infrastructure.converter.ReportDefinitionConverter;
import com.ontology.platform.infrastructure.persistence.ReportDefinitionPO;
import com.ontology.platform.infrastructure.persistence.ReportDefinitionPOMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ReportDefinitionRepositoryImpl implements ReportDefinitionRepository {

    private final ReportDefinitionPOMapper reportDefinitionPOMapper;
    private final ReportDefinitionConverter reportDefinitionConverter;

    @Override
    public Optional<ReportDefinition> findById(String id) {
        log.debug("Finding report definition by id: {}", id);
        ReportDefinitionPO po = reportDefinitionPOMapper.selectById(id);
        return Optional.ofNullable(reportDefinitionConverter.toEntity(po));
    }

    @Override
    public List<ReportDefinition> findByOntologyId(String ontologyId) {
        log.debug("Finding report definitions by ontologyId: {}", ontologyId);
        List<ReportDefinitionPO> poList = reportDefinitionPOMapper.selectByOntologyId(ontologyId);
        return reportDefinitionConverter.toEntityList(poList);
    }

    @Override
    public ReportDefinition save(ReportDefinition entity) {
        log.debug("Saving report definition: {}", entity.getId());
        ReportDefinitionPO po = reportDefinitionConverter.toPO(entity);
        if (po.getCreatedAt() == null) {
            po.setCreatedAt(Instant.now());
        }
        po.setUpdatedAt(Instant.now());
        if (reportDefinitionPOMapper.selectById(entity.getId()) != null) {
            reportDefinitionPOMapper.updateById(po);
        } else {
            reportDefinitionPOMapper.insert(po);
        }
        return entity;
    }

    @Override
    public void deleteById(String id) {
        log.debug("Soft-deleting report definition: {}", id);
        ReportDefinitionPO po = reportDefinitionPOMapper.selectById(id);
        if (po != null) {
            po.setDeleted(true);
            po.setUpdatedAt(Instant.now());
            reportDefinitionPOMapper.updateById(po);
        }
    }
}
