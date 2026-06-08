package com.ontology.platform.application.service;

import com.ontology.platform.common.enums.ErrorCode;
import com.ontology.platform.common.exception.BusinessException;
import com.ontology.platform.common.exception.ResourceNotFoundException;
import com.ontology.platform.domain.entity.AggregateRoot;
import com.ontology.platform.domain.entity.ObjectTypeV2;
import com.ontology.platform.domain.entity.Relationship;
import com.ontology.platform.domain.repository.AggregateRootRepository;
import com.ontology.platform.domain.repository.ObjectTypeRepository;
import com.ontology.platform.domain.repository.RelationshipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ModelingService {
    private final AggregateRootRepository aggregateRootRepo;
    private final ObjectTypeRepository objectTypeRepo;
    private final RelationshipRepository relationshipRepo;

    public AggregateRoot createAggregateRoot(String contextId, String name, String code, String desc) {
        if (aggregateRootRepo.existsByCode(contextId, code))
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "聚合根 '" + code + "' 已存在于此上下文");
        AggregateRoot ar = AggregateRoot.create(contextId, name, code, desc);
        aggregateRootRepo.save(ar);
        return ar;
    }

    public List<AggregateRoot> listAggregateRoots(String contextId) {
        return aggregateRootRepo.findByContextId(contextId);
    }

    public AggregateRoot getAggregateRoot(String id) {
        return aggregateRootRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("AggregateRoot not found: " + id));
    }

    public ObjectTypeV2 createObjectType(String contextId, String name, String code, String objectKind, String aggregateRootId) {
        ObjectTypeV2 ot = ObjectTypeV2.create(contextId, name, code, objectKind, aggregateRootId);
        objectTypeRepo.save(ot);
        return ot;
    }

    public List<ObjectTypeV2> listObjectTypes(String contextId) {
        return objectTypeRepo.findByContextId(contextId);
    }

    public ObjectTypeV2 getObjectType(String id) {
        return objectTypeRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("ObjectType not found: " + id));
    }

    public ObjectTypeV2 updateAttributes(String id, String attributes) {
        ObjectTypeV2 ot = getObjectType(id);
        ot.setAttributes(attributes);
        objectTypeRepo.save(ot);
        return ot;
    }

    public Relationship createRelationship(String contextId, String sourceId, String targetId,
                                           String name, String code, String cardinality, String kind) {
        return createRelationship(contextId, sourceId, targetId, name, code, cardinality, kind, false, null);
    }

    public Relationship createRelationship(String contextId, String sourceId, String targetId,
                                           String name, String code, String cardinality, String kind,
                                           boolean crossContext, String targetContextId) {
        if ("COMPOSITION".equals(kind) && !crossContext) {
            ObjectTypeV2 src = getObjectType(sourceId);
            ObjectTypeV2 tgt = getObjectType(targetId);
            if (src.getAggregateRootId() == null || !src.getAggregateRootId().equals(tgt.getAggregateRootId()))
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, "组合关系只能在同一聚合根内的对象之间建立");
        }
        Relationship r = Relationship.builder()
                .contextId(contextId).sourceObjectId(sourceId).targetObjectId(targetId)
                .name(name).code(code).cardinality(cardinality).relationKind(kind)
                .crossContext(crossContext).targetContextId(targetContextId).build();
        relationshipRepo.save(r);
        return r;
    }

    public List<Relationship> listRelationships(String contextId) {
        return relationshipRepo.findByContextId(contextId);
    }
}
