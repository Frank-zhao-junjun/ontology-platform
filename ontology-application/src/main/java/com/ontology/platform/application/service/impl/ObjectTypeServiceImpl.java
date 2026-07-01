package com.ontology.platform.application.service.impl;

import com.ontology.platform.application.dto.*;
import com.ontology.platform.application.service.ObjectTypeService;
import com.ontology.platform.common.exception.ResourceNotFoundException;
import com.ontology.platform.common.exception.ValidationException;
import com.ontology.platform.domain.entity.ObjectType;
import com.ontology.platform.domain.repository.ObjectTypeRepository;
import com.ontology.platform.domain.repository.OntologyRepository;
import com.ontology.platform.domain.repository.PropertyRepository;
import com.ontology.platform.domain.vo.Property;
import com.ontology.platform.domain.vo.PropertyConstraint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

/**
 * 对象类型服务实现
 * ObjectType Service Implementation
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ObjectTypeServiceImpl implements ObjectTypeService {

    private final ObjectTypeRepository objectTypeRepository;
    private final PropertyRepository propertyRepository;
    private final OntologyRepository ontologyRepository;

    @Override
    @Transactional
    public ObjectTypeResponse createObjectType(CreateObjectTypeRequest request) {
        log.info("Creating object type: name={}, ontologyId={}", request.getName(), request.getOntologyId());

        ontologyRepository.findById(request.getOntologyId())
                .orElseThrow(() -> new ResourceNotFoundException("Ontology", request.getOntologyId()));

        if (objectTypeRepository.existsByOntologyIdAndName(request.getOntologyId(), request.getName())) {
            throw new ValidationException("对象类型名称已存在", "name: " + request.getName());
        }

        ObjectType objectType = ObjectType.create(
                request.getOntologyId(),
                request.getName(),
                request.getDisplayName(),
                request.getDescription(),
                request.getPrimaryKey(),
                request.getEntityRole(),
                request.getBusinessScenarioId()
        );
        // 循环继承检测：验证 parentId 不会形成 A→B→...→A 环
        if (request.getParentId() != null) {
            checkParentCycle(null, request.getParentId());
        }
        objectType.setParentId(request.getParentId());
        objectType.setParentAggregateId(request.getParentAggregateId());
        objectType.setSubDomain(request.getSubDomain());
        objectType.setInterfaceNames(request.getInterfaceNames() != null ? request.getInterfaceNames() : new ArrayList<>());

        objectType = objectTypeRepository.save(objectType);
        log.info("ObjectType created: id={}", objectType.getId());

        return toObjectTypeResponse(objectType);
    }

    @Override
    public ObjectTypeDetailResponse getObjectTypeById(String id) {
        log.debug("Getting object type by id: {}", id);
        ObjectType objectType = objectTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ObjectType", id));

        return toObjectTypeDetailResponse(objectType);
    }

    @Override
    public List<ObjectTypeResponse> listObjectTypes(String ontologyId) {
        log.debug("Listing object types: ontologyId={}", ontologyId);
        return objectTypeRepository.findByOntologyId(ontologyId)
                .stream()
                .map(this::toObjectTypeResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ObjectTypeResponse updateObjectType(String id, UpdateObjectTypeRequest request) {
        log.info("Updating object type: id={}", id);
        ObjectType objectType = objectTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ObjectType", id));

        objectType.update(
                request.getDisplayName(),
                request.getDescription(),
                request.getPrimaryKey()
        );

        if (request.getInterfaceNames() != null) {
            objectType.setInterfaceNames(request.getInterfaceNames());
        }

        if (request.getEntityRole() != null) {
            objectType.setEntityRole(request.getEntityRole());
        }
        if (request.getParentAggregateId() != null) {
            objectType.setParentAggregateId(request.getParentAggregateId());
        }
        if (request.getBusinessScenarioId() != null) {
            objectType.setBusinessScenarioId(request.getBusinessScenarioId());
        }
        if (request.getSubDomain() != null) {
            objectType.setSubDomain(request.getSubDomain());
        }
        if (request.getParentId() != null) {
            if (request.getParentId().equals(id)) {
                throw new ValidationException("不能将自身设为父类型", "parentId: " + request.getParentId());
            }
            checkParentCycle(id, request.getParentId());
            objectType.setParentId(request.getParentId());
        }
        if (request.getAttributesJsonb() != null) {
            objectType.setAttributesJsonb(request.getAttributesJsonb());
        }

        objectType = objectTypeRepository.update(objectType);
        log.info("ObjectType updated: id={}", id);

        return toObjectTypeResponse(objectType);
    }

    @Override
    @Transactional
    public void deleteObjectType(String id) {
        log.info("Deleting object type: id={}", id);
        objectTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ObjectType", id));
        objectTypeRepository.deleteById(id);
        log.info("ObjectType deleted: id={}", id);
    }

    @Override
    @Transactional
    public PropertyResponse createProperty(CreatePropertyRequest request) {
        log.info("Creating property: name={}, objectTypeId={}", request.getName(), request.getObjectTypeId());

        objectTypeRepository.findById(request.getObjectTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("ObjectType", request.getObjectTypeId()));

        if (propertyRepository.existsByObjectTypeIdAndName(request.getObjectTypeId(), request.getName())) {
            throw new ValidationException("属性名称已存在", "name: " + request.getName());
        }

        Property property = Property.create(
                request.getObjectTypeId(),
                request.getName(),
                request.getDisplayName(),
                request.getDescription(),
                request.getDataType(),
                request.isRequired()
        );

        property.setUnique(request.isUnique());
        property.setSearchable(request.isSearchable());
        property.setSortable(request.isSortable());
        property.setComputed(request.isComputed());
        property.setDefaultValue(request.getDefaultValue());
        property.setSortOrder(request.getSortOrder());

        if (request.getConstraints() != null && !request.getConstraints().isEmpty()) {
            for (ConstraintDefinition constraintDef : request.getConstraints()) {
                validateConstraintDefinition(constraintDef);
                PropertyConstraint constraint = createConstraint(constraintDef);
                property = property.addConstraint(constraint);
            }
        }

        property = propertyRepository.save(property);
        log.info("Property created: id={}", property.getId());

        return toPropertyResponse(property);
    }

    @Override
    @Transactional
    public PropertyResponse updateProperty(String id, UpdatePropertyRequest request) {
        log.info("Updating property: id={}", id);

        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property", id));

        property = property.update(
                request.getDisplayName() != null ? request.getDisplayName() : property.getDisplayName(),
                request.getDescription() != null ? request.getDescription() : property.getDescription(),
                request.getIsRequired() != null ? request.getIsRequired() : property.isRequired()
        );

        if (request.getDataType() != null) {
            property.setDataType(request.getDataType());
        }
        if (request.getIsUnique() != null) {
            property.setUnique(request.getIsUnique());
        }
        if (request.getIsSearchable() != null) {
            property.setSearchable(request.getIsSearchable());
        }
        if (request.getIsSortable() != null) {
            property.setSortable(request.getIsSortable());
        }
        if (request.getDefaultValue() != null) {
            property.setDefaultValue(request.getDefaultValue());
        }
        if (request.getSortOrder() != null) {
            property = property.updateSortOrder(request.getSortOrder());
        }

        property = propertyRepository.update(property);
        log.info("Property updated: id={}", id);

        return toPropertyResponse(property);
    }

    @Override
    @Transactional
    public void deleteProperty(String id) {
        log.info("Deleting property: id={}", id);
        propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property", id));
        propertyRepository.deleteById(id);
        log.info("Property deleted: id={}", id);
    }

    @Override
    public List<PropertyResponse> listProperties(String objectTypeId) {
        log.debug("Listing properties: objectTypeId={}", objectTypeId);
        return propertyRepository.findByObjectTypeId(objectTypeId)
                .stream()
                .map(this::toPropertyResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<PropertyResponse> batchCreateProperties(String objectTypeId, List<CreatePropertyRequest> requests) {
        log.info("Batch creating properties: objectTypeId={}, count={}", objectTypeId, requests.size());

        objectTypeRepository.findById(objectTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("ObjectType", objectTypeId));

        List<PropertyResponse> responses = new ArrayList<>();
        for (CreatePropertyRequest request : requests) {
            request.setObjectTypeId(objectTypeId);
            PropertyResponse response = createProperty(request);
            responses.add(response);
        }

        return responses;
    }

    /**
     * 循环继承检测：遍历 parentId 的祖先链，检查是否会出现 A→B→...→A 环。
     * <p>
     * 对于新建对象 (currentId == null)，只需检查 parentId 是否合法（自引用在实体层已校验）。
     * 对于已有对象，需确保祖先链中不包含当前对象自身。
     *
     * @param currentId 当前正在设置父类型的对象 ID（新建时为 null）
     * @param parentId  拟设置的父类型 ID
     * @throws ValidationException 如果检测到循环继承链
     */
    private void checkParentCycle(String currentId, String parentId) {
        java.util.Set<String> visited = new java.util.HashSet<>();
        String cursor = parentId;
        int maxDepth = 50; // 最大遍历深度，防止意外无限循环

        while (cursor != null && maxDepth-- > 0) {
            if (cursor.equals(currentId)) {
                throw new ValidationException(
                    "检测到循环继承链",
                    "currentId: " + currentId + ", parentId: " + parentId
                );
            }
            if (!visited.add(cursor)) {
                // 祖先链中已存在环（数据异常），中断检测
                log.warn("Parent chain already contains a cycle at id={}", cursor);
                throw new ValidationException("继承链数据异常，存在已有循环", "id: " + cursor);
            }
            final String nextCursor = cursor;
            cursor = objectTypeRepository.findById(cursor)
                .map(ObjectType::getParentId)
                .orElse(null);
        }
    }

    private void validateConstraintDefinition(ConstraintDefinition constraintDef) {
        String type = constraintDef.getType().toUpperCase();
        Object value = constraintDef.getValue();

        switch (type) {
            case "MIN_VALUE", "MAX_VALUE" -> {
                if (!(value instanceof Number || value instanceof String)) {
                    throw new ValidationException("约束类型 " + type + " 的值必须是数字");
                }
            }
            case "MIN_LENGTH", "MAX_LENGTH" -> {
                if (!(value instanceof Number || (value instanceof String && ((String) value).matches("\\d+")))) {
                    throw new ValidationException("约束类型 " + type + " 的值必须是整数");
                }
            }
            case "PATTERN" -> {
                if (!(value instanceof String)) {
                    throw new ValidationException("约束类型 PATTERN 的值必须是正则表达式字符串");
                }
                try {
                    java.util.regex.Pattern.compile((String) value);
                } catch (PatternSyntaxException e) {
                    throw new ValidationException("无效的正则表达式: " + value);
                }
            }
            case "ENUM_VALUES" -> {
                if (!(value instanceof List)) {
                    throw new ValidationException("约束类型 ENUM_VALUES 的值必须是数组");
                }
            }
            case "CUSTOM" -> {
                if (!(value instanceof String)) {
                    throw new ValidationException("约束类型 CUSTOM 的值必须是字符串");
                }
            }
            default -> throw new ValidationException("不支持的约束类型: " + type);
        }
    }

    private PropertyConstraint createConstraint(ConstraintDefinition constraintDef) {
        String type = constraintDef.getType().toUpperCase();
        Object value = constraintDef.getValue();
        String errorMessage = constraintDef.getErrorMessage();

        return switch (type) {
            case "MIN_VALUE" -> PropertyConstraint.minValue(
                    value instanceof Number ? new BigDecimal(value.toString()) : new BigDecimal((String) value),
                    errorMessage
            );
            case "MAX_VALUE" -> PropertyConstraint.maxValue(
                    value instanceof Number ? new BigDecimal(value.toString()) : new BigDecimal((String) value),
                    errorMessage
            );
            case "MIN_LENGTH" -> PropertyConstraint.minLength(
                    value instanceof Number ? ((Number) value).intValue() : Integer.parseInt((String) value),
                    errorMessage
            );
            case "MAX_LENGTH" -> PropertyConstraint.maxLength(
                    value instanceof Number ? ((Number) value).intValue() : Integer.parseInt((String) value),
                    errorMessage
            );
            case "PATTERN" -> PropertyConstraint.pattern((String) value, errorMessage);
            case "ENUM_VALUES" -> PropertyConstraint.enumValues((java.util.List<String>) value, errorMessage);
            case "CUSTOM" -> PropertyConstraint.custom((String) value, errorMessage);
            default -> throw new ValidationException("不支持的约束类型: " + type);
        };
    }

    private ObjectTypeResponse toObjectTypeResponse(ObjectType objectType) {
        if (objectType == null) return null;
        return ObjectTypeResponse.builder()
                .id(objectType.getId())
                .ontologyId(objectType.getOntologyId())
                .name(objectType.getName())
                .displayName(objectType.getDisplayName())
                .description(objectType.getDescription())
                .primaryKey(objectType.getPrimaryKey())
                .parentId(objectType.getParentId())
                .entityRole(objectType.getEntityRole())
                .parentAggregateId(objectType.getParentAggregateId())
                .businessScenarioId(objectType.getBusinessScenarioId())
                .subDomain(objectType.getSubDomain())
                .attributesJsonb(objectType.getAttributesJsonb())
                .interfaceNames(objectType.getInterfaceNames())
                .instanceCount(objectType.getInstanceCount())
                .createdAt(objectType.getCreatedAt())
                .updatedAt(objectType.getUpdatedAt())
                .build();
    }

    private ObjectTypeDetailResponse toObjectTypeDetailResponse(ObjectType objectType) {
        if (objectType == null) return null;
        ObjectTypeDetailResponse response = new ObjectTypeDetailResponse();
        response.setId(objectType.getId());
        response.setOntologyId(objectType.getOntologyId());
        response.setName(objectType.getName());
        response.setDisplayName(objectType.getDisplayName());
        response.setDescription(objectType.getDescription());
        response.setPrimaryKey(objectType.getPrimaryKey());
        response.setParentId(objectType.getParentId());
        response.setEntityRole(objectType.getEntityRole());
        response.setParentAggregateId(objectType.getParentAggregateId());
        response.setBusinessScenarioId(objectType.getBusinessScenarioId());
        response.setSubDomain(objectType.getSubDomain());
        response.setAttributesJsonb(objectType.getAttributesJsonb());
        response.setInterfaceNames(objectType.getInterfaceNames());
        response.setInstanceCount(objectType.getInstanceCount());
        response.setCreatedAt(objectType.getCreatedAt());
        response.setUpdatedAt(objectType.getUpdatedAt());
        response.setProperties(objectType.getProperties() != null
                ? objectType.getProperties().stream().map(this::toPropertyResponse).collect(Collectors.toList())
                : List.of());
        return response;
    }

    private PropertyResponse toPropertyResponse(Property property) {
        if (property == null) return null;
        return PropertyResponse.builder()
                .id(property.getId())
                .objectTypeId(property.getObjectTypeId())
                .name(property.getName())
                .displayName(property.getDisplayName())
                .description(property.getDescription())
                .dataType(property.getDataType())
                .isComputed(property.isComputed())
                .isRequired(property.isRequired())
                .isUnique(property.isUnique())
                .isSearchable(property.isSearchable())
                .isSortable(property.isSortable())
                .defaultValue(property.getDefaultValue())
                .sortOrder(property.getSortOrder())
                .createdAt(property.getCreatedAt())
                .updatedAt(property.getUpdatedAt())
                .build();
    }
}
