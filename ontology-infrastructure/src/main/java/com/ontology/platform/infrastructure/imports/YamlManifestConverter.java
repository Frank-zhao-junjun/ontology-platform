package com.ontology.platform.infrastructure.imports;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.domain.dto.imports.YamlImportResult;
import com.ontology.platform.domain.dto.imports.YamlManifest;
import com.ontology.platform.domain.dto.imports.YamlManifest.*;
import com.ontology.platform.common.enums.PropertyDataType;
import com.ontology.platform.common.enums.RelationCardinality;
import com.ontology.platform.domain.entity.*;
import com.ontology.platform.domain.vo.Property;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * YAML OntologyManifest → 项目2 领域实体 转换器
 *
 * <p>将解析后的 {@link YamlManifest} 转换为领域实体列表。</p>
 */
@Component
public class YamlManifestConverter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 将 YAML Manifest 转换为领域实体集合
     *
     * @param manifest 解析后的 YAML Manifiest
     * @return 包含所有领域实体的 YamlImportResult
     */
    public YamlImportResult convert(YamlManifest manifest) {
        if (manifest == null || manifest.getMetadata() == null) {
            throw new IllegalArgumentException("YAML Manifest 元数据为空");
        }

        List<String> warnings = new ArrayList<>();
        Metadata meta = manifest.getMetadata();

        // === 1. 创建 Ontology ===
        Ontology ontology = createOntology(meta);

        // === 2. 提取对象类型 ===
        List<ObjectType> objectTypes = new ArrayList<>();
        Set<String> processedIds = new HashSet<>();

        if (manifest.getSpec() != null && manifest.getSpec().getSemantic() != null) {
            Semantic sem = manifest.getSpec().getSemantic();

            // 2a. objectTypes
            if (sem.getObjectTypes() != null) {
                for (ObjectTypeDef def : sem.getObjectTypes()) {
                    if (def.getId() == null || def.getId().isBlank()) {
                        warnings.add("跳过无 ID 的 ObjectType");
                        continue;
                    }
                    if (!processedIds.add(def.getId())) {
                        warnings.add("跳过重复 ObjectType ID: " + def.getId());
                        continue;
                    }
                    objectTypes.add(createObjectType(ontology, def));
                }
            }

            // 2b. businessScenarios → ObjectType (kind=scenario)
            if (sem.getBusinessScenarios() != null) {
                for (BusinessScenario bs : sem.getBusinessScenarios()) {
                    if (bs.getId() != null && !bs.getId().isBlank()) {
                        if (!processedIds.add(bs.getId())) continue;
                        objectTypes.add(createScenarioObjectType(ontology, bs));
                    }
                }
            }

            // 2c. valueObjects → ObjectType (kind=value_object)
            if (sem.getValueObjects() != null) {
                for (ValueObject vo : sem.getValueObjects()) {
                    if (vo.getId() != null && !vo.getId().isBlank()) {
                        if (!processedIds.add(vo.getId())) continue;
                        objectTypes.add(createValueObjectType(ontology, vo));
                    }
                }
            }
        }

        // === 3. 状态机 ===
        List<StateMachine> stateMachines = new ArrayList<>();
        if (manifest.getSpec() != null && manifest.getSpec().getSemantic() != null
                && manifest.getSpec().getSemantic().getStateMachines() != null) {
            for (StateMachineDef def : manifest.getSpec().getSemantic().getStateMachines()) {
                if (def.getId() != null && !def.getId().isBlank()) {
                    stateMachines.add(createStateMachine(ontology, def));
                }
            }
        }

        // === 4. 动作定义 ===
        List<ActionDefinition> actions = new ArrayList<>();
        if (manifest.getSpec() != null && manifest.getSpec().getBehavior() != null
                && manifest.getSpec().getBehavior().getActions() != null) {
            for (ActionDef def : manifest.getSpec().getBehavior().getActions()) {
                if (def.getId() != null && !def.getId().isBlank()) {
                    actions.add(createActionDefinition(ontology, def));
                }
            }
        }

        // === 5. 领域事件 ===
        List<DomainEvent> domainEvents = new ArrayList<>();
        if (manifest.getSpec() != null && manifest.getSpec().getEvents() != null
                && manifest.getSpec().getEvents().getDomainEvents() != null) {
            for (DomainEventDef def : manifest.getSpec().getEvents().getDomainEvents()) {
                if (def.getId() != null && !def.getId().isBlank()) {
                    domainEvents.add(createDomainEvent(ontology, def));
                }
            }
        }

        // === 6. 构建结果 ===
        int totalEntities = 1 + objectTypes.size() + stateMachines.size() + actions.size() + domainEvents.size();

        return YamlImportResult.builder()
                .ontology(ontology)
                .objectTypes(objectTypes)
                .stateMachines(stateMachines)
                .actions(actions)
                .domainEvents(domainEvents)
                .totalEntities(totalEntities)
                .warnings(warnings)
                .build();
    }

    // ==================== 实体映射方法 ====================

    private Ontology createOntology(Metadata meta) {
        String description = meta.getDescription();
        if (meta.getBoundedContext() != null && !meta.getBoundedContext().isBlank()) {
            description = (description != null ? description : "")
                    + " (限界上下文: " + meta.getBoundedContext() + ")";
        }

        // domainTags 存为 JSON 语义
        String semantics = null;
        if (meta.getDomainTags() != null && !meta.getDomainTags().isEmpty()) {
            try {
                semantics = objectMapper.writeValueAsString(Map.of("domainTags", meta.getDomainTags()));
            } catch (JsonProcessingException ignored) {}
        }

        return Ontology.create(
                meta.getId() != null ? meta.getId() : UUID.randomUUID().toString(),
                meta.getDisplayName() != null ? meta.getDisplayName() : meta.getName(),
                description,
                semantics != null ? semantics : "{}"
        );
    }

    private ObjectType createObjectType(Ontology ontology, ObjectTypeDef def) {
        ObjectType ot = ObjectType.builder()
                .id(UUID.randomUUID().toString())
                .ontologyId(ontology.getName())
                .name(def.getId())
                .displayName(def.getName())
                .description(def.getDescription())
                .parentId(def.getAggregateRootId())
                .instanceCount(0)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        // 映射属性
        if (def.getProperties() != null) {
            for (PropertyDef pdef : def.getProperties()) {
                ot.addProperty(toProperty(pdef));
            }
        }

        // 映射关系
        if (def.getRelations() != null) {
            for (RelationDef rdef : def.getRelations()) {
                ot.addRelation(toRelation(rdef));
            }
        }

        return ot;
    }

    private ObjectType createScenarioObjectType(Ontology ontology, BusinessScenario bs) {
        return ObjectType.builder()
                .id(UUID.randomUUID().toString())
                .ontologyId(ontology.getName())
                .name(bs.getId())
                .displayName(bs.getName())
                .description(bs.getDescription())
                .primaryKey("scenario")
                .instanceCount(0)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private ObjectType createValueObjectType(Ontology ontology, ValueObject vo) {
        ObjectType ot = ObjectType.builder()
                .id(UUID.randomUUID().toString())
                .ontologyId(ontology.getName())
                .name(vo.getId())
                .displayName(vo.getName())
                .description("值对象")
                .primaryKey("value_object")
                .instanceCount(0)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        if (vo.getProperties() != null) {
            for (PropertyDef pdef : vo.getProperties()) {
                ot.addProperty(toProperty(pdef));
            }
        }

        return ot;
    }

    private StateMachine createStateMachine(Ontology ontology, StateMachineDef def) {
        String statesJson = "[]";
        if (def.getStates() != null && !def.getStates().isEmpty()) {
            try {
                statesJson = objectMapper.writeValueAsString(def.getStates());
            } catch (JsonProcessingException ignored) {}
        }

        String initialState = null;
        if (def.getStates() != null) {
            initialState = def.getStates().stream()
                    .filter(s -> Boolean.TRUE.equals(s.getIsInitial()))
                    .findFirst()
                    .map(StateDef::getCode)
                    .orElse(null);
        }

        return StateMachine.create(
                ontology.getName(),
                def.getObjectTypeId(),
                def.getName(),
                initialState,
                statesJson
        );
    }

    private ActionDefinition createActionDefinition(Ontology ontology, ActionDef def) {
        String riskLevel = "WRITE";
        if (def.getNameEn() != null && def.getNameEn().toLowerCase().contains("query")) {
            riskLevel = "READ";
        }

        String inputSchema = "{}";
        if (def.getParameters() != null && !def.getParameters().isEmpty()) {
            try {
                inputSchema = objectMapper.writeValueAsString(def.getParameters());
            } catch (JsonProcessingException ignored) {}
        }

        String preRules = "[]";
        if (def.getPreRuleIds() != null && !def.getPreRuleIds().isEmpty()) {
            try {
                preRules = objectMapper.writeValueAsString(def.getPreRuleIds());
            } catch (JsonProcessingException ignored) {}
        }

        return ActionDefinition.create(
                ontology.getName(),
                def.getAggregateRootId(),
                def.getId(),
                def.getName(),
                riskLevel,
                "BEHAVIOR",
                riskLevel
        );
    }

    private DomainEvent createDomainEvent(Ontology ontology, DomainEventDef def) {
        String payloadSchema = "{}";
        if (def.getPayloadSchema() != null && def.getPayloadSchema().getRequired() != null) {
            try {
                payloadSchema = objectMapper.writeValueAsString(def.getPayloadSchema());
            } catch (JsonProcessingException ignored) {}
        }

        return DomainEvent.create(
                ontology.getName(),
                def.getAggregateRootId(),
                def.getId(),
                def.getName(),
                "DOMAIN",
                "INFO"
        );
    }

    // ==================== 辅助方法 ====================

    private Property toProperty(PropertyDef pdef) {
        PropertyDataType dataType = PropertyDataType.STRING;
        try {
            if (pdef.getDataType() != null) {
                dataType = PropertyDataType.fromValue(pdef.getDataType());
            }
        } catch (IllegalArgumentException e) {
            dataType = PropertyDataType.STRING;
        }

        boolean isRequired = Boolean.TRUE.equals(pdef.getRequired());

        return Property.builder()
                .id(pdef.getId() != null ? pdef.getId() : UUID.randomUUID().toString())
                .name(pdef.getName() != null ? pdef.getName() : pdef.getId())
                .displayName(pdef.getName())
                .dataType(dataType)
                .description(pdef.getNameEn())
                .isRequired(isRequired)
                .build();
    }

    private Relation toRelation(RelationDef rdef) {
        RelationCardinality cardinality = RelationCardinality.MANY_TO_ONE;
        try {
            if (rdef.getCardinality() != null) {
                cardinality = RelationCardinality.fromValue(rdef.getCardinality());
            }
        } catch (IllegalArgumentException e) {
            cardinality = RelationCardinality.MANY_TO_ONE;
        }

        return Relation.builder()
                .id(rdef.getId() != null ? rdef.getId() : UUID.randomUUID().toString())
                .displayName(rdef.getName())
                .sourceTypeId(rdef.getSourceObjectTypeId())
                .targetTypeId(rdef.getTargetObjectTypeId())
                .cardinality(cardinality)
                .build();
    }
}
