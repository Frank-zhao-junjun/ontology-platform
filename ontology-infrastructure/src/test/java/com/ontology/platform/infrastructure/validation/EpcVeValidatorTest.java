package com.ontology.platform.infrastructure.validation;

import com.ontology.platform.domain.dto.imports.OntologyExchangeDocument;
import com.ontology.platform.domain.service.validation.ValidationContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link EpcVeValidator}.
 */
class EpcVeValidatorTest {

    private final EpcVeValidator validator = new EpcVeValidator();

    private OntologyExchangeDocument createDoc() {
        var doc = new OntologyExchangeDocument();
        doc.setMetadata(OntologyExchangeDocument.Metadata.builder().projectId("test").build());
        doc.setSpec(new OntologyExchangeDocument.Spec());
        var project = new OntologyExchangeDocument.OntologyProject();
        doc.getSpec().setProject(project);
        var dm = new OntologyExchangeDocument.DataModel();
        project.setDataModel(dm);
        return doc;
    }

    @Test
    @DisplayName("VE-01/02: valid entity → no issues")
    void validEntity_noIssues() {
        var doc = createDoc();
        var entity = OntologyExchangeDocument.Entity.builder()
                .id("valid-entity").name("有效实体").nameEn("ValidEntity")
                .entityRole("aggregate_root").businessScenarioId("scenario-1")
                .build();
        doc.getSpec().getProject().getDataModel().setEntities(List.of(entity));

        var ctx = new ValidationContext(doc, "strict", "test");
        assertThat(validator.validate(ctx)).isEmpty();
    }

    @Test
    @DisplayName("VE-01: empty ID → error")
    void missingId_error() {
        var doc = createDoc();
        var entity = OntologyExchangeDocument.Entity.builder()
                .name("NoID").entityRole("aggregate_root").build();
        doc.getSpec().getProject().getDataModel().setEntities(List.of(entity));

        var ctx = new ValidationContext(doc, "strict", "test");
        var issues = validator.validate(ctx);
        assertThat(issues).anyMatch(i -> "VE-01".equals(i.getCode()));
    }

    @Test
    @DisplayName("VE-02: empty name → error")
    void missingName_error() {
        var doc = createDoc();
        var entity = OntologyExchangeDocument.Entity.builder()
                .id("no-name").entityRole("aggregate_root").build();
        doc.getSpec().getProject().getDataModel().setEntities(List.of(entity));

        var ctx = new ValidationContext(doc, "strict", "test");
        var issues = validator.validate(ctx);
        assertThat(issues).anyMatch(i -> "VE-02".equals(i.getCode()));
    }

    @Test
    @DisplayName("VE-03: child_entity without parentAggregateId → error")
    void childEntityWithoutParent_error() {
        var doc = createDoc();
        var entity = OntologyExchangeDocument.Entity.builder()
                .id("child-no-parent").name("子实体")
                .entityRole("child_entity").build();
        doc.getSpec().getProject().getDataModel().setEntities(List.of(entity));

        var ctx = new ValidationContext(doc, "strict", "test");
        var issues = validator.validate(ctx);
        assertThat(issues).anyMatch(i -> "VE-03".equals(i.getCode()));
    }

    @Test
    @DisplayName("VE-04: missing businessScenarioId → warning")
    void missingScenario_warning() {
        var doc = createDoc();
        var entity = OntologyExchangeDocument.Entity.builder()
                .id("no-scenario").name("无场景实体")
                .entityRole("aggregate_root").build();
        doc.getSpec().getProject().getDataModel().setEntities(List.of(entity));

        var ctx = new ValidationContext(doc, "strict", "test");
        var issues = validator.validate(ctx);
        assertThat(issues).anyMatch(i -> "VE-04".equals(i.getCode()) && "warning".equals(i.getSeverity()));
    }

    @Test
    @DisplayName("VE-05: missing attribute name → error")
    void missingAttributeName_error() {
        var doc = createDoc();
        var attr = OntologyExchangeDocument.Attribute.builder()
                .id("attr-1").build(); // no name
        var entity = OntologyExchangeDocument.Entity.builder()
                .id("entity-a").name("Entity A")
                .entityRole("aggregate_root").businessScenarioId("s1")
                .attributes(List.of(attr)).build();
        doc.getSpec().getProject().getDataModel().setEntities(List.of(entity));

        var ctx = new ValidationContext(doc, "strict", "test");
        var issues = validator.validate(ctx);
        assertThat(issues).anyMatch(i -> "VE-05".equals(i.getCode()));
    }

    @Test
    @DisplayName("VE-06: empty relation target → error")
    void emptyRelationTarget_error() {
        var doc = createDoc();
        var rel = OntologyExchangeDocument.Relation.builder()
                .id("rel-1").name("test rel").build(); // no targetEntity
        var entity = OntologyExchangeDocument.Entity.builder()
                .id("entity-b").name("Entity B")
                .entityRole("aggregate_root").businessScenarioId("s1")
                .relations(List.of(rel)).build();
        doc.getSpec().getProject().getDataModel().setEntities(List.of(entity));

        var ctx = new ValidationContext(doc, "strict", "test");
        var issues = validator.validate(ctx);
        assertThat(issues).anyMatch(i -> "VE-06".equals(i.getCode()));
    }

    @Test
    @DisplayName("null document → VE-00 error")
    void nullDocument_error() {
        var ctx = new ValidationContext(null, "strict", null);
        var issues = validator.validate(ctx);
        assertThat(issues).anyMatch(i -> "VE-00".equals(i.getCode()));
    }
}
