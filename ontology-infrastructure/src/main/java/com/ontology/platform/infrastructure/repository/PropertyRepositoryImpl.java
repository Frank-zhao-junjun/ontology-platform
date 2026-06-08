package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.domain.repository.PropertyRepository;
import com.ontology.platform.domain.vo.Property;
import com.ontology.platform.infrastructure.converter.PropertyConverter;
import com.ontology.platform.infrastructure.persistence.PropertyMapper;
import com.ontology.platform.infrastructure.persistence.PropertyPO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * 属性仓储实现
 * Property Repository Implementation
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class PropertyRepositoryImpl implements PropertyRepository {

    private final PropertyMapper propertyMapper;
    private final PropertyConverter propertyConverter;

    @Override
    public Optional<Property> findById(String id) {
        log.debug("Finding property by id: {}", id);
        
        PropertyPO po = propertyMapper.selectById(id);
        if (po == null) {
            return Optional.empty();
        }
        
        return Optional.of(propertyConverter.toVO(po));
    }

    @Override
    public List<Property> findByObjectTypeId(String objectTypeId) {
        log.debug("Finding properties by objectTypeId: {}", objectTypeId);
        
        List<PropertyPO> poList = propertyMapper.selectByObjectTypeId(objectTypeId);
        if (poList == null || poList.isEmpty()) {
            return List.of();
        }
        
        return propertyConverter.toVOList(poList);
    }

    @Override
    public Optional<Property> findByObjectTypeIdAndName(String objectTypeId, String name) {
        log.debug("Finding property by objectTypeId and name: {}, {}", objectTypeId, name);
        
        PropertyPO po = propertyMapper.selectByObjectTypeIdAndName(objectTypeId, name);
        if (po == null) {
            return Optional.empty();
        }
        
        return Optional.of(propertyConverter.toVO(po));
    }

    @Override
    public Property save(Property property) {
        log.debug("Saving property: {}", property.getId());
        
        if (property.getCreatedAt() == null) {
            property.setCreatedAt(Instant.now());
        }
        if (property.getUpdatedAt() == null) {
            property.setUpdatedAt(Instant.now());
        }
        
        PropertyPO po = propertyConverter.toPO(property);
        propertyMapper.insert(po);
        
        return property;
    }

    @Override
    public Property update(Property property) {
        log.debug("Updating property: {}", property.getId());
        
        property.setUpdatedAt(Instant.now());
        
        PropertyPO po = propertyConverter.toPO(property);
        propertyMapper.updateById(po);
        
        return property;
    }

    @Override
    public void deleteById(String id) {
        log.debug("Deleting property by id: {}", id);
        propertyMapper.deleteById(id);
    }

    @Override
    public boolean existsByObjectTypeIdAndName(String objectTypeId, String name) {
        log.debug("Checking if property exists by objectTypeId and name: {}, {}", objectTypeId, name);
        
        int count = propertyMapper.countByObjectTypeIdAndName(objectTypeId, name);
        return count > 0;
    }

    @Override
    public boolean existsByObjectTypeIdAndNameAndIdNot(String objectTypeId, String name, String excludeId) {
        log.debug("Checking if property exists by objectTypeId and name excluding id: {}, {}, {}", objectTypeId, name, excludeId);
        
        int count = propertyMapper.countByObjectTypeIdAndNameExcludingId(objectTypeId, name, excludeId);
        return count > 0;
    }

    @Override
    public long countByObjectTypeId(String objectTypeId) {
        log.debug("Counting properties by objectTypeId: {}", objectTypeId);
        
        return propertyMapper.countByObjectTypeId(objectTypeId);
    }

    @Override
    public List<Property> saveAll(List<Property> properties) {
        log.debug("Saving all properties, count: {}", properties.size());
        
        for (Property property : properties) {
            save(property);
        }
        
        return properties;
    }

    @Override
    public void deleteAllByObjectTypeId(String objectTypeId) {
        log.debug("Deleting all properties by objectTypeId: {}", objectTypeId);
        propertyMapper.deleteAllByObjectTypeId(objectTypeId);
    }
}
