package com.ontology.platform.application.service.impl;

import com.ontology.platform.application.dto.*;
import com.ontology.platform.application.service.RelationService;
import com.ontology.platform.common.exception.ResourceNotFoundException;
import com.ontology.platform.common.exception.ValidationException;
import com.ontology.platform.domain.entity.ObjectType;
import com.ontology.platform.domain.entity.Relation;
import com.ontology.platform.domain.repository.ObjectTypeRepository;
import com.ontology.platform.domain.repository.OntologyRepository;
import com.ontology.platform.domain.repository.RelationRepository;
import com.ontology.platform.domain.vo.RelationProperty;
import com.ontology.platform.infrastructure.service.AgeGraphService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 关系服务实现
 * Relation Service Implementation
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RelationServiceImpl implements RelationService {

    private final RelationRepository relationRepository;
    private final ObjectTypeRepository objectTypeRepository;
    private final OntologyRepository ontologyRepository;
    private final AgeGraphService ageGraphService;

    @Override
    @Transactional
    public RelationResponse createRelation(CreateRelationRequest request) {
        log.info("Creating relation: name={}, ontologyId={}, source={}, target={}",
                request.getName(), request.getOntologyId(),
                request.getSourceTypeId(), request.getTargetTypeId());

        // 验证本体存在
        ontologyRepository.findById(request.getOntologyId())
                .orElseThrow(() -> new ResourceNotFoundException("Ontology", request.getOntologyId()));

        // 验证源对象类型存在
        ObjectType sourceType = objectTypeRepository.findById(request.getSourceTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("ObjectType (source)", request.getSourceTypeId()));

        // 验证目标对象类型存在
        ObjectType targetType = objectTypeRepository.findById(request.getTargetTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("ObjectType (target)", request.getTargetTypeId()));

        // 验证源对象类型属于指定本体
        if (!sourceType.getOntologyId().equals(request.getOntologyId())) {
            throw new ValidationException("源对象类型不属于指定本体",
                    "sourceTypeId: " + request.getSourceTypeId());
        }

        // 验证目标对象类型属于指定本体
        if (!targetType.getOntologyId().equals(request.getOntologyId())) {
            throw new ValidationException("目标对象类型不属于指定本体",
                    "targetTypeId: " + request.getTargetTypeId());
        }

        // 验证关系名称唯一性
        if (relationRepository.existsByOntologyIdAndName(request.getOntologyId(), request.getName())) {
            throw new ValidationException("关系名称已存在", "name: " + request.getName());
        }

        // 创建关系
        Relation relation = Relation.create(
                request.getOntologyId(),
                request.getSourceTypeId(),
                request.getTargetTypeId(),
                request.getName(),
                request.getDisplayName(),
                request.getDescription(),
                request.getCardinality()
        );

        // 设置反向关系
        if (request.getReverseName() != null) {
            relation.setReverse(request.getReverseName(), request.getReverseDisplayName());
        }

        // 添加关系属性
        if (request.getProperties() != null) {
            for (RelationPropertyDTO propDto : request.getProperties()) {
                RelationProperty property = RelationProperty.create(
                        propDto.getName(),
                        propDto.getDisplayName(),
                        propDto.getDataType(),
                        propDto.isRequired()
                );
                property.setDefaultValue(propDto.getDefaultValue());
                relation.addProperty(property);
            }
        }

        // 保存关系到关系数据库
        relation = relationRepository.save(relation);

        // 同步创建图数据库中的边
        ageGraphService.createEdge(relation);

        log.info("Relation created: id={}", relation.getId());
        return toRelationResponse(relation);
    }

    @Override
    public RelationResponse getRelationById(String id) {
        log.debug("Getting relation by id: {}", id);
        Relation relation = relationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Relation", id));
        return toRelationResponse(relation);
    }

    @Override
    public List<RelationResponse> listRelations(String ontologyId) {
        log.debug("Listing relations for ontology: {}", ontologyId);
        return relationRepository.findByOntologyId(ontologyId).stream()
                .map(this::toRelationResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RelationResponse updateRelation(String id, UpdateRelationRequest request) {
        log.info("Updating relation: id={}", id);
        Relation relation = relationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Relation", id));

        // 更新基本信息
        if (request.getDisplayName() != null || request.getDescription() != null) {
            relation.update(
                    request.getDisplayName() != null ? request.getDisplayName() : relation.getDisplayName(),
                    request.getDescription() != null ? request.getDescription() : relation.getDescription()
            );
        }

        // 更新反向关系
        if (request.getReverseName() != null) {
            relation.setReverse(
                    request.getReverseName(),
                    request.getReverseDisplayName() != null ? request.getReverseDisplayName() : relation.getReverseDisplayName()
            );
        }

        // 更新关系属性
        if (request.getProperties() != null) {
            // 清空现有属性
            List<String> existingPropertyNames = relation.getProperties().stream()
                    .map(p -> p.getName())
                    .collect(Collectors.toList());
            for (String name : existingPropertyNames) {
                relation.removeProperty(name);
            }
            // 添加新属性
            for (RelationPropertyDTO propDto : request.getProperties()) {
                RelationProperty property = RelationProperty.create(
                        propDto.getName(),
                        propDto.getDisplayName(),
                        propDto.getDataType(),
                        propDto.isRequired()
                );
                property.setDefaultValue(propDto.getDefaultValue());
                relation.addProperty(property);
            }
        }

        // 更新关系数据库
        relation = relationRepository.update(relation);

        // 同步更新图数据库中的边
        ageGraphService.updateEdge(relation);

        log.info("Relation updated: id={}", id);
        return toRelationResponse(relation);
    }

    @Override
    @Transactional
    public void deleteRelation(String id) {
        log.info("Deleting relation: id={}", id);
        Relation relation = relationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Relation", id));

        // 删除图数据库中的边
        ageGraphService.deleteEdge(id);

        // 删除关系数据库中的关系
        relationRepository.deleteById(id);

        log.info("Relation deleted: id={}", id);
    }

    @Override
    public List<RelationResponse> findBySourceTypeId(String sourceTypeId) {
        log.debug("Finding relations by source type: {}", sourceTypeId);
        return relationRepository.findBySourceTypeId(sourceTypeId).stream()
                .map(this::toRelationResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<RelationResponse> findByTargetTypeId(String targetTypeId) {
        log.debug("Finding relations by target type: {}", targetTypeId);
        return relationRepository.findByTargetTypeId(targetTypeId).stream()
                .map(this::toRelationResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ObjectTypeResponse> findRelatedObjectTypes(String relationId) {
        log.debug("Finding related object types for relation: {}", relationId);
        Relation relation = relationRepository.findById(relationId)
                .orElseThrow(() -> new ResourceNotFoundException("Relation", relationId));

        List<ObjectTypeResponse> result = new ArrayList<>();

        // 查询源对象类型
        objectTypeRepository.findById(relation.getSourceTypeId())
                .ifPresent(type -> result.add(toObjectTypeResponse(type)));

        // 查询目标对象类型
        objectTypeRepository.findById(relation.getTargetTypeId())
                .ifPresent(type -> result.add(toObjectTypeResponse(type)));

        return result;
    }

    /**
     * 转换为RelationResponse
     */
    private RelationResponse toRelationResponse(Relation relation) {
        List<RelationPropertyDTO> propertyDTOs = new ArrayList<>();
        if (relation.getProperties() != null) {
            for (RelationProperty prop : relation.getProperties()) {
                propertyDTOs.add(RelationPropertyDTO.builder()
                        .name(prop.getName())
                        .displayName(prop.getDisplayName())
                        .dataType(prop.getDataType())
                        .isRequired(prop.isRequired())
                        .defaultValue(prop.getDefaultValue())
                        .build());
            }
        }

        return RelationResponse.builder()
                .id(relation.getId())
                .ontologyId(relation.getOntologyId())
                .sourceTypeId(relation.getSourceTypeId())
                .targetTypeId(relation.getTargetTypeId())
                .name(relation.getName())
                .displayName(relation.getDisplayName())
                .description(relation.getDescription())
                .cardinality(relation.getCardinality())
                .reverseName(relation.getReverseName())
                .reverseDisplayName(relation.getReverseDisplayName())
                .properties(propertyDTOs)
                .createdAt(relation.getCreatedAt())
                .updatedAt(relation.getUpdatedAt())
                .build();
    }

    /**
     * 转换为ObjectTypeResponse
     */
    private ObjectTypeResponse toObjectTypeResponse(ObjectType objectType) {
        return ObjectTypeResponse.builder()
                .id(objectType.getId())
                .ontologyId(objectType.getOntologyId())
                .name(objectType.getName())
                .displayName(objectType.getDisplayName())
                .description(objectType.getDescription())
                .primaryKey(objectType.getPrimaryKey())
                .parentId(objectType.getParentId())
                .interfaceNames(objectType.getInterfaceNames())
                .createdAt(objectType.getCreatedAt())
                .updatedAt(objectType.getUpdatedAt())
                .build();
    }
}
