package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.common.enums.OntologyStatus;
import com.ontology.platform.domain.entity.Ontology;
import com.ontology.platform.domain.repository.OntologyRepository;
import com.ontology.platform.infrastructure.converter.OntologyConverter;
import com.ontology.platform.infrastructure.persistence.OntologyPO;
import com.ontology.platform.infrastructure.persistence.OntologyPOMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 本体仓储实现
 * Ontology Repository Implementation
 * 
 * 负责本体聚合根的持久化操作，将领域对象与数据库记录进行转换。
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class OntologyRepositoryImpl implements OntologyRepository {

    private final OntologyPOMapper ontologyPOMapper;
    private final OntologyConverter ontologyConverter;

    @Override
    public Optional<Ontology> findById(String id) {
        log.debug("Finding ontology by id: {}", id);
        OntologyPO po = ontologyPOMapper.selectById(id);
        return Optional.ofNullable(ontologyConverter.toEntity(po));
    }

    @Override
    public Optional<Ontology> findByTenantIdAndName(String tenantId, String name) {
        log.debug("Finding ontology by tenantId and name: {}, {}", tenantId, name);
        OntologyPO po = ontologyPOMapper.selectByTenantIdAndName(tenantId, name);
        return Optional.ofNullable(ontologyConverter.toEntity(po));
    }

    @Override
    public List<Ontology> findByTenantId(String tenantId) {
        log.debug("Finding ontologies by tenantId: {}", tenantId);
        List<OntologyPO> poList = ontologyPOMapper.selectByTenantIdWithPage(tenantId, 0, Integer.MAX_VALUE);
        return ontologyConverter.toEntityList(poList);
    }

    @Override
    public List<Ontology> findByTenantIdAndStatus(String tenantId, OntologyStatus status) {
        log.debug("Finding ontologies by tenantId and status: {}, {}", tenantId, status);
        List<OntologyPO> poList = ontologyPOMapper.selectByTenantIdAndStatus(tenantId, status.getValue());
        return ontologyConverter.toEntityList(poList);
    }

    @Override
    public Ontology save(Ontology ontology) {
        log.debug("Saving ontology: {}", ontology.getId());
        OntologyPO po = ontologyConverter.toPO(ontology);
        ontologyPOMapper.insert(po);
        log.info("Ontology saved successfully: id={}", ontology.getId());
        return ontology;
    }

    @Override
    public Ontology update(Ontology ontology) {
        log.debug("Updating ontology: {}", ontology.getId());
        OntologyPO po = ontologyConverter.toPO(ontology);
        ontologyPOMapper.updateById(po);
        log.info("Ontology updated successfully: id={}", ontology.getId());
        return ontology;
    }

    @Override
    public void deleteById(String id) {
        log.debug("Deleting ontology by id: {}", id);
        ontologyPOMapper.deleteById(id);
        log.info("Ontology deleted successfully: id={}", id);
    }

    @Override
    public boolean existsByTenantIdAndName(String tenantId, String name) {
        log.debug("Checking if ontology exists by tenantId and name: {}, {}", tenantId, name);
        return ontologyPOMapper.countByTenantIdAndName(tenantId, name) > 0;
    }

    @Override
    public boolean existsByTenantIdAndNameAndIdNot(String tenantId, String name, String excludeId) {
        log.debug("Checking if ontology exists by tenantId and name excluding id: {}, {}, {}", tenantId, name, excludeId);
        return ontologyPOMapper.countByTenantIdAndNameExcludingId(tenantId, name, excludeId) > 0;
    }

    @Override
    public long countByTenantId(String tenantId) {
        log.debug("Counting ontologies by tenantId: {}", tenantId);
        return ontologyPOMapper.countByTenantId(tenantId);
    }

    @Override
    public List<Ontology> findByTenantIdWithPage(String tenantId, int page, int pageSize) {
        log.debug("Finding ontologies by tenantId with pagination: {}, {}, {}", tenantId, page, pageSize);
        int offset = (page - 1) * pageSize;
        List<OntologyPO> poList = ontologyPOMapper.selectByTenantIdWithPage(tenantId, offset, pageSize);
        return ontologyConverter.toEntityList(poList);
    }
}
