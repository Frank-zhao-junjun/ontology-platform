package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.domain.entity.ObjectInstance;
import com.ontology.platform.domain.repository.ObjectInstanceRepository;
import com.ontology.platform.infrastructure.converter.ObjectInstanceConverter;
import com.ontology.platform.infrastructure.persistence.ObjectInstancePO;
import com.ontology.platform.infrastructure.persistence.ObjectInstancePOMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * 对象实例仓储实现
 * Object Instance Repository Implementation
 * 基于 MyBatis-Plus + PostgreSQL object_instance 表
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ObjectInstanceRepositoryImpl implements ObjectInstanceRepository {

    private final ObjectInstancePOMapper objectInstancePOMapper;
    private final ObjectInstanceConverter objectInstanceConverter;

    @Override
    public Optional<ObjectInstance> findById(String id) {
        log.debug("Finding object instance by id: {}", id);
        ObjectInstancePO po = objectInstancePOMapper.selectById(id);
        return Optional.ofNullable(objectInstanceConverter.toEntity(po));
    }

    @Override
    public List<ObjectInstance> findByObjectTypeId(String objectTypeId, int offset, int limit) {
        log.debug("Finding object instances by objectTypeId: {}, offset={}, limit={}", objectTypeId, offset, limit);
        List<ObjectInstancePO> poList = objectInstancePOMapper.selectByObjectTypeId(objectTypeId, offset, limit);
        return objectInstanceConverter.toEntityList(poList);
    }

    @Override
    public long countByObjectTypeId(String objectTypeId) {
        log.debug("Counting object instances by objectTypeId: {}", objectTypeId);
        return objectInstancePOMapper.countByObjectTypeId(objectTypeId);
    }

    @Override
    public ObjectInstance save(ObjectInstance instance) {
        log.debug("Saving object instance: {}", instance.getId());
        if (instance.getCreatedAt() == null) {
            instance.setCreatedAt(Instant.now());
        }
        instance.setUpdatedAt(Instant.now());

        ObjectInstancePO po = objectInstanceConverter.toPO(instance);
        objectInstancePOMapper.insert(po);
        return instance;
    }

    @Override
    public void deleteById(String id) {
        log.debug("Deleting object instance by id: {}", id);
        objectInstancePOMapper.deleteById(id);
    }

    @Override
    public boolean existsByObjectTypeIdAndPrimaryKeyValue(String objectTypeId, String pkValue) {
        log.debug("Checking object instance exists by objectTypeId and primaryKeyValue: {}, {}",
                objectTypeId, pkValue);
        ObjectInstancePO po = objectInstancePOMapper.selectByObjectTypeIdAndPrimaryKeyValue(objectTypeId, pkValue);
        return po != null;
    }
}
