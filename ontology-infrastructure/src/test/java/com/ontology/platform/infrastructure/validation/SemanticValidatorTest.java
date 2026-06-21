package com.ontology.platform.infrastructure.validation;

import com.ontology.platform.domain.dto.imports.OntologyExchangeDocument;
import com.ontology.platform.domain.service.validation.ValidationContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SemanticValidator Test")
class SemanticValidatorTest {

    private final SemanticValidator validator = new SemanticValidator();

    @Test
    @DisplayName("should pass when agentSemanticLayer is null")
    void passWhenNoSemanticLayer() {
        var doc = OntologyExchangeDocument.builder()
                .spec(OntologyExchangeDocument.Spec.builder()
                        .project(OntologyExchangeDocument.OntologyProject.builder().build())
                        .build())
                .build();
        assertThat(validator.validate(new ValidationContext(doc, "strict", "test"))).isEmpty();
    }

    @Test
    @DisplayName("should detect missing intent id")
    void detectMissingIntentId() {
        var doc = buildDocWithIntent(OntologyExchangeDocument.Intent.builder()
                .name("下达订单")
                .build());
        var issues = validator.validate(new ValidationContext(doc, "strict", "test"));
        assertThat(issues).anyMatch(i -> "V-AS-01".equals(i.getCode()));
    }

    @Test
    @DisplayName("should detect missing actionId")
    void detectMissingActionId() {
        var doc = buildDocWithIntent(OntologyExchangeDocument.Intent.builder()
                .id("intent-1")
                .name("下达订单")
                .triggerPhrases(List.of("下达"))
                .build());
        var issues = validator.validate(new ValidationContext(doc, "strict", "test"));
        assertThat(issues).anyMatch(i -> "V-AS-03".equals(i.getCode()));
    }

    @Test
    @DisplayName("should detect actionId not in behaviorModel")
    void detectUnknownActionId() {
        var doc = buildDocWithIntent(OntologyExchangeDocument.Intent.builder()
                .id("intent-1")
                .name("下达订单")
                .actionId("missing-action")
                .triggerPhrases(List.of("下达"))
                .build());
        var issues = validator.validate(new ValidationContext(doc, "strict", "test"));
        assertThat(issues).anyMatch(i -> "V-AS-05".equals(i.getCode()));
    }

    @Test
    @DisplayName("should pass valid intent referencing existing action")
    void passValidIntent() {
        var doc = buildDocWithIntent(OntologyExchangeDocument.Intent.builder()
                .id("intent-release-order")
                .name("下达生产订单")
                .actionId("action-release-order")
                .targetEntityId("production-order")
                .triggerPhrases(List.of("下达订单"))
                .slotFilling(OntologyExchangeDocument.SlotFilling.builder()
                        .slots(List.of(OntologyExchangeDocument.IntentSlot.builder()
                                .id("slot-order-id")
                                .paramName("order_id")
                                .displayName("生产订单号")
                                .build()))
                        .requiredSlots(List.of("slot-order-id"))
                        .build())
                .build());
        assertThat(validator.validate(new ValidationContext(doc, "strict", "test"))).isEmpty();
    }

    private OntologyExchangeDocument buildDocWithIntent(OntologyExchangeDocument.Intent intent) {
        return OntologyExchangeDocument.builder()
                .spec(OntologyExchangeDocument.Spec.builder()
                        .project(OntologyExchangeDocument.OntologyProject.builder()
                                .dataModel(OntologyExchangeDocument.DataModel.builder()
                                        .entities(List.of(OntologyExchangeDocument.Entity.builder()
                                                .id("production-order")
                                                .name("生产订单")
                                                .build()))
                                        .build())
                                .behaviorModel(OntologyExchangeDocument.BehaviorModel.builder()
                                        .actions(List.of(OntologyExchangeDocument.Action.builder()
                                                .id("action-release-order")
                                                .name("下达")
                                                .build()))
                                        .build())
                                .agentSemanticLayer(OntologyExchangeDocument.AgentSemanticLayer.builder()
                                        .intents(List.of(intent))
                                        .build())
                                .build())
                        .build())
                .build();
    }
}
