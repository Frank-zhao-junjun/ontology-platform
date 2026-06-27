package com.ontology.platform.infrastructure.validation;

import com.ontology.platform.domain.dto.imports.OntologyExchangeDocument;
import com.ontology.platform.domain.service.validation.ValidationContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StructuralValidatorTest {

    private final StructuralValidator validator = new StructuralValidator();

    // ──────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────

    /** Minimal valid doc: apiVersion correct, metadata with semver, one aggregate_root entity. */
    private OntologyExchangeDocument createBaseDoc() {
        return createBaseDocWithEntities("entity-1");
    }

    private OntologyExchangeDocument createBaseDocWithEntities(String... entityIds) {
        var doc = new OntologyExchangeDocument();
        doc.setApiVersion("ontology.platform/v2");
        doc.setMetadata(OntologyExchangeDocument.Metadata.builder()
                .version("1.0.0").projectId("test").build());
        doc.setSpec(new OntologyExchangeDocument.Spec());
        var project = new OntologyExchangeDocument.OntologyProject();
        doc.getSpec().setProject(project);

        if (entityIds.length > 0) {
            var dm = new OntologyExchangeDocument.DataModel();
            dm.setEntities(List.of(entityIds).stream()
                    .map(id -> OntologyExchangeDocument.Entity.builder()
                            .id(id).name(id).entityRole("aggregate_root")
                            .attributes(List.of(
                                    OntologyExchangeDocument.Attribute.builder()
                                            .id(id + "-status").name("status").dataType("STRING").build()))
                            .build())
                    .toList());
            project.setDataModel(dm);
        } else {
            // empty data model with no entities
            project.setDataModel(new OntologyExchangeDocument.DataModel());
        }
        return doc;
    }

    private ValidationContext ctx(OntologyExchangeDocument doc) {
        return new ValidationContext(doc, "strict", "test");
    }

    // ══════════════════════════════════════════════
    // Null / missing document guards (STR-00)
    // ══════════════════════════════════════════════

    @Test
    @DisplayName("STR-00: null document → error")
    void nullDocument_returnsSTR00() {
        var ctx = new ValidationContext(null, "strict", "test");
        var issues = validator.validate(ctx);
        assertThat(issues).anyMatch(i -> "STR-00".equals(i.getCode()));
    }

    @Test
    @DisplayName("null metadata → V02 skipped, no NPE")
    void nullMetadata_skipsV02() {
        var doc = createBaseDoc();
        doc.setMetadata(null);
        var issues = validator.validate(ctx(doc));
        assertThat(issues).noneMatch(i -> "STR-02".equals(i.getCode()));
    }

    @Test
    @DisplayName("null spec → V03-V11 skipped, no NPE")
    void nullSpec_skipsAllProjectRules() {
        var doc = createBaseDoc();
        doc.setSpec(null);
        var issues = validator.validate(ctx(doc));
        // Only V01 can fire if apiVersion is wrong, but here it's correct
        assertThat(issues).noneMatch(i -> i.getCode().startsWith("STR-0")
                && !"STR-01".equals(i.getCode()));
    }

    @Test
    @DisplayName("null project → V03-V11 skipped, no NPE")
    void nullProject_skipsAllProjectRules() {
        var doc = createBaseDoc();
        doc.getSpec().setProject(null);
        var issues = validator.validate(ctx(doc));
        assertThat(issues).isEmpty();
    }

    // ══════════════════════════════════════════════
    // V01 / STR-01: apiVersion must be ontology.platform/v2
    // ══════════════════════════════════════════════

    @Test
    @DisplayName("V-01: valid apiVersion → no issue")
    void validApiVersion_noIssue() {
        var doc = createBaseDoc();
        doc.setApiVersion("ontology.platform/v2");
        var issues = validator.validate(ctx(doc));
        assertThat(issues).noneMatch(i -> "STR-01".equals(i.getCode()));
    }

    @Test
    @DisplayName("V-01: null apiVersion → STR-01 error")
    void nullApiVersion_issue() {
        var doc = createBaseDoc();
        doc.setApiVersion(null);
        var issues = validator.validate(ctx(doc));
        assertThat(issues).anyMatch(i -> "STR-01".equals(i.getCode()));
    }

    @Test
    @DisplayName("V-01: wrong apiVersion → STR-01 error")
    void wrongApiVersion_issue() {
        var doc = createBaseDoc();
        doc.setApiVersion("ontology.platform/v1");
        var issues = validator.validate(ctx(doc));
        assertThat(issues).anyMatch(i -> "STR-01".equals(i.getCode()));
    }

    // ══════════════════════════════════════════════
    // V02 / STR-02: metadata.version must be semver
    // ══════════════════════════════════════════════

    @Test
    @DisplayName("V-02: valid semver → no issue")
    void validSemver_noIssue() {
        var doc = createBaseDoc();
        doc.getMetadata().setVersion("2.3.4");
        var issues = validator.validate(ctx(doc));
        assertThat(issues).noneMatch(i -> "STR-02".equals(i.getCode()));
    }

    @Test
    @DisplayName("V-02: null version → STR-02 error")
    void nullVersion_issue() {
        var doc = createBaseDoc();
        doc.getMetadata().setVersion(null);
        var issues = validator.validate(ctx(doc));
        assertThat(issues).anyMatch(i -> "STR-02".equals(i.getCode()));
    }

    @Test
    @DisplayName("V-02: non-semver version → STR-02 error")
    void nonSemverVersion_issue() {
        var doc = createBaseDoc();
        doc.getMetadata().setVersion("abc");
        var issues = validator.validate(ctx(doc));
        assertThat(issues).anyMatch(i -> "STR-02".equals(i.getCode()));
    }

    // ══════════════════════════════════════════════
    // V03 / STR-03: at least one aggregate_root entity
    // ══════════════════════════════════════════════

    @Test
    @DisplayName("V-03: entity with aggregate_root → no issue")
    void hasAggregateRoot_noIssue() {
        var doc = createBaseDoc(); // already has entity with aggregate_root
        var issues = validator.validate(ctx(doc));
        assertThat(issues).noneMatch(i -> "STR-03".equals(i.getCode()));
    }

    @Test
    @DisplayName("V-03: no entities at all → STR-03 error")
    void noEntities_issue() {
        var doc = createBaseDocWithEntities(); // empty data model
        var issues = validator.validate(ctx(doc));
        assertThat(issues).anyMatch(i -> "STR-03".equals(i.getCode()));
    }

    @Test
    @DisplayName("V-03: entities exist but none with aggregate_root → STR-03 error")
    void noAggregateRootAmongEntities_issue() {
        var doc = createBaseDocWithEntities("child-1");
        doc.getSpec().getProject().getDataModel().getEntities().get(0).setEntityRole("child_entity");
        var issues = validator.validate(ctx(doc));
        assertThat(issues).anyMatch(i -> "STR-03".equals(i.getCode()));
    }

    @Test
    @DisplayName("V-03: null DataModel → STR-03 error")
    void nullDataModel_issue() {
        var doc = createBaseDoc();
        doc.getSpec().getProject().setDataModel(null);
        var issues = validator.validate(ctx(doc));
        assertThat(issues).anyMatch(i -> "STR-03".equals(i.getCode()));
    }

    @Test
    @DisplayName("V-03: null entities list → STR-03 error")
    void nullEntities_issue() {
        var doc = createBaseDoc();
        doc.getSpec().getProject().getDataModel().setEntities(null);
        var issues = validator.validate(ctx(doc));
        assertThat(issues).anyMatch(i -> "STR-03".equals(i.getCode()));
    }

    // ══════════════════════════════════════════════
    // V04 / STR-04: child_entity parentAggregateId ref
    // ══════════════════════════════════════════════

    @Test
    @DisplayName("V-04: child_entity with valid parentAggregateId → no issue")
    void validChildEntityRef_noIssue() {
        var doc = createBaseDocWithEntities("parent-1", "child-1");
        var entities = doc.getSpec().getProject().getDataModel().getEntities();
        // find child-1 and set it up
        entities.stream().filter(e -> "child-1".equals(e.getId())).findFirst().ifPresent(e -> {
            e.setEntityRole("child_entity");
            e.setParentAggregateId("parent-1");
        });
        var issues = validator.validate(ctx(doc));
        assertThat(issues).noneMatch(i -> "STR-04".equals(i.getCode()));
    }

    @Test
    @DisplayName("V-04: child_entity with null parentAggregateId → STR-04 error")
    void childEntityNullParentRef_issue() {
        var doc = createBaseDocWithEntities("parent-1", "child-1");
        var entities = doc.getSpec().getProject().getDataModel().getEntities();
        entities.stream().filter(e -> "child-1".equals(e.getId())).findFirst().ifPresent(e -> {
            e.setEntityRole("child_entity");
            e.setParentAggregateId(null);
        });
        var issues = validator.validate(ctx(doc));
        assertThat(issues).anyMatch(i -> "STR-04".equals(i.getCode()));
    }

    @Test
    @DisplayName("V-04: child_entity referencing non-existent entity → STR-04 error")
    void childEntityBadParentRef_issue() {
        var doc = createBaseDocWithEntities("parent-1", "child-1");
        var entities = doc.getSpec().getProject().getDataModel().getEntities();
        entities.stream().filter(e -> "child-1".equals(e.getId())).findFirst().ifPresent(e -> {
            e.setEntityRole("child_entity");
            e.setParentAggregateId("non-existent-parent");
        });
        var issues = validator.validate(ctx(doc));
        assertThat(issues).anyMatch(i -> "STR-04".equals(i.getCode()));
    }

    @Test
    @DisplayName("V-04: null DataModel → no STR-04 (safe skip)")
    void nullDataModel_skipsV04() {
        var doc = createBaseDoc();
        doc.getSpec().getProject().setDataModel(null);
        var issues = validator.validate(ctx(doc));
        assertThat(issues).noneMatch(i -> "STR-04".equals(i.getCode()));
    }

    // ══════════════════════════════════════════════
    // V05 / STR-05: Action targetEntityId ref
    // ══════════════════════════════════════════════

    @Test
    @DisplayName("V-05: action with valid targetEntityId → no issue")
    void validActionEntityRef_noIssue() {
        var doc = createBaseDocWithEntities("order");
        var bm = new OntologyExchangeDocument.BehaviorModel();
        bm.setActions(List.of(OntologyExchangeDocument.Action.builder()
                .id("act-1").name("Act").targetEntityId("order").actionType("custom").build()));
        doc.getSpec().getProject().setBehaviorModel(bm);
        var issues = validator.validate(ctx(doc));
        assertThat(issues).noneMatch(i -> "STR-05".equals(i.getCode()));
    }

    @Test
    @DisplayName("V-05: action with null targetEntityId → no issue (null is ok)")
    void nullTargetEntityId_noIssue() {
        var doc = createBaseDocWithEntities("order");
        var bm = new OntologyExchangeDocument.BehaviorModel();
        bm.setActions(List.of(OntologyExchangeDocument.Action.builder()
                .id("act-1").name("Act").actionType("custom").build()));
        doc.getSpec().getProject().setBehaviorModel(bm);
        var issues = validator.validate(ctx(doc));
        assertThat(issues).noneMatch(i -> "STR-05".equals(i.getCode()));
    }

    @Test
    @DisplayName("V-05: action referencing unknown entity → STR-05 error")
    void invalidActionEntityRef_issue() {
        var doc = createBaseDocWithEntities("order");
        var bm = new OntologyExchangeDocument.BehaviorModel();
        bm.setActions(List.of(OntologyExchangeDocument.Action.builder()
                .id("act-1").name("Act").targetEntityId("ghost-entity").actionType("custom").build()));
        doc.getSpec().getProject().setBehaviorModel(bm);
        var issues = validator.validate(ctx(doc));
        assertThat(issues).anyMatch(i -> "STR-05".equals(i.getCode()));
    }

    @Test
    @DisplayName("V-05: null BehaviorModel → no STR-05 (safe skip)")
    void nullBehaviorModel_skipsV05() {
        var doc = createBaseDoc();
        doc.getSpec().getProject().setBehaviorModel(null);
        var issues = validator.validate(ctx(doc));
        assertThat(issues).noneMatch(i -> "STR-05".equals(i.getCode()));
    }

    // ══════════════════════════════════════════════
    // V06 / STR-06: Action preConditions ref existing rules
    // ══════════════════════════════════════════════

    @Test
    @DisplayName("V-06: preCondition referencing existing rule → no issue")
    void validPreConditionRef_noIssue() {
        var doc = createBaseDocWithEntities("order");
        var bm = new OntologyExchangeDocument.BehaviorModel();
        bm.setActions(List.of(OntologyExchangeDocument.Action.builder()
                .id("act-1").name("Act").actionType("custom")
                .preConditions(List.of("rule-1"))
                .build()));
        doc.getSpec().getProject().setBehaviorModel(bm);
        var rm = new OntologyExchangeDocument.RuleModel();
        rm.setRules(List.of(OntologyExchangeDocument.Rule.builder()
                .id("rule-1").name("Rule1").build()));
        doc.getSpec().getProject().setRuleModel(rm);
        var issues = validator.validate(ctx(doc));
        assertThat(issues).noneMatch(i -> "STR-06".equals(i.getCode()));
    }

    @Test
    @DisplayName("V-06: preCondition referencing non-existent rule → STR-06 error")
    void invalidPreConditionRef_issue() {
        var doc = createBaseDocWithEntities("order");
        var bm = new OntologyExchangeDocument.BehaviorModel();
        bm.setActions(List.of(OntologyExchangeDocument.Action.builder()
                .id("act-1").name("Act").actionType("custom")
                .preConditions(List.of("ghost-rule"))
                .build()));
        doc.getSpec().getProject().setBehaviorModel(bm);
        var rm = new OntologyExchangeDocument.RuleModel();
        rm.setRules(List.of(OntologyExchangeDocument.Rule.builder()
                .id("rule-1").name("Rule1").build()));
        doc.getSpec().getProject().setRuleModel(rm);
        var issues = validator.validate(ctx(doc));
        assertThat(issues).anyMatch(i -> "STR-06".equals(i.getCode()));
    }

    @Test
    @DisplayName("V-06: no preConditions → no issue")
    void noPreConditions_noIssue() {
        var doc = createBaseDocWithEntities("order");
        var bm = new OntologyExchangeDocument.BehaviorModel();
        bm.setActions(List.of(OntologyExchangeDocument.Action.builder()
                .id("act-1").name("Act").actionType("custom").build()));
        doc.getSpec().getProject().setBehaviorModel(bm);
        var issues = validator.validate(ctx(doc));
        assertThat(issues).noneMatch(i -> "STR-06".equals(i.getCode()));
    }

    @Test
    @DisplayName("V-06: null RuleModel → no STR-06 (safe skip)")
    void nullRuleModel_skipsV06() {
        var doc = createBaseDocWithEntities("order");
        var bm = new OntologyExchangeDocument.BehaviorModel();
        bm.setActions(List.of(OntologyExchangeDocument.Action.builder()
                .id("act-1").name("Act").actionType("custom")
                .preConditions(List.of("ghost-rule"))
                .build()));
        doc.getSpec().getProject().setBehaviorModel(bm);
        doc.getSpec().getProject().setRuleModel(null);
        var issues = validator.validate(ctx(doc));
        assertThat(issues).noneMatch(i -> "STR-06".equals(i.getCode()));
    }

    // ══════════════════════════════════════════════
    // V07 / STR-07: Transition publishEventId references
    // ══════════════════════════════════════════════

    @Test
    @DisplayName("V-07: transition with valid publishEventId → no issue")
    void validEventRef_noIssue() {
        var doc = createBaseDocWithEntities("order");
        var em = new OntologyExchangeDocument.EventModel();
        em.setEvents(List.of(OntologyExchangeDocument.EventDefinition.builder()
                .id("evt-1").nameEn("orderCreated").isDomainEvent(true).build()));
        doc.getSpec().getProject().setEventModel(em);
        var bm = new OntologyExchangeDocument.BehaviorModel();
        var sm = OntologyExchangeDocument.StateMachine.builder()
                .id("sm-1").name("SM").entity("order")
                .states(List.of(
                        OntologyExchangeDocument.State.builder().id("s1").name("Init").isInitial(true).build(),
                        OntologyExchangeDocument.State.builder().id("s2").name("Done").isFinal(true).build()
                ))
                .transitions(List.of(OntologyExchangeDocument.Transition.builder()
                        .id("t-1").name("Proc").from("s1").to("s2").trigger("manual")
                        .publishEventId("evt-1").build()))
                .build();
        bm.setStateMachines(List.of(sm));
        doc.getSpec().getProject().setBehaviorModel(bm);
        var issues = validator.validate(ctx(doc));
        assertThat(issues).noneMatch(i -> "STR-07".equals(i.getCode()));
    }

    @Test
    @DisplayName("V-07: transition with null publishEventId → no issue")
    void nullPublishEventId_noIssue() {
        var doc = createBaseDocWithEntities("order");
        var em = new OntologyExchangeDocument.EventModel();
        em.setEvents(List.of(OntologyExchangeDocument.EventDefinition.builder()
                .id("evt-1").nameEn("orderCreated").isDomainEvent(true).build()));
        doc.getSpec().getProject().setEventModel(em);
        var bm = new OntologyExchangeDocument.BehaviorModel();
        var sm = OntologyExchangeDocument.StateMachine.builder()
                .id("sm-1").name("SM").entity("order")
                .states(List.of(
                        OntologyExchangeDocument.State.builder().id("s1").name("Init").isInitial(true).build(),
                        OntologyExchangeDocument.State.builder().id("s2").name("Done").isFinal(true).build()
                ))
                .transitions(List.of(OntologyExchangeDocument.Transition.builder()
                        .id("t-1").name("Proc").from("s1").to("s2").trigger("manual").build()))
                .build();
        bm.setStateMachines(List.of(sm));
        doc.getSpec().getProject().setBehaviorModel(bm);
        var issues = validator.validate(ctx(doc));
        assertThat(issues).noneMatch(i -> "STR-07".equals(i.getCode()));
    }

    @Test
    @DisplayName("V-07: transition with non-existent publishEventId → STR-07 warning")
    void invalidEventRef_issue() {
        var doc = createBaseDocWithEntities("order");
        var em = new OntologyExchangeDocument.EventModel();
        em.setEvents(List.of(OntologyExchangeDocument.EventDefinition.builder()
                .id("evt-1").nameEn("orderCreated").isDomainEvent(true).build()));
        doc.getSpec().getProject().setEventModel(em);
        var bm = new OntologyExchangeDocument.BehaviorModel();
        var sm = OntologyExchangeDocument.StateMachine.builder()
                .id("sm-1").name("SM").entity("order")
                .states(List.of(
                        OntologyExchangeDocument.State.builder().id("s1").name("Init").isInitial(true).build(),
                        OntologyExchangeDocument.State.builder().id("s2").name("Done").isFinal(true).build()
                ))
                .transitions(List.of(OntologyExchangeDocument.Transition.builder()
                        .id("t-1").name("Proc").from("s1").to("s2").trigger("manual")
                        .publishEventId("ghost-event").build()))
                .build();
        bm.setStateMachines(List.of(sm));
        doc.getSpec().getProject().setBehaviorModel(bm);
        var issues = validator.validate(ctx(doc));
        assertThat(issues).anyMatch(i -> "STR-07".equals(i.getCode()));
    }

    @Test
    @DisplayName("V-07: null EventModel → no STR-07 (safe skip)")
    void nullEventModel_skipsV07() {
        var doc = createBaseDocWithEntities("order");
        var bm = new OntologyExchangeDocument.BehaviorModel();
        bm.setStateMachines(List.of(OntologyExchangeDocument.StateMachine.builder()
                .id("sm-1").name("SM").entity("order")
                .states(List.of(OntologyExchangeDocument.State.builder().id("s1").name("Init").isInitial(true).build()))
                .transitions(List.of(OntologyExchangeDocument.Transition.builder()
                        .id("t-1").name("Proc").from("s1").to("s1").trigger("manual")
                        .publishEventId("ghost-event").build()))
                .build()));
        doc.getSpec().getProject().setBehaviorModel(bm);
        doc.getSpec().getProject().setEventModel(null);
        var issues = validator.validate(ctx(doc));
        assertThat(issues).noneMatch(i -> "STR-07".equals(i.getCode()));
    }

    // ══════════════════════════════════════════════
    // V08 / STR-08: Event nameEn past tense
    // ══════════════════════════════════════════════

    @Test
    @DisplayName("V-08: event nameEn ending with past tense → no issue")
    void pastTenseEventName_noIssue() {
        var doc = createBaseDocWithEntities("order");
        var em = new OntologyExchangeDocument.EventModel();
        em.setEvents(List.of(
                OntologyExchangeDocument.EventDefinition.builder()
                        .id("evt-1").nameEn("orderCreated").isDomainEvent(true).build(),
                OntologyExchangeDocument.EventDefinition.builder()
                        .id("evt-2").nameEn("orderShipped").isDomainEvent(true).build(),
                OntologyExchangeDocument.EventDefinition.builder()
                        .id("evt-3").nameEn("paid").isDomainEvent(true).build()
        ));
        doc.getSpec().getProject().setEventModel(em);
        var issues = validator.validate(ctx(doc));
        assertThat(issues).noneMatch(i -> "STR-08".equals(i.getCode()));
    }

    @Test
    @DisplayName("V-08: event nameEn not ending with past tense → STR-08 warning")
    void nonPastTenseEventName_issue() {
        var doc = createBaseDocWithEntities("order");
        var em = new OntologyExchangeDocument.EventModel();
        em.setEvents(List.of(
                OntologyExchangeDocument.EventDefinition.builder()
                        .id("evt-1").nameEn("createOrder").isDomainEvent(true).build()
        ));
        doc.getSpec().getProject().setEventModel(em);
        var issues = validator.validate(ctx(doc));
        assertThat(issues).anyMatch(i -> "STR-08".equals(i.getCode()));
    }

    @Test
    @DisplayName("V-08: event with null nameEn → no issue (skipped)")
    void nullEventName_noIssue() {
        var doc = createBaseDocWithEntities("order");
        var em = new OntologyExchangeDocument.EventModel();
        em.setEvents(List.of(
                OntologyExchangeDocument.EventDefinition.builder()
                        .id("evt-1").isDomainEvent(true).build()
        ));
        doc.getSpec().getProject().setEventModel(em);
        var issues = validator.validate(ctx(doc));
        assertThat(issues).noneMatch(i -> "STR-08".equals(i.getCode()));
    }

    @Test
    @DisplayName("V-08: null EventModel → no STR-08 (safe skip)")
    void nullEventModel_skipsV08() {
        var doc = createBaseDoc();
        doc.getSpec().getProject().setEventModel(null);
        var issues = validator.validate(ctx(doc));
        assertThat(issues).noneMatch(i -> "STR-08".equals(i.getCode()));
    }

    // ══════════════════════════════════════════════
    // V09 / STR-09: state machine must have exactly one initial
    // ══════════════════════════════════════════════

    @Test
    @DisplayName("V-09: state machine with exactly one initial state → no issue")
    void exactlyOneInitialState_noIssue() {
        var doc = createBaseDocWithEntities("order");
        var bm = new OntologyExchangeDocument.BehaviorModel();
        var sm = OntologyExchangeDocument.StateMachine.builder()
                .id("sm-1").name("SM").entity("order")
                .states(List.of(
                        OntologyExchangeDocument.State.builder().id("s1").name("Init").isInitial(true).build(),
                        OntologyExchangeDocument.State.builder().id("s2").name("Done").isFinal(true).build()
                ))
                .build();
        bm.setStateMachines(List.of(sm));
        doc.getSpec().getProject().setBehaviorModel(bm);
        var issues = validator.validate(ctx(doc));
        assertThat(issues).noneMatch(i -> "STR-09".equals(i.getCode()));
    }

    @Test
    @DisplayName("V-09: state machine with 0 initial states → STR-09 error")
    void zeroInitialStates_issue() {
        var doc = createBaseDocWithEntities("order");
        var bm = new OntologyExchangeDocument.BehaviorModel();
        var sm = OntologyExchangeDocument.StateMachine.builder()
                .id("sm-1").name("SM").entity("order")
                .states(List.of(
                        OntologyExchangeDocument.State.builder().id("s1").name("Init").build(),
                        OntologyExchangeDocument.State.builder().id("s2").name("Done").isFinal(true).build()
                ))
                .build();
        bm.setStateMachines(List.of(sm));
        doc.getSpec().getProject().setBehaviorModel(bm);
        var issues = validator.validate(ctx(doc));
        assertThat(issues).anyMatch(i -> "STR-09".equals(i.getCode()));
    }

    @Test
    @DisplayName("V-09: state machine with 2 initial states → STR-09 error")
    void multipleInitialStates_issue() {
        var doc = createBaseDocWithEntities("order");
        var bm = new OntologyExchangeDocument.BehaviorModel();
        var sm = OntologyExchangeDocument.StateMachine.builder()
                .id("sm-1").name("SM").entity("order")
                .states(List.of(
                        OntologyExchangeDocument.State.builder().id("s1").name("Init").isInitial(true).build(),
                        OntologyExchangeDocument.State.builder().id("s2").name("AlsoInit").isInitial(true).build()
                ))
                .build();
        bm.setStateMachines(List.of(sm));
        doc.getSpec().getProject().setBehaviorModel(bm);
        var issues = validator.validate(ctx(doc));
        assertThat(issues).anyMatch(i -> "STR-09".equals(i.getCode()));
    }

    @Test
    @DisplayName("V-09: null states list → skipped, no issue")
    void nullStates_skipsV09() {
        var doc = createBaseDocWithEntities("order");
        var bm = new OntologyExchangeDocument.BehaviorModel();
        var sm = OntologyExchangeDocument.StateMachine.builder()
                .id("sm-1").name("SM").entity("order")
                .build();
        // states will be null since @Builder.Default sets empty list, need to force null
        sm.setStates(null);
        bm.setStateMachines(List.of(sm));
        doc.getSpec().getProject().setBehaviorModel(bm);
        var issues = validator.validate(ctx(doc));
        assertThat(issues).noneMatch(i -> "STR-09".equals(i.getCode()));
    }

    @Test
    @DisplayName("V-09: empty states list → skipped, no issue")
    void emptyStates_skipsV09() {
        var doc = createBaseDocWithEntities("order");
        var bm = new OntologyExchangeDocument.BehaviorModel();
        var sm = OntologyExchangeDocument.StateMachine.builder()
                .id("sm-1").name("SM").entity("order")
                .states(List.of())
                .build();
        bm.setStateMachines(List.of(sm));
        doc.getSpec().getProject().setBehaviorModel(bm);
        var issues = validator.validate(ctx(doc));
        assertThat(issues).noneMatch(i -> "STR-09".equals(i.getCode()));
    }

    @Test
    @DisplayName("V-09: null BehaviorModel → no STR-09 (safe skip)")
    void nullBehaviorModel_skipsV09() {
        var doc = createBaseDoc();
        doc.getSpec().getProject().setBehaviorModel(null);
        var issues = validator.validate(ctx(doc));
        assertThat(issues).noneMatch(i -> "STR-09".equals(i.getCode()));
    }

    // ══════════════════════════════════════════════
    // V10 / STR-10: DataSources must not expose credentials
    // ══════════════════════════════════════════════

    @Test
    @DisplayName("V-10: DataSource with safe name → no issue")
    void safeDataSourceName_noIssue() {
        var doc = createBaseDocWithEntities("order");
        var dsm = new OntologyExchangeDocument.DataSourcesModel();
        dsm.setSources(List.of(
                OntologyExchangeDocument.DataSource.builder()
                        .id("ds-1").name("my-database").type("postgres").build()
        ));
        doc.getSpec().getProject().setDataSourcesModel(dsm);
        var issues = validator.validate(ctx(doc));
        assertThat(issues).noneMatch(i -> "STR-10".equals(i.getCode()));
    }

    @Test
    @DisplayName("V-10: DataSource name with credential pattern → STR-10 warning")
    void credentialDataSourceName_issue() {
        var doc = createBaseDocWithEntities("order");
        var dsm = new OntologyExchangeDocument.DataSourcesModel();
        dsm.setSources(List.of(
                OntologyExchangeDocument.DataSource.builder()
                        .id("ds-1").name("my_secret_key").type("postgres").build()
        ));
        doc.getSpec().getProject().setDataSourcesModel(dsm);
        var issues = validator.validate(ctx(doc));
        assertThat(issues).anyMatch(i -> "STR-10".equals(i.getCode()));
    }

    @Test
    @DisplayName("V-10: DataSource with password-like name → STR-10 warning")
    void passwordDataSourceName_issue() {
        var doc = createBaseDocWithEntities("order");
        var dsm = new OntologyExchangeDocument.DataSourcesModel();
        dsm.setSources(List.of(
                OntologyExchangeDocument.DataSource.builder()
                        .id("ds-1").name("db-password").type("postgres").build()
        ));
        doc.getSpec().getProject().setDataSourcesModel(dsm);
        var issues = validator.validate(ctx(doc));
        assertThat(issues).anyMatch(i -> "STR-10".equals(i.getCode()));
    }

    @Test
    @DisplayName("V-10: DataSource with api.authSecretRef → STR-10 warning")
    void authSecretRef_issue() {
        var doc = createBaseDocWithEntities("order");
        var dsm = new OntologyExchangeDocument.DataSourcesModel();
        var api = OntologyExchangeDocument.ApiDef.builder()
                .baseUrl("https://example.com/api")
                .authSecretRef("vault://secret")
                .build();
        dsm.setSources(List.of(
                OntologyExchangeDocument.DataSource.builder()
                        .id("ds-1").name("my-api").type("rest").api(api).build()
        ));
        doc.getSpec().getProject().setDataSourcesModel(dsm);
        var issues = validator.validate(ctx(doc));
        assertThat(issues).anyMatch(i -> "STR-10".equals(i.getCode()));
    }

    @Test
    @DisplayName("V-10: no DataSources → no STR-10 (safe skip)")
    void nullDataSourcesModel_skipsV10() {
        var doc = createBaseDoc();
        doc.getSpec().getProject().setDataSourcesModel(null);
        var issues = validator.validate(ctx(doc));
        assertThat(issues).noneMatch(i -> "STR-10".equals(i.getCode()));
    }

    // ══════════════════════════════════════════════
    // V11 / STR-11: All IDs must be unique
    // ══════════════════════════════════════════════

    @Test
    @DisplayName("V-11: all unique IDs across sections → no issue")
    void uniqueIds_noIssue() {
        var doc = createBaseDocWithEntities("entity-1");
        var project = doc.getSpec().getProject();
        // Add entity attribute
        project.getDataModel().getEntities().get(0).setAttributes(List.of(
                OntologyExchangeDocument.Attribute.builder().id("attr-1").name("status").dataType("STRING").build()
        ));
        // Add action
        var bm = new OntologyExchangeDocument.BehaviorModel();
        bm.setActions(List.of(OntologyExchangeDocument.Action.builder()
                .id("action-1").name("Act").actionType("custom").build()));
        project.setBehaviorModel(bm);
        // Add rule
        var rm = new OntologyExchangeDocument.RuleModel();
        rm.setRules(List.of(OntologyExchangeDocument.Rule.builder().id("rule-1").name("Rule1").build()));
        project.setRuleModel(rm);
        // Add event
        var em = new OntologyExchangeDocument.EventModel();
        em.setEvents(List.of(OntologyExchangeDocument.EventDefinition.builder()
                .id("event-1").nameEn("created").isDomainEvent(true).build()));
        project.setEventModel(em);
        // Add state + transition via SM
        var sm = OntologyExchangeDocument.StateMachine.builder()
                .id("sm-1").name("SM").entity("entity-1")
                .states(List.of(
                        OntologyExchangeDocument.State.builder().id("state-1").name("Init").isInitial(true).build()
                ))
                .transitions(List.of(
                        OntologyExchangeDocument.Transition.builder().id("trans-1").name("Go").from("state-1").to("state-1").trigger("manual").build()
                ))
                .build();
        bm.setStateMachines(List.of(sm));
        // Add role
        var gm = new OntologyExchangeDocument.GovernanceModel();
        gm.setRoles(List.of(OntologyExchangeDocument.GovernanceRole.builder().id("role-1").name("Admin").build()));
        project.setGovernanceModel(gm);
        // Add data source
        var dsm = new OntologyExchangeDocument.DataSourcesModel();
        dsm.setSources(List.of(OntologyExchangeDocument.DataSource.builder().id("ds-1").name("db").build()));
        project.setDataSourcesModel(dsm);

        var issues = validator.validate(ctx(doc));
        assertThat(issues).noneMatch(i -> "STR-11".equals(i.getCode()));
    }

    @Test
    @DisplayName("V-11: duplicate ID between entity and action → STR-11 error")
    void duplicateEntityAndActionId_issue() {
        var doc = createBaseDocWithEntities("duplicate-id");
        var project = doc.getSpec().getProject();
        var bm = new OntologyExchangeDocument.BehaviorModel();
        bm.setActions(List.of(OntologyExchangeDocument.Action.builder()
                .id("duplicate-id").name("Act").actionType("custom").build()));
        project.setBehaviorModel(bm);
        var issues = validator.validate(ctx(doc));
        assertThat(issues).anyMatch(i -> "STR-11".equals(i.getCode()));
    }

    @Test
    @DisplayName("V-11: duplicate ID between entity and rule → STR-11 error")
    void duplicateEntityAndRuleId_issue() {
        var doc = createBaseDocWithEntities("duplicate-id");
        var project = doc.getSpec().getProject();
        var rm = new OntologyExchangeDocument.RuleModel();
        rm.setRules(List.of(OntologyExchangeDocument.Rule.builder().id("duplicate-id").name("Rule1").build()));
        project.setRuleModel(rm);
        var issues = validator.validate(ctx(doc));
        assertThat(issues).anyMatch(i -> "STR-11".equals(i.getCode()));
    }

    @Test
    @DisplayName("V-11: null sections still work → no NPE")
    void nullSections_noNpe() {
        // base doc with only entity, no other sections set
        var doc = createBaseDocWithEntities("entity-1");
        // Don't set behaviorModel, ruleModel, eventModel, etc. — all null
        var issues = validator.validate(ctx(doc));
        assertThat(issues).noneMatch(i -> "STR-11".equals(i.getCode()));
    }
}
