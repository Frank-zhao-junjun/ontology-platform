package com.ontology.platform.application.service;

import com.ontology.platform.common.enums.ErrorCode;
import com.ontology.platform.common.exception.BusinessException;
import com.ontology.platform.common.exception.ResourceNotFoundException;
import com.ontology.platform.domain.entity.AggregateRoot;
import com.ontology.platform.domain.entity.ObjectTypeV2;
import com.ontology.platform.domain.entity.Relationship;
import com.ontology.platform.domain.repository.AggregateRootRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ModelingService {
    private final AggregateRootRepository aggregateRootRepo;
    // In-memory stores for ObjectType and Relationship (MVP simplicity)
    private final Map<String, ObjectTypeV2> objectTypes = new ConcurrentHashMap<>();
    private final Map<String, Relationship> relationships = new ConcurrentHashMap<>();

    // ──── US-S03: Aggregate Roots ────
    public AggregateRoot createAggregateRoot(String contextId, String name, String code, String desc) {
        if (aggregateRootRepo.existsByCode(contextId, code))
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "聚合根 '" + code + "' 已存在于此上下文");
        AggregateRoot ar = AggregateRoot.create(contextId, name, code, desc);
        aggregateRootRepo.save(ar);
        return ar;
    }
    public List<AggregateRoot> listAggregateRoots(String contextId) { return aggregateRootRepo.findByContextId(contextId); }
    public AggregateRoot getAggregateRoot(String id) { return aggregateRootRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("AggregateRoot not found: " + id)); }

    // ──── US-S04: Object Types ────
    public ObjectTypeV2 createObjectType(String contextId, String name, String code, String objectKind, String aggregateRootId) {
        ObjectTypeV2 ot = ObjectTypeV2.create(contextId, name, code, objectKind, aggregateRootId);
        objectTypes.put(ot.getId(), ot);
        return ot;
    }
    public List<ObjectTypeV2> listObjectTypes(String contextId) { return objectTypes.values().stream().filter(o -> o.getContextId().equals(contextId)).collect(Collectors.toList()); }
    public ObjectTypeV2 getObjectType(String id) { return Optional.ofNullable(objectTypes.get(id)).orElseThrow(() -> new ResourceNotFoundException("ObjectType not found: " + id)); }
    public ObjectTypeV2 updateAttributes(String id, String attributes) { ObjectTypeV2 ot = getObjectType(id); ot.setAttributes(attributes); return ot; }

    // ──── US-S07: Relationships ────
    public Relationship createRelationship(String contextId, String sourceId, String targetId, String name, String code, String cardinality, String kind) {
        if ("COMPOSITION".equals(kind)) {
            // Validate: composition only within same aggregate root
            ObjectTypeV2 src = getObjectType(sourceId);
            ObjectTypeV2 tgt = getObjectType(targetId);
            if (src.getAggregateRootId() == null || !src.getAggregateRootId().equals(tgt.getAggregateRootId()))
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, "组合关系只能在同一聚合根内的对象之间建立");
        }
        Relationship r = Relationship.create(contextId, sourceId, targetId, name, code, cardinality, kind);
        relationships.put(r.getId(), r);
        return r;
    }
    public List<Relationship> listRelationships(String contextId) { return relationships.values().stream().filter(r -> r.getContextId().equals(contextId)).collect(Collectors.toList()); }
}
