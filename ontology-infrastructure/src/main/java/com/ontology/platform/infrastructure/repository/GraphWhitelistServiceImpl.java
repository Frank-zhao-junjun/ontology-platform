package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.domain.entity.ObjectType;
import com.ontology.platform.domain.entity.Relation;
import com.ontology.platform.domain.repository.ObjectTypeRepository;
import com.ontology.platform.domain.repository.RelationRepository;
import com.ontology.platform.domain.service.GraphWhitelistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 图遍历白名单服务实现
 * Graph Whitelist Service Implementation
 * 
 * 使用Redis缓存白名单配置，提高查询性能。
 * Redis 可选 — 无 Redis 时直接从 DB 查询（无缓存降级）。
 */
@Slf4j
@Service
public class GraphWhitelistServiceImpl implements GraphWhitelistService {
    
    private final ObjectTypeRepository objectTypeRepository;
    private final RelationRepository relationRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String RELATION_TYPES_KEY = "ontology:whitelist:relations:%s";
    private static final String OBJECT_TYPES_KEY = "ontology:whitelist:objects:%s";
    private static final String PROPERTIES_KEY = "ontology:whitelist:props:%s:%s";
    private static final long CACHE_TTL_MINUTES = 30;

    /**
     * RedisTemplate is optional. When Redis is not available, 
     * the service works without caching (DB-only).
     */
    public GraphWhitelistServiceImpl(
            ObjectTypeRepository objectTypeRepository,
            RelationRepository relationRepository,
            @Autowired(required = false) RedisTemplate<String, Object> redisTemplate) {
        this.objectTypeRepository = objectTypeRepository;
        this.relationRepository = relationRepository;
        this.redisTemplate = redisTemplate;
        if (redisTemplate == null) {
            log.warn("RedisTemplate not available — GraphWhitelistServiceImpl running without cache");
        }
    }
    
    private String relationTypesKey(String ontologyId) {
        return String.format(RELATION_TYPES_KEY, ontologyId);
    }
    
    private String objectTypesKey(String ontologyId) {
        return String.format(OBJECT_TYPES_KEY, ontologyId);
    }
    
    private String propertiesKey(String ontologyId, String objectType) {
        return String.format(PROPERTIES_KEY, ontologyId, objectType);
    }
    
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
        
        if (redisTemplate != null) {
            try {
                @SuppressWarnings("unchecked")
                Set<String> cached = (Set<String>) redisTemplate.opsForValue()
                        .get(relationTypesKey(ontologyId));
                if (cached != null) {
                    return cached;
                }
            } catch (Exception e) {
                log.warn("Failed to get cached relation types: {}", e.getMessage());
            }
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
        
        if (redisTemplate != null) {
            try {
                redisTemplate.opsForValue().set(
                    relationTypesKey(ontologyId), 
                    relationTypes, 
                    CACHE_TTL_MINUTES, 
                    TimeUnit.MINUTES
                );
            } catch (Exception e) {
                log.warn("Failed to cache relation types: {}", e.getMessage());
            }
        }
        
        return relationTypes;
    }
    
    @Override
    public Set<String> getAllowedObjectTypes(String ontologyId) {
        if (ontologyId == null) {
            return Set.of();
        }
        
        if (redisTemplate != null) {
            try {
                @SuppressWarnings("unchecked")
                Set<String> cached = (Set<String>) redisTemplate.opsForValue()
                        .get(objectTypesKey(ontologyId));
                if (cached != null) {
                    return cached;
                }
            } catch (Exception e) {
                log.warn("Failed to get cached object types: {}", e.getMessage());
            }
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
        
        if (redisTemplate != null) {
            try {
                redisTemplate.opsForValue().set(
                    objectTypesKey(ontologyId), 
                    objectTypes, 
                    CACHE_TTL_MINUTES, 
                    TimeUnit.MINUTES
                );
            } catch (Exception e) {
                log.warn("Failed to cache object types: {}", e.getMessage());
            }
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
    
    public void clearCache(String ontologyId) {
        if (redisTemplate == null) return;
        try {
            redisTemplate.delete(relationTypesKey(ontologyId));
            redisTemplate.delete(objectTypesKey(ontologyId));
            log.debug("Cleared whitelist cache for ontology: {}", ontologyId);
        } catch (Exception e) {
            log.warn("Failed to clear whitelist cache: {}", e.getMessage());
        }
    }
    
    public void warmupCache(String ontologyId) {
        getAllowedRelationTypes(ontologyId);
        getAllowedObjectTypes(ontologyId);
        log.info("Warmed up whitelist cache for ontology: {}", ontologyId);
    }
}
