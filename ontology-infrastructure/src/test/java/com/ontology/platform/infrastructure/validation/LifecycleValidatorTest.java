package com.ontology.platform.infrastructure.validation;

import com.ontology.platform.domain.dto.imports.OntologyExchangeDocument;
import com.ontology.platform.domain.service.validation.ValidationContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LifecycleValidator Test")
class LifecycleValidatorTest {

    private final LifecycleValidator validator = new LifecycleValidator();

    @Test
    @DisplayName("should pass when behaviorModel is null")
    void passWhenNoBehaviorModel() {
        var doc = OntologyExchangeDocument.builder()
                .spec(OntologyExchangeDocument.Spec.builder()
                        .project(OntologyExchangeDocument.OntologyProject.builder().build())
                        .build())
                .build();
        assertThat(validator.validate(new ValidationContext(doc, "strict", "test"))).isEmpty();
    }

    @Test
    @DisplayName("should detect missing initial state")
    void detectMissingInitialState() {
        var doc = buildDocWithStateMachine(
                OntologyExchangeDocument.StateMachine.builder()
                        .id("sm-1")
                        .entity("production-order")
                        .statusField("status")
                        .states(List.of(
                                OntologyExchangeDocument.State.builder()
                                        .id("st-a").name("A").build()))
                        .build());
        var issues = validator.validate(new ValidationContext(doc, "strict", "test"));
        assertThat(issues).anyMatch(i -> "V-LC-05".equals(i.getCode()));
    }

    @Test
    @DisplayName("should detect transition referencing unknown state")
    void detectInvalidTransition() {
        var doc = buildDocWithStateMachine(
                OntologyExchangeDocument.StateMachine.builder()
                        .id("sm-1")
                        .entity("production-order")
                        .statusField("status")
                        .states(List.of(
                                OntologyExchangeDocument.State.builder()
                                        .id("st-a").name("A").isInitial(true).build()))
                        .transitions(List.of(
                                OntologyExchangeDocument.Transition.builder()
                                        .id("tr-1")
                                        .from("st-a")
                                        .to("st-missing")
                                        .build()))
                        .build());
        var issues = validator.validate(new ValidationContext(doc, "strict", "test"));
        assertThat(issues).anyMatch(i -> "V-LC-07".equals(i.getCode()));
    }

    @Test
    @DisplayName("should detect unknown preCondition rule on transition")
    void detectUnknownPreCondition() {
        var doc = buildDocWithStateMachine(
                OntologyExchangeDocument.StateMachine.builder()
                        .id("sm-1")
                        .entity("production-order")
                        .statusField("status")
                        .states(List.of(
                                OntologyExchangeDocument.State.builder()
                                        .id("st-a").name("A").isInitial(true).build(),
                                OntologyExchangeDocument.State.builder()
                                        .id("st-b").name("B").isFinal(true).build()))
                        .transitions(List.of(
                                OntologyExchangeDocument.Transition.builder()
                                        .id("tr-1")
                                        .from("st-a")
                                        .to("st-b")
                                        .preConditions(List.of("rule-unknown"))
                                        .build()))
                        .build());
        var issues = validator.validate(new ValidationContext(doc, "strict", "test"));
        assertThat(issues).anyMatch(i -> "V-LC-10".equals(i.getCode()));
    }

    @Test
    @DisplayName("should pass valid production-order lifecycle")
    void passValidLifecycle() {
        var doc = buildDocWithStateMachine(
                OntologyExchangeDocument.StateMachine.builder()
                        .id("sm-production-order")
                        .entity("production-order")
                        .statusField("status")
                        .states(List.of(
                                OntologyExchangeDocument.State.builder()
                                        .id("state-created")
                                        .name("创建")
                                        .isInitial(true)
                                        .availableActions(List.of("action-release-order"))
                                        .semanticTag("created")
                                        .build(),
                                OntologyExchangeDocument.State.builder()
                                        .id("state-released")
                                        .name("已下达")
                                        .build(),
                                OntologyExchangeDocument.State.builder()
                                        .id("state-tech-closed")
                                        .name("技术关闭")
                                        .isFinal(true)
                                        .build()))
                        .transitions(List.of(
                                OntologyExchangeDocument.Transition.builder()
                                        .id("trans-release")
                                        .from("state-created")
                                        .to("state-released")
                                        .preConditions(List.of("rule-kitting"))
                                        .publishEventId("evt-order-released")
                                        .build()))
                        .build());
        doc.getSpec().getProject().getBehaviorModel().setActions(List.of(
                OntologyExchangeDocument.Action.builder()
                        .id("action-release-order")
                        .targetEntityId("production-order")
                        .preConditions(List.of("rule-kitting"))
                        .build()));
        doc.getSpec().getProject().setEventModel(OntologyExchangeDocument.EventModel.builder()
                .events(List.of(OntologyExchangeDocument.EventDefinition.builder()
                        .id("evt-order-released")
                        .entity("production-order")
                        .build()))
                .build());

        assertThat(validator.validate(new ValidationContext(doc, "strict", "test"))).isEmpty();
    }

    @Test
    @DisplayName("should detect unknown entity in spec.lifecycle")
    void detectUnknownLifecycleEntity() {
        var doc = buildDocWithStateMachine(null);
        doc.getSpec().setLifecycle(OntologyExchangeDocument.LifecycleSpec.builder()
                .byEntityId(Map.of("unknown-entity",
                        OntologyExchangeDocument.EntityLifecycleEntry.builder()
                                .entityId("unknown-entity")
                                .build()))
                .build());
        var issues = validator.validate(new ValidationContext(doc, "strict", "test"));
        assertThat(issues).anyMatch(i -> "V-LC-15".equals(i.getCode()));
    }

    private OntologyExchangeDocument buildDocWithStateMachine(
            OntologyExchangeDocument.StateMachine stateMachine) {
        var entity = OntologyExchangeDocument.Entity.builder()
                .id("production-order")
                .name("生产订单")
                .attributes(List.of(
                        OntologyExchangeDocument.Attribute.builder()
                                .nameEn("status")
                                .dataType("enum")
                                .build()))
                .build();

        var behaviorBuilder = OntologyExchangeDocument.BehaviorModel.builder()
                .actions(List.of());

        if (stateMachine != null) {
            behaviorBuilder.stateMachines(List.of(stateMachine));
        }

        return OntologyExchangeDocument.builder()
                .spec(OntologyExchangeDocument.Spec.builder()
                        .project(OntologyExchangeDocument.OntologyProject.builder()
                                .dataModel(OntologyExchangeDocument.DataModel.builder()
                                        .entities(List.of(entity))
                                        .build())
                                .behaviorModel(behaviorBuilder.build())
                                .ruleModel(OntologyExchangeDocument.RuleModel.builder()
                                        .rules(List.of(OntologyExchangeDocument.Rule.builder()
                                                .id("rule-kitting")
                                                .name("齐套")
                                                .build()))
                                        .build())
                                .build())
                        .build())
                .build();
    }
}
