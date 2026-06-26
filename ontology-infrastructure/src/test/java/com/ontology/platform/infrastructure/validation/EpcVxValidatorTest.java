package com.ontology.platform.infrastructure.validation;

import com.ontology.platform.domain.dto.imports.OntologyExchangeDocument;
import com.ontology.platform.domain.service.validation.ValidationContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EpcVxValidator Test")
class EpcVxValidatorTest {

    private final EpcVxValidator validator = new EpcVxValidator();

    @Test
    @DisplayName("should pass when epcModel is absent")
    void passWhenNoEpcModel() {
        var doc = OntologyExchangeDocument.builder()
                .spec(OntologyExchangeDocument.Spec.builder()
                        .project(OntologyExchangeDocument.OntologyProject.builder().build())
                        .build())
                .build();
        assertThat(validator.validate(new ValidationContext(doc, "strict", "test"))).isEmpty();
    }

    @Test
    @DisplayName("VX-01: empty chain id")
    void emptyChainId() {
        var doc = buildDocWithChain(OntologyExchangeDocument.EpcChain.builder()
                .name("Chain").aggregateRootId("entity-1")
                .nodes(List.of(node("n1", "state", "st-a")))
                .build());
        var issues = validator.validate(new ValidationContext(doc, "strict", "test"));
        assertThat(issues).anyMatch(i -> "VX-01".equals(i.getCode()));
    }

    @Test
    @DisplayName("VX-03: missing aggregateRootId")
    void missingAggregateRootId() {
        var doc = buildDocWithChain(OntologyExchangeDocument.EpcChain.builder()
                .id("chain-1").name("Chain")
                .nodes(List.of(node("n1", "state", "st-a")))
                .build());
        var issues = validator.validate(new ValidationContext(doc, "strict", "test"));
        assertThat(issues).anyMatch(i -> "VX-03".equals(i.getCode()));
    }

    @Test
    @DisplayName("VX-04: unknown aggregateRootId")
    void unknownAggregateRootId() {
        var doc = buildDocWithChain(OntologyExchangeDocument.EpcChain.builder()
                .id("chain-1").name("Chain").aggregateRootId("missing-entity")
                .nodes(List.of(node("n1", "state", "st-a")))
                .build());
        var issues = validator.validate(new ValidationContext(doc, "strict", "test"));
        assertThat(issues).anyMatch(i -> "VX-04".equals(i.getCode()));
    }

    @Test
    @DisplayName("VX-05: chain without nodes")
    void chainWithoutNodes() {
        var doc = buildDocWithChain(OntologyExchangeDocument.EpcChain.builder()
                .id("chain-1").name("Chain").aggregateRootId("entity-1")
                .nodes(List.of())
                .build());
        var issues = validator.validate(new ValidationContext(doc, "strict", "test"));
        assertThat(issues).anyMatch(i -> "VX-05".equals(i.getCode()));
    }

    @Test
    @DisplayName("VX-09: unknown state refId")
    void unknownStateRef() {
        var doc = buildDocWithChain(OntologyExchangeDocument.EpcChain.builder()
                .id("chain-1").name("Chain").aggregateRootId("entity-1")
                .nodes(List.of(node("n1", "state", "st-missing")))
                .edges(List.of(edge("e1", "n1", "n1")))
                .build());
        var issues = validator.validate(new ValidationContext(doc, "strict", "test"));
        assertThat(issues).anyMatch(i -> "VX-09".equals(i.getCode()));
    }

    @Test
    @DisplayName("VX-12: edge sourceNodeId not in chain")
    void invalidEdgeSource() {
        var doc = buildDocWithChain(OntologyExchangeDocument.EpcChain.builder()
                .id("chain-1").name("Chain").aggregateRootId("entity-1")
                .nodes(List.of(node("n1", "state", "st-a")))
                .edges(List.of(edge("e1", "missing", "n1")))
                .build());
        var issues = validator.validate(new ValidationContext(doc, "strict", "test"));
        assertThat(issues).anyMatch(i -> "VX-12".equals(i.getCode()));
    }

    @Test
    @DisplayName("VX-13: edge targetNodeId not in chain")
    void invalidEdgeTarget() {
        var doc = buildDocWithChain(OntologyExchangeDocument.EpcChain.builder()
                .id("chain-1").name("Chain").aggregateRootId("entity-1")
                .nodes(List.of(node("n1", "state", "st-a")))
                .edges(List.of(edge("e1", "n1", "missing")))
                .build());
        var issues = validator.validate(new ValidationContext(doc, "strict", "test"));
        assertThat(issues).anyMatch(i -> "VX-13".equals(i.getCode()));
    }

    @Test
    @DisplayName("VX-15: duplicate chain id")
    void duplicateChainId() {
        var chain = OntologyExchangeDocument.EpcChain.builder()
                .id("chain-1").name("Chain").aggregateRootId("entity-1")
                .nodes(List.of(node("n1", "state", "st-a")))
                .edges(List.of(edge("e1", "n1", "n1")))
                .build();
        var doc = buildDocWithChains(List.of(chain, chain));
        var issues = validator.validate(new ValidationContext(doc, "strict", "test"));
        assertThat(issues).anyMatch(i -> "VX-15".equals(i.getCode()));
    }

    @Test
    @DisplayName("valid chain with resolved refs passes")
    void validChainPasses() {
        var doc = buildDocWithChain(validChain());
        var issues = validator.validate(new ValidationContext(doc, "strict", "test"));
        assertThat(issues.stream().filter(i -> "error".equals(i.getSeverity()))).isEmpty();
    }

    private OntologyExchangeDocument.EpcChain validChain() {
        return OntologyExchangeDocument.EpcChain.builder()
                .id("chain-1").name("Chain").aggregateRootId("entity-1")
                .nodes(List.of(
                        node("n1", "state", "st-a"),
                        node("n2", "action", "act-1")))
                .edges(List.of(edge("e1", "n1", "n2")))
                .build();
    }

    private OntologyExchangeDocument buildDocWithChain(OntologyExchangeDocument.EpcChain chain) {
        return buildDocWithChains(List.of(chain));
    }

    private OntologyExchangeDocument buildDocWithChains(List<OntologyExchangeDocument.EpcChain> chains) {
        return OntologyExchangeDocument.builder()
                .spec(OntologyExchangeDocument.Spec.builder()
                        .project(OntologyExchangeDocument.OntologyProject.builder()
                                .dataModel(OntologyExchangeDocument.DataModel.builder()
                                        .entities(List.of(OntologyExchangeDocument.Entity.builder()
                                                .id("entity-1").name("Entity").build()))
                                        .build())
                                .behaviorModel(OntologyExchangeDocument.BehaviorModel.builder()
                                        .stateMachines(List.of(OntologyExchangeDocument.StateMachine.builder()
                                                .id("sm-1").entity("entity-1")
                                                .states(List.of(OntologyExchangeDocument.State.builder()
                                                        .id("st-a").name("A").isInitial(true).build()))
                                                .build()))
                                        .actions(List.of(OntologyExchangeDocument.Action.builder()
                                                .id("act-1").name("Act").targetEntityId("entity-1").build()))
                                        .build())
                                .epcModel(OntologyExchangeDocument.EpcModel.builder()
                                        .chains(chains)
                                        .build())
                                .build())
                        .build())
                .build();
    }

    private OntologyExchangeDocument.EpcNode node(String id, String refType, String refId) {
        return OntologyExchangeDocument.EpcNode.builder()
                .id(id).nodeType("function").name(id).refType(refType).refId(refId).build();
    }

    private OntologyExchangeDocument.EpcEdge edge(String id, String source, String target) {
        return OntologyExchangeDocument.EpcEdge.builder()
                .id(id).sourceNodeId(source).targetNodeId(target).edgeType("control_flow").build();
    }
}
