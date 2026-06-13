package com.ontology.platform.application.service.impl;

import com.ontology.platform.application.dto.*;
import com.ontology.platform.application.service.OntologyService;
import com.ontology.platform.application.service.graph.GraphQueryService;
import com.ontology.platform.common.exception.ResourceNotFoundException;
import com.ontology.platform.common.exception.ValidationException;
import com.ontology.platform.domain.entity.ObjectInstance;
import com.ontology.platform.domain.entity.ObjectType;
import com.ontology.platform.domain.entity.Ontology;
import com.ontology.platform.domain.entity.Relation;
import com.ontology.platform.domain.repository.ObjectInstanceRepository;
import com.ontology.platform.domain.repository.ObjectTypeRepository;
import com.ontology.platform.domain.repository.OntologyRepository;
import com.ontology.platform.domain.repository.PropertyRepository;
import com.ontology.platform.domain.repository.RelationRepository;
import com.ontology.platform.domain.vo.Property;
import com.ontology.platform.domain.vo.RelationProperty;
import com.ontology.platform.domain.vo.traversal.GraphTraversalRequest;
import com.ontology.platform.domain.vo.traversal.TraversalResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final RelationRepository relationRepository;
    private final PropertyRepository propertyRepository;
    private final ObjectInstanceRepository objectInstanceRepository;
    private final GraphQueryService graphQueryService;

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

        List<ValidationResultResponse.ValidationIssue> issues = new ArrayList<>();
        int errors = 0;
        int warnings = 0;
        int passed = 0;

        // 1. 验证本体名称非空
        if (ontology.getName() == null || ontology.getName().isBlank()) {
            issues.add(ValidationResultResponse.ValidationIssue.builder()
                    .severity("ERROR")
                    .type("ONTOLOGY_NAME_EMPTY")
                    .entityType("Ontology")
                    .entityId(ontology.getId())
                    .message("本体名称不能为空")
                    .build());
            errors++;
        } else {
            passed++;
        }

        // 2. 验证对象类型
        List<ObjectType> objectTypes = objectTypeRepository.findByOntologyId(id);
        if (objectTypes.isEmpty()) {
            issues.add(ValidationResultResponse.ValidationIssue.builder()
                    .severity("WARNING")
                    .type("NO_OBJECT_TYPES")
                    .entityType("Ontology")
                    .entityId(ontology.getId())
                    .message("本体未定义任何对象类型")
                    .suggestion("请至少创建一个对象类型")
                    .build());
            warnings++;
        } else {
            passed++;
        }

        // 3. 验证每个对象类型是否有属性和主键
        for (ObjectType ot : objectTypes) {
            if (ot.getProperties().isEmpty()) {
                issues.add(ValidationResultResponse.ValidationIssue.builder()
                        .severity("WARNING")
                        .type("OBJECT_TYPE_NO_PROPERTIES")
                        .entityType("ObjectType")
                        .entityId(ot.getId())
                        .entityName(ot.getName())
                        .message(String.format("对象类型 '%s' 未定义任何属性", ot.getName()))
                        .suggestion("请为该对象类型添加属性定义")
                        .build());
                warnings++;
            } else {
                passed++;
            }

            // 验证主键属性是否存在
            if (ot.getPrimaryKey() != null && !ot.getPrimaryKey().isBlank()) {
                boolean pkExists = ot.getProperties().stream()
                        .anyMatch(p -> p.getName().equals(ot.getPrimaryKey()));
                if (!pkExists) {
                    issues.add(ValidationResultResponse.ValidationIssue.builder()
                            .severity("ERROR")
                            .type("PRIMARY_KEY_NOT_FOUND")
                            .entityType("ObjectType")
                            .entityId(ot.getId())
                            .entityName(ot.getName())
                            .message(String.format("对象类型 '%s' 的主键属性 '%s' 不存在",
                                    ot.getName(), ot.getPrimaryKey()))
                            .suggestion("请添加名为 '" + ot.getPrimaryKey() + "' 的属性，或修改主键设置")
                            .build());
                    errors++;
                } else {
                    passed++;
                }
            }
        }

        // 4. 验证关系引用的对象类型是否存在
        List<Relation> relations = relationRepository.findByOntologyId(id);
        for (Relation rel : relations) {
            boolean sourceExists = objectTypes.stream().anyMatch(ot -> ot.getId().equals(rel.getSourceTypeId()));
            boolean targetExists = objectTypes.stream().anyMatch(ot -> ot.getId().equals(rel.getTargetTypeId()));
            if (!sourceExists || !targetExists) {
                issues.add(ValidationResultResponse.ValidationIssue.builder()
                        .severity("ERROR")
                        .type("RELATION_INVALID_REFERENCE")
                        .entityType("Relation")
                        .entityId(rel.getId())
                        .entityName(rel.getName())
                        .message(String.format("关系 '%s' 引用了不存在的对象类型", rel.getName()))
                        .suggestion("请检查关系的源对象类型和目标对象类型是否正确")
                        .build());
                errors++;
            } else {
                passed++;
            }
        }

        ValidationResultResponse.ValidationSummary summary = ValidationResultResponse.ValidationSummary.builder()
                .errors(errors)
                .warnings(warnings)
                .passed(passed)
                .build();

        return ValidationResultResponse.builder()
                .valid(errors == 0)
                .summary(summary)
                .issues(issues)
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

        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property", id));

        // 应用更新字段
        if (request.getDisplayName() != null) {
            property.setDisplayName(request.getDisplayName());
        }
        if (request.getDescription() != null) {
            property.setDescription(request.getDescription());
        }
        if (request.getDataType() != null) {
            property.setDataType(request.getDataType());
        }
        if (request.getIsRequired() != null) {
            property.setRequired(request.getIsRequired());
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
            property.setSortOrder(request.getSortOrder());
        }

        property = propertyRepository.update(property);
        log.info("Property updated: id={}", id);

        return toPropertyResponse(property);
    }

    @Override
    @Transactional
    public void deleteProperty(String id) {
        log.info("Deleting property: id={}", id);

        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property", id));

        // 从所属对象类型中移除该属性
        ObjectType objectType = objectTypeRepository.findById(property.getObjectTypeId())
                .orElse(null);
        if (objectType != null) {
            objectType.removeProperty(id);
            objectTypeRepository.update(objectType);
        }

        propertyRepository.deleteById(id);
        log.info("Property deleted: id={}", id);
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

        // 保存关系
        relation = relationRepository.save(relation);
        log.info("Relation created: id={}", relation.getId());

        return toRelationResponse(relation);
    }

    @Override
    @Transactional
    public RelationResponse updateRelation(String id, UpdateRelationRequest request) {
        log.info("Updating relation: id={}", id);

        Relation relation = relationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Relation", id));

        // 更新基本字段
        if (request.getDisplayName() != null) {
            relation.setDisplayName(request.getDisplayName());
        }
        if (request.getDescription() != null) {
            relation.setDescription(request.getDescription());
        }

        // 更新反向关系信息
        if (request.getReverseName() != null) {
            relation.setReverse(request.getReverseName(), request.getReverseDisplayName());
        }

        // 更新关系属性
        if (request.getProperties() != null) {
            relation.getProperties().clear();
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

        relation = relationRepository.update(relation);
        log.info("Relation updated: id={}", id);

        return toRelationResponse(relation);
    }

    @Override
    @Transactional
    public void deleteRelation(String id) {
        log.info("Deleting relation: id={}", id);

        Relation relation = relationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Relation", id));

        // 从源对象类型和目标对象类型中移除关系引用
        ObjectType sourceType = objectTypeRepository.findById(relation.getSourceTypeId()).orElse(null);
        if (sourceType != null) {
            sourceType.removeRelation(id);
            objectTypeRepository.update(sourceType);
        }

        ObjectType targetType = objectTypeRepository.findById(relation.getTargetTypeId()).orElse(null);
        if (targetType != null) {
            targetType.removeRelation(id);
            objectTypeRepository.update(targetType);
        }

        relationRepository.deleteById(id);
        log.info("Relation deleted: id={}", id);
    }

    // ==================== 查询 ====================

    @Override
    public GraphQueryResponse graphTraversal(GraphQueryRequest request) {
        log.info("Executing graph traversal: ontologyId={}, startType={}, startId={}",
                request.getOntologyId(), request.getStartObjectType(), request.getStartObjectId());

        // 构建领域层图遍历请求
        GraphTraversalRequest traversalRequest = GraphTraversalRequest.builder()
                .startObjectType(request.getStartObjectType())
                .startObjectId(request.getStartObjectId())
                .maxDepth(request.getMaxDepth() != null ? request.getMaxDepth() : 3)
                .limit(request.getLimit() != null ? request.getLimit() : 100)
                .build();

        // 委托给图查询服务执行
        TraversalResult result = graphQueryService.traverse(request.getOntologyId(), traversalRequest);

        // 将遍历结果转换为API响应DTO
        List<GraphQueryResponse.Node> nodes = new ArrayList<>();
        List<GraphQueryResponse.Edge> edges = new ArrayList<>();

        if (result.getNodes() != null) {
            for (var node : result.getNodes()) {
                nodes.add(GraphQueryResponse.Node.builder()
                        .id(node.getObjectId())
                        .type(node.getObjectType())
                        .data(node.getProperties())
                        .build());
            }
        }

        if (result.getEdges() != null) {
            for (var edge : result.getEdges()) {
                edges.add(GraphQueryResponse.Edge.builder()
                        .source(edge.getSourceId())
                        .target(edge.getTargetId())
                        .relation(edge.getRelationType())
                        .properties(edge.getProperties())
                        .build());
            }
        }

        GraphQueryResponse.GraphMetadata metadata = GraphQueryResponse.GraphMetadata.builder()
                .totalNodes(nodes.size())
                .totalEdges(edges.size())
                .queryTimeMs(result.getExecutionTimeMs())
                .build();

        return GraphQueryResponse.builder()
                .nodes(nodes)
                .edges(edges)
                .metadata(metadata)
                .build();
    }

    @Override
    public ObjectListResponse queryObjects(ObjectQueryRequest request) {
        log.info("Querying objects: ontologyId={}, objectType={}",
                request.getOntologyId(), request.getObjectType());

        // 验证本体存在
        ontologyRepository.findById(request.getOntologyId())
                .orElseThrow(() -> new ResourceNotFoundException("Ontology", request.getOntologyId()));

        // 验证对象类型存在，并获取 objectTypeId
        ObjectType objectType = objectTypeRepository
                .findByOntologyIdAndName(request.getOntologyId(), request.getObjectType())
                .orElseThrow(() -> new ResourceNotFoundException("ObjectType", request.getObjectType()));
        String objectTypeId = objectType.getId();

        // 拉取分页数据与总数
        int offset = request.getOffset() != null ? request.getOffset() : 0;
        int limit = request.getLimit() != null ? request.getLimit() : 20;

        List<ObjectInstance> instances = objectInstanceRepository.findByObjectTypeId(objectTypeId, offset, limit);
        long total = objectInstanceRepository.countByObjectTypeId(objectTypeId);

        // 转换为 ObjectListResponse.ObjectData（coreData 合并 extendedData，extendedData 优先）
        List<ObjectListResponse.ObjectData> items = instances.stream()
                .map(instance -> {
                    Map<String, Object> merged = new HashMap<>();
                    if (instance.getCoreData() != null) {
                        merged.putAll(instance.getCoreData());
                    }
                    if (instance.getExtendedData() != null) {
                        merged.putAll(instance.getExtendedData());
                    }
                    return ObjectListResponse.ObjectData.builder()
                            .id(instance.getId())
                            .objectType(request.getObjectType())
                            .properties(merged)
                            .build();
                })
                .collect(Collectors.toList());

        ObjectListResponse.PaginationMeta meta = ObjectListResponse.PaginationMeta.builder()
                .total((int) Math.min(total, Integer.MAX_VALUE))
                .offset(offset)
                .limit(limit)
                .hasMore(offset + items.size() < total)
                .build();

        return ObjectListResponse.builder()
                .items(items)
                .meta(meta)
                .build();
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
