package com.ontology.platform.domain.factory;

import com.ontology.platform.common.enums.OntologyStatus;
import com.ontology.platform.common.enums.PropertyDataType;
import com.ontology.platform.common.enums.RelationCardinality;
import com.ontology.platform.domain.entity.ObjectType;
import com.ontology.platform.domain.entity.Ontology;
import com.ontology.platform.domain.entity.Relation;
import com.ontology.platform.domain.vo.Property;

import java.time.Instant;

/**
 * 测试工厂类 - 提供测试数据构建
 */
public class TestFactory {

    private TestFactory() {
        // 私有构造函数
    }

    // ==================== Ontology Factory ====================

    public static Ontology createOntology(String id, String name, OntologyStatus status) {
        return Ontology.builder()
                .id(id)
                .tenantId("default")
                .name(name)
                .displayName("Display " + name)
                .description("Description for " + name)
                .version("1.0.0")
                .status(status)
                .objectTypeCount(0)
                .actionTypeCount(0)
                .createdBy("test-user")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public static Ontology createDraftOntology(String id, String name) {
        return createOntology(id, name, OntologyStatus.DRAFT);
    }

    public static Ontology createPublishedOntology(String id, String name) {
        Ontology ontology = createOntology(id, name, OntologyStatus.PUBLISHED);
        ontology.setPublishedAt(Instant.now());
        return ontology;
    }

    public static Ontology createArchivedOntology(String id, String name) {
        return createOntology(id, name, OntologyStatus.ARCHIVED);
    }

    // ==================== ObjectType Factory ====================

    public static ObjectType createObjectType(String id, String ontologyId, String name) {
        return ObjectType.builder()
                .id(id)
                .ontologyId(ontologyId)
                .name(name)
                .displayName("Display " + name)
                .description("Description for " + name)
                .primaryKey("id")
                .instanceCount(0)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public static ObjectType createObjectTypeWithProperties(String id, String ontologyId, String name, int propertyCount) {
        ObjectType objectType = createObjectType(id, ontologyId, name);
        for (int i = 0; i < propertyCount; i++) {
            Property property = createProperty("prop-" + i, id, "property" + i, PropertyDataType.STRING);
            objectType.addProperty(property);
        }
        return objectType;
    }

    // ==================== Property Factory ====================

    public static Property createProperty(String id, String objectTypeId, String name, PropertyDataType dataType) {
        return Property.builder()
                .id(id)
                .objectTypeId(objectTypeId)
                .name(name)
                .displayName("Display " + name)
                .description("Description for " + name)
                .dataType(dataType)
                .isRequired(false)
                .isUnique(false)
                .isSearchable(true)
                .isSortable(true)
                .isComputed(false)
                .sortOrder(0)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public static Property createRequiredProperty(String id, String objectTypeId, String name, PropertyDataType dataType) {
        Property property = createProperty(id, objectTypeId, name, dataType);
        property.setRequired(true);
        return property;
    }

    // ==================== Relation Factory ====================

    public static Relation createRelation(String id, String ontologyId, String sourceTypeId, 
                                          String targetTypeId, String name, RelationCardinality cardinality) {
        return Relation.builder()
                .id(id)
                .ontologyId(ontologyId)
                .sourceTypeId(sourceTypeId)
                .targetTypeId(targetTypeId)
                .name(name)
                .displayName("Display " + name)
                .description("Description for " + name)
                .cardinality(cardinality)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public static Relation createOneToManyRelation(String id, String ontologyId, 
                                                     String sourceTypeId, String targetTypeId, String name) {
        return createRelation(id, ontologyId, sourceTypeId, targetTypeId, name, RelationCardinality.ONE_TO_MANY);
    }
}
