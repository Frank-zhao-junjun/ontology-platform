package com.ontology.platform.application.service.manifest;

import com.ontology.platform.domain.vo.manifest.*;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ManifestValidator V01-V11")
class ManifestValidatorTest {

    private final ManifestValidator validator = new ManifestValidator();

    private ManifestDocument validDoc() {
        return ManifestDocument.builder()
                .apiVersion("ontology.platform/v1")
                .metadata(ManifestDocument.Metadata.builder().id("test").version("0.1.0").build())
                .spec(ManifestDocument.Spec.builder()
                        .semantic(ManifestDocument.Semantic.builder()
                                .objectTypes(List.of(
                                        ManifestDocument.ObjectType.builder().id("po").kind("aggregate_root").name("PO").build(),
                                        ManifestDocument.ObjectType.builder().id("item").kind("entity").aggregateRootId("po").name("Item").build()
                                )).build())
                        .build())
                .build();
    }

    @Test @DisplayName("V01: reject unsupported apiVersion")
    void v01RejectsBadApiVersion() {
        ManifestDocument doc = validDoc();
        doc.setApiVersion("v99");
        ManifestValidationResult r = validator.validate(doc);
        assertThat(r.isValid()).isFalse();
        assertThat(r.getErrors()).anyMatch(e -> "V01".equals(e.getCode()));
    }

    @Test @DisplayName("V02: reject invalid semver")
    void v02RejectsBadSemver() {
        ManifestDocument doc = validDoc();
        doc.getMetadata().setVersion("abc");
        ManifestValidationResult r = validator.validate(doc);
        assertThat(r.isValid()).isFalse();
        assertThat(r.getErrors()).anyMatch(e -> "V02".equals(e.getCode()));
    }

    @Test @DisplayName("V03: reject no aggregate_root")
    void v03RejectsNoAggregateRoot() {
        ManifestDocument doc = validDoc();
        doc.getSpec().getSemantic().setObjectTypes(List.of(
                ManifestDocument.ObjectType.builder().id("x").kind("entity").build()));
        ManifestValidationResult r = validator.validate(doc);
        assertThat(r.isValid()).isFalse();
        assertThat(r.getErrors()).anyMatch(e -> "V03".equals(e.getCode()));
    }

    @Test @DisplayName("V04: reject broken entity ref")
    void v04RejectsBrokenEntityRef() {
        ManifestDocument doc = validDoc();
        doc.getSpec().getSemantic().getObjectTypes().get(1).setAggregateRootId("missing");
        ManifestValidationResult r = validator.validate(doc);
        assertThat(r.isValid()).isFalse();
        assertThat(r.getErrors()).anyMatch(e -> "V04".equals(e.getCode()));
    }

    @Test @DisplayName("V05: reject broken action aggregateRootId")
    void v05RejectsBrokenActionRef() {
        ManifestDocument doc = validDoc();
        doc.setSpec(ManifestDocument.Spec.builder()
                .semantic(doc.getSpec().getSemantic())
                .behavior(ManifestDocument.Behavior.builder()
                        .actions(List.of(ManifestDocument.ActionDef.builder().id("act1").aggregateRootId("ghost").build()))
                        .build()).build());
        ManifestValidationResult r = validator.validate(doc);
        assertThat(r.isValid()).isFalse();
        assertThat(r.getErrors()).anyMatch(e -> "V05".equals(e.getCode()));
    }

    @Test @DisplayName("V06: reject broken preRuleId ref")
    void v06RejectsBrokenRuleRef() {
        ManifestDocument doc = validDoc();
        doc.setSpec(ManifestDocument.Spec.builder()
                .semantic(doc.getSpec().getSemantic())
                .behavior(ManifestDocument.Behavior.builder()
                        .actions(List.of(ManifestDocument.ActionDef.builder().id("act").aggregateRootId("po").preRuleIds(List.of("ghost-rule")).build()))
                        .build()).build());
        ManifestValidationResult r = validator.validate(doc);
        assertThat(r.isValid()).isFalse();
        assertThat(r.getErrors()).anyMatch(e -> "V06".equals(e.getCode()));
    }

    @Test @DisplayName("V07: reject broken event ref")
    void v07RejectsBrokenEventRef() {
        ManifestDocument doc = validDoc();
        doc.setSpec(ManifestDocument.Spec.builder()
                .semantic(doc.getSpec().getSemantic())
                .behavior(ManifestDocument.Behavior.builder()
                        .actions(List.of(ManifestDocument.ActionDef.builder().id("act").aggregateRootId("po").publishesEventIds(List.of("ghost-event")).build()))
                        .build()).build());
        ManifestValidationResult r = validator.validate(doc);
        assertThat(r.isValid()).isFalse();
        assertThat(r.getErrors()).anyMatch(e -> "V07".equals(e.getCode()));
    }

    @Test @DisplayName("V08: warn non-past-tense event nameEn")
    void v08WarnsNonPastTense() {
        ManifestDocument doc = validDoc();
        doc.setSpec(ManifestDocument.Spec.builder()
                .semantic(doc.getSpec().getSemantic())
                .events(ManifestDocument.Events.builder()
                        .domainEvents(List.of(ManifestDocument.EventDef.builder().id("e1").nameEn("CreateOrder").build()))
                        .build()).build());
        ManifestValidationResult r = validator.validate(doc);
        assertThat(r.getWarnings()).anyMatch(e -> "V08".equals(e.getCode()) && "WARNING".equals(e.getSeverity()));
    }

    @Test @DisplayName("V09: reject stateMachine without exactly 1 isInitial")
    void v09RejectsBadStateCount() {
        ManifestDocument doc = validDoc();
        doc.setSpec(ManifestDocument.Spec.builder()
                .semantic(doc.getSpec().getSemantic())
                .behavior(ManifestDocument.Behavior.builder()
                        .stateMachines(List.of(ManifestDocument.StateMachineDef.builder().id("sm1")
                                .states(List.of(
                                        ManifestDocument.StateDef.builder().name("a").isInitial(true).build(),
                                        ManifestDocument.StateDef.builder().name("b").isInitial(true).build()
                                )).build()))
                        .build()).build());
        ManifestValidationResult r = validator.validate(doc);
        assertThat(r.isValid()).isFalse();
        assertThat(r.getErrors()).anyMatch(e -> "V09".equals(e.getCode()));
    }

    @Test @DisplayName("V10: reject plaintext password in dataSources")
    void v10RejectsCredentials() {
        ManifestDocument doc = validDoc();
        doc.setSpec(ManifestDocument.Spec.builder()
                .semantic(doc.getSpec().getSemantic())
                .dataSources(List.of(ManifestDocument.DataSource.builder().id("ds1")
                        .connectionConfig(java.util.Map.of("password", "admin123")).build()))
                .build());
        ManifestValidationResult r = validator.validate(doc);
        assertThat(r.isValid()).isFalse();
        assertThat(r.getErrors()).anyMatch(e -> "V10".equals(e.getCode()));
    }

    @Test @DisplayName("V11: reject duplicate ids")
    void v11RejectsDuplicateIds() {
        ManifestDocument doc = validDoc();
        doc.setSpec(ManifestDocument.Spec.builder()
                .semantic(ManifestDocument.Semantic.builder()
                        .objectTypes(List.of(
                                ManifestDocument.ObjectType.builder().id("dup").kind("aggregate_root").build(),
                                ManifestDocument.ObjectType.builder().id("dup").kind("entity").build()
                        )).build())
                .build());
        ManifestValidationResult r = validator.validate(doc);
        assertThat(r.isValid()).isFalse();
        assertThat(r.getErrors()).anyMatch(e -> "V11".equals(e.getCode()));
    }

    @Test @DisplayName("valid manifest passes all 11 checks")
    void validManifestPasses() {
        ManifestDocument doc = validDoc();
        ManifestValidationResult r = validator.validate(doc);
        assertThat(r.isValid()).isTrue();
        assertThat(r.getErrors()).isEmpty();
    }

    @Test @DisplayName("null manifest returns V00 error")
    void nullManifestReturnsError() {
        ManifestValidationResult r = validator.validate(null);
        assertThat(r.isValid()).isFalse();
        assertThat(r.getErrors()).anyMatch(e -> "V00".equals(e.getCode()));
    }
}