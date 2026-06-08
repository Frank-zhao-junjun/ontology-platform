package com.ontology.platform.application.service.impl;

import com.ontology.platform.application.dto.*;
import com.ontology.platform.application.service.OntologyService;
import com.ontology.platform.common.exception.BusinessException;
import com.ontology.platform.common.exception.ResourceNotFoundException;
import com.ontology.platform.common.exception.ValidationException;
import com.ontology.platform.common.enums.ErrorCode;
import com.ontology.platform.domain.entity.ObjectType;
import com.ontology.platform.domain.entity.Ontology;
import com.ontology.platform.domain.entity.Relation;
import com.ontology.platform.domain.repository.ObjectTypeRepository;
import com.ontology.platform.domain.repository.OntologyRepository;
import com.ontology.platform.domain.vo.Property;
import com.ontology.platform.domain.vo.RelationProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 本体服务实现
 * Ontology Service Implementation
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OntologyServiceImpl implements OntologyService {

    private final OntologyRepository ontologyRepository;
    private final ObjectTypeRepository objectTypeRepository;

    // ==================== 本体管理 ====================

    @Override
    @Transactional
    public OntologyResponse createOntology(CreateOntologyRequest request, String userId) {
        log.info("Creating ontology: name={}, displayName={}", request.getName(), request.getDisplayName());

        // 检查名称唯一性
        if (ontologyRepository.existsByTenantIdAndName("default", request.getName())) {
            throw new ValidationException("本体名称已存在", "name: " + request.getName());
        }

        // 创建本体
        Ontology ontology = Ontology.create(
                request.getName(),
                request.getDisplayName(),
                request.getDescription(),
                userId
        );

        ontology = ontologyRepository.save(ontology);
        log.info("Ontology created: id={}", ontology.getId());

        return toOntologyResponse(ontology);
    }

    @Override
    public OntologyDetailResponse getOntologyById(String id) {
        log.debug("Getting ontology by id: {}", id);
        Ontology ontology = ontologyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ontology", id));

        return toOntologyDetailResponse(ontology);
    }

    @Override
    public List<OntologyResponse> listOntologies(String tenantId, int page, int pageSize) {
        log.debug("Listing ontologies: tenantId={}, page={}, pageSize={}", tenantId, page, pageSize);
        return ontologyRepository.findByTenantIdWithPage(tenantId, page, pageSize)
                .stream()
                .map(this::toOntologyResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OntologyResponse updateOntology(String id, UpdateOntologyRequest request) {
        log.info("Updating ontology: id={}", id);
        Ontology ontology = ontologyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ontology", id));

        // 检查名称唯一性
        if (request.getDisplayName() != null && ontologyRepository.existsByTenantIdAndNameAndIdNot(
                ontology.getTenantId(), ontology.getName(), id)) {
            throw new ValidationException("本体名称已存在");
        }

        ontology.update(request.getDisplayName(), request.getDescription());
        ontology = ontologyRepository.update(ontology);
        log.info("Ontology updated: id={}", id);

        return toOntologyResponse(ontology);
    }

    @Override
    @Transactional
    public void deleteOntology(String id) {
        log.info("Deleting ontology: id={}", id);
        Ontology ontology = ontologyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ontology", id));
        ontologyRepository.deleteById(id);
        log.info("Ontology deleted: id={}", id);
    }

    @Override
    @Transactional
    public OntologyResponse publishOntology(String id) {
        log.info("Publishing ontology: id={}", id);
        Ontology ontology = ontologyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ontology", id));

        ontology.publish();
        ontology = ontologyRepository.update(ontology);
        log.info("Ontology published: id={}", id);

        return toOntologyResponse(ontology);
    }

    @Override
    @Transactional
    public OntologyResponse archiveOntology(String id) {
        log.info("Archiving ontology: id={}", id);
        Ontology ontology = ontologyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ontology", id));

        ontology.archive();
        ontology = ontologyRepository.update(ontology);
        log.info("Ontology archived: id={}", id);

        return toOntologyResponse(ontology);
    }

    @Override
    public ValidationResultResponse validateOntology(String id) {
        log.info("Validating ontology: id={}", id);
        Ontology ontology = ontologyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ontology", id));

        // TODO: 实现完整的本体验证逻辑
        ValidationResultResponse.ValidationSummary summary = ValidationResultResponse.ValidationSummary.builder()
                .errors(0)
                .warnings(0)
                .passed(1)
                .build();

        return ValidationResultResponse.builder()
                .valid(true)
                .summary(summary)
                .issues(List.of())
                .build();
    }

    // ==================== 对象类型管理 ====================

    @Override
    @Transactional
    public ObjectTypeResponse createObjectType(CreateObjectTypeRequest request) {
        log.info("Creating object type: name={}, ontologyId={}", request.getName(), request.getOntologyId());

        // 检查本体是否存在
        ontologyRepository.findById(request.getOntologyId())
                .orElseThrow(() -> new ResourceNotFoundException("Ontology", request.getOntologyId()));

        // 检查名称唯一性
        if (objectTypeRepository.existsByOntologyIdAndName(request.getOntologyId(), request.getName())) {
            throw new ValidationException("对象类型名称已存在", "name: " + request.getName());
        }

        ObjectType objectType = ObjectType.create(
                request.getOntologyId(),
                request.getName(),
                request.getDisplayName(),
                request.getDescription(),
                request.getPrimaryKey()
        );
        objectType.setParentId(request.getParentId());
        objectType.setInterfaceNames(request.getInterfaceNames());

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

        objectType = objectTypeRepository.update(objectType);
        log.info("ObjectType updated: id={}", id);

        return toObjectTypeResponse(objectType);
    }

    @Override
    @Transactional
    public void deleteObjectType(String id) {
        log.info("Deleting object type: id={}", id);
        ObjectType objectType = objectTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ObjectType", id));
        objectTypeRepository.deleteById(id);
        log.info("ObjectType deleted: id={}", id);
    }

    // ==================== 属性管理 ====================

    @Override
    @Transactional
    public PropertyResponse createProperty(CreatePropertyRequest request) {
        log.info("Creating property: name={}, objectTypeId={}", request.getName(), request.getObjectTypeId());

        ObjectType objectType = objectTypeRepository.findById(request.getObjectTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("ObjectType", request.getObjectTypeId()));

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

        objectType.addProperty(property);
        objectTypeRepository.update(objectType);

        log.info("Property created: id={}", property.getId());
        return toPropertyResponse(property);
    }

    @Override
    @Transactional
    public PropertyResponse updateProperty(String id, UpdatePropertyRequest request) {
        log.info("Updating property: id={}", id);

        // TODO: 实现属性更新逻辑
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED, "Property update not implemented");
    }

    @Override
    @Transactional
    public void deleteProperty(String id) {
        log.info("Deleting property: id={}", id);
        // TODO: 实现属性删除逻辑
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED, "Property delete not implemented");
    }

    // ==================== 关系管理 ====================

    @Override
    @Transactional
    public RelationResponse createRelation(CreateRelationRequest request) {
        log.info("Creating relation: name={}, ontologyId={}", request.getName(), request.getOntologyId());

        // 验证源和目标类型存在
        objectTypeRepository.findById(request.getSourceTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("ObjectType", request.getSourceTypeId()));
        objectTypeRepository.findById(request.getTargetTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("ObjectType", request.getTargetTypeId()));

        Relation relation = Relation.create(
                request.getOntologyId(),
                request.getSourceTypeId(),
                request.getTargetTypeId(),
                request.getName(),
                request.getDisplayName(),
                request.getDescription(),
                request.getCardinality()
        );

        if (request.getReverseName() != null) {
            relation.setReverse(request.getReverseName(), request.getReverseDisplayName());
        }

        // 添加关系属性
        if (request.getProperties() != null) {
            for (var propDto : request.getProperties()) {
                RelationProperty prop = RelationProperty.create(
                        propDto.getName(),
                        propDto.getDisplayName(),
                        propDto.getDataType(),
                        propDto.isRequired()
                );
                relation.addProperty(prop);
            }
        }

        // TODO: 保存关系
        log.info("Relation created: id={}", relation.getId());

        return toRelationResponse(relation);
    }

    @Override
    @Transactional
    public RelationResponse updateRelation(String id, UpdateRelationRequest request) {
        log.info("Updating relation: id={}", id);
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED, "Relation update not implemented");
    }

    @Override
    @Transactional
    public void deleteRelation(String id) {
        log.info("Deleting relation: id={}", id);
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED, "Relation delete not implemented");
    }

    // ==================== 查询 ====================

    @Override
    public GraphQueryResponse graphTraversal(GraphQueryRequest request) {
        log.info("Executing graph traversal: ontologyId={}, startType={}, startId={}",
                request.getOntologyId(), request.getStartObjectType(), request.getStartObjectId());

        // TODO: 实现图遍历查询
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED, "Graph traversal not implemented");
    }

    @Override
    public ObjectListResponse queryObjects(ObjectQueryRequest request) {
        log.info("Querying objects: ontologyId={}, objectType={}",
                request.getOntologyId(), request.getObjectType());

        // TODO: 实现对象列表查询
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED, "Object query not implemented");
    }

    // ==================== 转换方法 ====================

    private OntologyResponse toOntologyResponse(Ontology ontology) {
        return OntologyResponse.builder()
                .id(ontology.getId())
                .name(ontology.getName())
                .displayName(ontology.getDisplayName())
                .description(ontology.getDescription())
                .version(ontology.getVersion())
                .status(ontology.getStatus())
                .publishedAt(ontology.getPublishedAt())
                .objectTypeCount(ontology.getObjectTypeCount())
                .actionTypeCount(ontology.getActionTypeCount())
                .createdAt(ontology.getCreatedAt())
                .updatedAt(ontology.getUpdatedAt())
                .build();
    }

    private OntologyDetailResponse toOntologyDetailResponse(Ontology ontology) {
        OntologyDetailResponse response = new OntologyDetailResponse();
        response.setId(ontology.getId());
        response.setName(ontology.getName());
        response.setDisplayName(ontology.getDisplayName());
        response.setDescription(ontology.getDescription());
        response.setVersion(ontology.getVersion());
        response.setStatus(ontology.getStatus());
        response.setPublishedAt(ontology.getPublishedAt());
        response.setObjectTypeCount(ontology.getObjectTypeCount());
        response.setActionTypeCount(ontology.getActionTypeCount());
        response.setCreatedAt(ontology.getCreatedAt());
        response.setUpdatedAt(ontology.getUpdatedAt());

        // 设置对象类型摘要
        response.setObjectTypes(ontology.getObjectTypes().stream()
                .map(ot -> OntologyDetailResponse.ObjectTypeSummary.builder()
                        .id(ot.getId())
                        .name(ot.getName())
                        .displayName(ot.getDisplayName())
                        .propertyCount(ot.getProperties().size())
                        .relationCount(ot.getRelations().size())
                        .instanceCount(ot.getInstanceCount())
                        .build())
                .collect(Collectors.toList()));

        return response;
    }

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
                .instanceCount(objectType.getInstanceCount())
                .createdAt(objectType.getCreatedAt())
                .updatedAt(objectType.getUpdatedAt())
                .build();
    }

    private ObjectTypeDetailResponse toObjectTypeDetailResponse(ObjectType objectType) {
        ObjectTypeDetailResponse response = new ObjectTypeDetailResponse();
        response.setId(objectType.getId());
        response.setOntologyId(objectType.getOntologyId());
        response.setName(objectType.getName());
        response.setDisplayName(objectType.getDisplayName());
        response.setDescription(objectType.getDescription());
        response.setPrimaryKey(objectType.getPrimaryKey());
        response.setParentId(objectType.getParentId());
        response.setInterfaceNames(objectType.getInterfaceNames());
        response.setInstanceCount(objectType.getInstanceCount());
        response.setCreatedAt(objectType.getCreatedAt());
        response.setUpdatedAt(objectType.getUpdatedAt());

        response.setProperties(objectType.getProperties().stream()
                .map(this::toPropertyResponse)
                .collect(Collectors.toList()));

        return response;
    }

    private PropertyResponse toPropertyResponse(Property property) {
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

    private RelationResponse toRelationResponse(Relation relation) {
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
                .createdAt(relation.getCreatedAt())
                .updatedAt(relation.getUpdatedAt())
                .build();
    }
}
