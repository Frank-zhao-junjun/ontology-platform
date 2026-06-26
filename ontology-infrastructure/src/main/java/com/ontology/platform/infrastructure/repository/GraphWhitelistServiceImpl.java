package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.domain.entity.ObjectType;
import com.ontology.platform.domain.entity.Relation;
import com.ontology.platform.domain.repository.ObjectTypeRepository;
import com.ontology.platform.domain.repository.RelationRepository;
import com.ontology.platform.domain.service.GraphWhitelistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 图遍历白名单服务实现 — DB 直查（不依赖 Redis 缓存）
 * Graph Whitelist Service Implementation (DB-only, no Redis)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GraphWhitelistServiceImpl implements GraphWhitelistService {

    private final ObjectTypeRepository objectTypeRepository;
    private final RelationRepository relationRepository;

    @Override
    public boolean isRelationTypeAllowed(String relationType) {
        if (relationType == null || relationType.isBlank()) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isObjectTypeAllowed(String objectType) {
        if (objectType == null || objectType.isBlank()) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isPropertyAllowed(String objectType, String fieldName) {
        if (objectType == null || fieldName == null) {
            return false;
        }
        Set<String> allowedProperties = getAllowedProperties(objectType);
        if (allowedProperties == null || allowedProperties.isEmpty()) {
            return true;
        }
        return allowedProperties.contains(fieldName);
    }

    @Override
    public Set<String> getAllowedProperties(String objectType) {
        if (objectType == null || objectType.isBlank()) {
            return Set.of();
        }
        return Set.of();
    }

    @Override
    public Set<String> getAllowedRelationTypes(String ontologyId) {
        if (ontologyId == null) {
            return Set.of();
        }

        Set<String> relationTypes = new HashSet<>();
        try {
            List<Relation> relations = relationRepository.findByOntologyId(ontologyId);
            relationTypes = new HashSet<>(relations.stream()
                    .map(Relation::getName)
                    .toList());
        } catch (Exception e) {
            log.error("Failed to load relation types from DB: {}", e.getMessage());
        }
        return relationTypes;
    }

    @Override
    public Set<String> getAllowedObjectTypes(String ontologyId) {
        if (ontologyId == null) {
            return Set.of();
        }

        Set<String> objectTypes = new HashSet<>();
        try {
            List<ObjectType> types = objectTypeRepository.findByOntologyId(ontologyId);
            objectTypes = new HashSet<>(types.stream()
                    .map(ObjectType::getName)
                    .toList());
        } catch (Exception e) {
            log.error("Failed to load object types from DB: {}", e.getMessage());
        }
        return objectTypes;
    }

    @Override
    public String normalizeRelationType(String relationType) {
        if (relationType == null) {
            return "";
        }
        return relationType.toUpperCase().trim();
    }

    @Override
    public String normalizeObjectType(String objectType) {
        if (objectType == null) {
            return "";
        }
        return objectType.toLowerCase().trim();
    }
}
