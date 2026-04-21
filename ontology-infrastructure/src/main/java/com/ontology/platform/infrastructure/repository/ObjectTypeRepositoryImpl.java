package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.domain.entity.ObjectType;
import com.ontology.platform.domain.repository.ObjectTypeRepository;
import com.ontology.platform.domain.repository.PropertyRepository;
import com.ontology.platform.domain.vo.Property;
import com.ontology.platform.infrastructure.converter.ObjectTypeConverter;
import com.ontology.platform.infrastructure.persistence.ObjectTypeMapper;
import com.ontology.platform.infrastructure.persistence.ObjectTypePO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * 对象类型仓储实现
 * ObjectType Repository Implementation
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ObjectTypeRepositoryImpl implements ObjectTypeRepository {

    private final ObjectTypeMapper objectTypeMapper;
    private final PropertyRepository propertyRepository;
    private final ObjectTypeConverter objectTypeConverter;

    @Override
    public Optional<ObjectType> findById(String id) {
        log.debug("Finding object type by id: {}", id);
        
        ObjectTypePO po = objectTypeMapper.selectById(id);
        if (po == null) {
            return Optional.empty();
        }
        
        List<Property> properties = propertyRepository.findByObjectTypeId(id);
        return Optional.of(objectTypeConverter.toEntity(po, properties));
    }

    @Override
    public List<ObjectType> findByOntologyId(String ontologyId) {
        log.debug("Finding object types by ontologyId: {}", ontologyId);
        
        List<ObjectTypePO> poList = objectTypeMapper.selectByOntologyId(ontologyId);
        if (poList == null || poList.isEmpty()) {
            return List.of();
        }
        
        return objectTypeConverter.toEntityList(poList);
    }

    @Override
    public Optional<ObjectType> findByOntologyIdAndName(String ontologyId, String name) {
        log.debug("Finding object type by ontologyId and name: {}, {}", ontologyId, name);
        
        ObjectTypePO po = objectTypeMapper.selectByOntologyIdAndName(ontologyId, name);
        if (po == null) {
            return Optional.empty();
        }
        
        List<Property> properties = propertyRepository.findByObjectTypeId(po.getId());
        return Optional.of(objectTypeConverter.toEntity(po, properties));
    }

    @Override
    public List<ObjectType> findByParentId(String parentId) {
        log.debug("Finding object types by parentId: {}", parentId);
        
        List<ObjectTypePO> poList = objectTypeMapper.selectByParentId(parentId);
        if (poList == null || poList.isEmpty()) {
            return List.of();
        }
        
        return objectTypeConverter.toEntityList(poList);
    }

    @Override
    public ObjectType save(ObjectType objectType) {
        log.debug("Saving object type: {}", objectType.getId());
        
        if (objectType.getCreatedAt() == null) {
            objectType.setCreatedAt(Instant.now());
        }
        if (objectType.getUpdatedAt() == null) {
            objectType.setUpdatedAt(Instant.now());
        }
        
        ObjectTypePO po = objectTypeConverter.toPO(objectType);
        objectTypeMapper.insert(po);
        
        if (objectType.getProperties() != null && !objectType.getProperties().isEmpty()) {
            for (Property property : objectType.getProperties()) {
                property.setObjectTypeId(objectType.getId());
                propertyRepository.save(property);
            }
        }
        
        return objectType;
    }

    @Override
    public ObjectType update(ObjectType objectType) {
        log.debug("Updating object type: {}", objectType.getId());
        
        objectType.setUpdatedAt(Instant.now());
        
        ObjectTypePO po = objectTypeConverter.toPO(objectType);
        objectTypeMapper.updateById(po);
        
        return objectType;
    }

    @Override
    public void deleteById(String id) {
        log.debug("Deleting object type by id: {}", id);
        
        propertyRepository.deleteAllByObjectTypeId(id);
        objectTypeMapper.deleteById(id);
    }

    @Override
    public boolean existsByOntologyIdAndName(String ontologyId, String name) {
        log.debug("Checking if object type exists by ontologyId and name: {}, {}", ontologyId, name);
        
        int count = objectTypeMapper.countByOntologyIdAndName(ontologyId, name);
        return count > 0;
    }

    @Override
    public boolean existsByOntologyIdAndNameAndIdNot(String ontologyId, String name, String excludeId) {
        log.debug("Checking if object type exists by ontologyId and name excluding id: {}, {}, {}", ontologyId, name, excludeId);
        
        int count = objectTypeMapper.countByOntologyIdAndNameExcludingId(ontologyId, name, excludeId);
        return count > 0;
    }

    @Override
    public long countByOntologyId(String ontologyId) {
        log.debug("Counting object types by ontologyId: {}", ontologyId);
        
        return objectTypeMapper.countByOntologyId(ontologyId);
    }
}
