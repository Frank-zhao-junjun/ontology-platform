package com.ontology.platform.infrastructure.validation;

import com.ontology.platform.domain.dto.imports.OntologyExchangeDocument;
import com.ontology.platform.domain.service.validation.ValidationContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EpcVmValidatorTest {

    private final EpcVmValidator validator = new EpcVmValidator();

    private OntologyExchangeDocument createDocWithEntities(String... ids) {
        var doc = new OntologyExchangeDocument();
        doc.setMetadata(OntologyExchangeDocument.Metadata.builder().projectId("test").build());
        doc.setSpec(new OntologyExchangeDocument.Spec());
        var project = new OntologyExchangeDocument.OntologyProject();
        doc.getSpec().setProject(project);

        var dm = new OntologyExchangeDocument.DataModel();
        dm.setEntities(List.of(ids).stream()
                .map(id -> OntologyExchangeDocument.Entity.builder().id(id).name(id).entityRole("aggregate_root").businessScenarioId("s1").build())
                .toList());
        project.setDataModel(dm);
        return doc;
    }

    @Test
    @DisplayName("VM-01: StateMachine references valid entity → no issues")
    void validStateMachineEntity_noIssues() {
        var doc = createDocWithEntities("production-order");
        var bm = new OntologyExchangeDocument.BehaviorModel();
        var sm = OntologyExchangeDocument.StateMachine.builder()
                .id("sm-1").name("SM").entity("production-order").statusField("status")
                .build();
        bm.setStateMachines(List.of(sm));
        doc.getSpec().getProject().setBehaviorModel(bm);

        var ctx = new ValidationContext(doc, "strict", "test");
        assertThat(validator.validate(ctx)).isEmpty();
    }

    @Test
    @DisplayName("VM-01: StateMachine references unknown entity → error")
    void invalidStateMachineEntity_error() {
        var doc = createDocWithEntities("valid-entity");
        var bm = new OntologyExchangeDocument.BehaviorModel();
        var sm = OntologyExchangeDocument.StateMachine.builder()
                .id("sm-1").name("SM").entity("ghost-entity").statusField("status")
                .build();
        bm.setStateMachines(List.of(sm));
        doc.getSpec().getProject().setBehaviorModel(bm);

        var ctx = new ValidationContext(doc, "strict", "test");
        var issues = validator.validate(ctx);
        assertThat(issues).anyMatch(i -> "VM-01".equals(i.getCode()));
    }

    @Test
    @DisplayName("VM-02: Action references unknown entity → error")
    void invalidActionEntity_error() {
        var doc = createDocWithEntities("real-entity");
        var bm = new OntologyExchangeDocument.BehaviorModel();
        bm.setActions(List.of(OntologyExchangeDocument.Action.builder()
                .id("action-1").name("Act").targetEntityId("phantom-entity").actionType("custom").build()));
        doc.getSpec().getProject().setBehaviorModel(bm);

        var ctx = new ValidationContext(doc, "strict", "test");
        var issues = validator.validate(ctx);
        assertThat(issues).anyMatch(i -> "VM-02".equals(i.getCode()));
    }

    @Test
    @DisplayName("VM-03: Rule references unknown entity → error")
    void invalidRuleEntity_error() {
        var doc = createDocWithEntities("real-entity");
        var rm = new OntologyExchangeDocument.RuleModel();
        rm.setRules(List.of(OntologyExchangeDocument.Rule.builder()
                .id("rule-1").name("Rule").entity("ghost").build()));
        doc.getSpec().getProject().setRuleModel(rm);

        var ctx = new ValidationContext(doc, "strict", "test");
        var issues = validator.validate(ctx);
        assertThat(issues).anyMatch(i -> "VM-03".equals(i.getCode()));
    }

    @Test
    @DisplayName("VM-04: Event references unknown entity → error")
    void invalidEventEntity_error() {
        var doc = createDocWithEntities("real-entity");
        var em = new OntologyExchangeDocument.EventModel();
        em.setEvents(List.of(OntologyExchangeDocument.EventDefinition.builder()
                .id("evt-1").name("Event").entity("ghost").isDomainEvent(true).build()));
        doc.getSpec().getProject().setEventModel(em);

        var ctx = new ValidationContext(doc, "strict", "test");
        var issues = validator.validate(ctx);
        assertThat(issues).anyMatch(i -> "VM-04".equals(i.getCode()));
    }

    @Test
    @DisplayName("VM-05: Transition from/to valid states → no issues")
    void validTransition_noIssues() {
        var doc = createDocWithEntities("entity-a");
        var bm = new OntologyExchangeDocument.BehaviorModel();
        var sm = OntologyExchangeDocument.StateMachine.builder()
                .id("sm-1").name("SM").entity("entity-a").statusField("status")
                .states(List.of(
                        OntologyExchangeDocument.State.builder().id("state-1").name("初始").isInitial(true).build(),
                        OntologyExchangeDocument.State.builder().id("state-2").name("完成").isFinal(true).build()
                ))
                .transitions(List.of(OntologyExchangeDocument.Transition.builder()
                        .id("trans-1").name("执行").from("state-1").to("state-2").trigger("manual").build()))
                .build();
        bm.setStateMachines(List.of(sm));
        doc.getSpec().getProject().setBehaviorModel(bm);

        var ctx = new ValidationContext(doc, "strict", "test");
        assertThat(validator.validate(ctx)).isEmpty();
    }

    @Test
    @DisplayName("VM-05: Transition from unknown state → error")
    void invalidTransitionFrom_error() {
        var doc = createDocWithEntities("entity-a");
        var bm = new OntologyExchangeDocument.BehaviorModel();
        var sm = OntologyExchangeDocument.StateMachine.builder()
                .id("sm-1").name("SM").entity("entity-a").statusField("status")
                .states(List.of(OntologyExchangeDocument.State.builder().id("state-1").name("初始").build()))
                .transitions(List.of(OntologyExchangeDocument.Transition.builder()
                        .id("trans-1").name("坏").from("ghost-state").to("state-1").trigger("automatic").build()))
                .build();
        bm.setStateMachines(List.of(sm));
        doc.getSpec().getProject().setBehaviorModel(bm);

        var ctx = new ValidationContext(doc, "strict", "test");
        var issues = validator.validate(ctx);
        assertThat(issues).anyMatch(i -> "VM-05".equals(i.getCode()));
    }
}
