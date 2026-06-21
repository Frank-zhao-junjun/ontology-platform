package com.ontology.platform.application.service.exchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.application.service.semantic.SemanticService;
import com.ontology.platform.domain.dto.imports.OntologyExchangeDocument;
import com.ontology.platform.domain.dto.semantic.IntentResult;
import com.ontology.platform.infrastructure.persistence.*;
import com.ontology.platform.infrastructure.validation.SemanticValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Phase 3c Sprint 2 E2E: golden JSON parse → validate → publish → resolve_intent.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Phase 3c Semantic E2E Test")
class ExchangePhase3cSemanticE2ETest {

    private static final String GOLDEN_PATH = "../docs/shared/examples/manufacturing-exchange-v2.json";

    @Mock private AgentIntentPOMapper agentIntentMapper;
    @Mock private IntentSlotPOMapper intentSlotMapper;
    @Mock private BusinessTermPOMapper businessTermMapper;
    @Mock private SemanticRelationPOMapper semanticRelationMapper;
    @Mock private AgentPolicySemanticPOMapper agentPolicySemanticMapper;
    @Mock private ErrorRecoveryPOMapper errorRecoveryMapper;
    @Mock private SemanticFieldMappingPOMapper semanticFieldMappingMapper;
    @Mock private EntityLifecycleSnapshotPOMapper lifecycleMapper;

    @Captor private ArgumentCaptor<AgentIntentPO> intentCaptor;
    @Captor private ArgumentCaptor<EntityLifecycleSnapshotPO> lifecycleCaptor;

    private ObjectMapper objectMapper;
    private String goldenJson;
    private ExchangePhase3cPublisher semanticPublisher;
    private ExchangePhase3cLifecyclePublisher lifecyclePublisher;
    private SemanticService semanticService;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        goldenJson = Files.readString(Path.of(GOLDEN_PATH));
        semanticPublisher = new ExchangePhase3cPublisher(
                agentIntentMapper, intentSlotMapper, businessTermMapper, semanticRelationMapper,
                agentPolicySemanticMapper, errorRecoveryMapper, semanticFieldMappingMapper, objectMapper);
        lifecyclePublisher = new ExchangePhase3cLifecyclePublisher(lifecycleMapper, objectMapper);
        semanticService = new SemanticService(
                agentIntentMapper, intentSlotMapper, businessTermMapper, semanticRelationMapper, objectMapper);
    }

    @Test
    @DisplayName("golden JSON: parse → SemanticValidator pass → publish → resolve_intent")
    void goldenJsonSemanticPipeline() throws Exception {
        OntologyExchangeDocument doc = objectMapper.readValue(goldenJson, OntologyExchangeDocument.class);
        assertThat(doc).isNotNull();

        var validator = new SemanticValidator();
        assertThat(validator.validate(
                new com.ontology.platform.domain.service.validation.ValidationContext(doc, "strict", "test")))
                .isEmpty();

        when(agentIntentMapper.insert(any())).thenReturn(1);
        when(intentSlotMapper.insert(any())).thenReturn(1);
        when(lifecycleMapper.insert(any())).thenReturn(1);

        var semanticCounts = semanticPublisher.publish("manufacturing-ontology", doc, goldenJson);
        assertThat(semanticCounts.get("intents")).isEqualTo(1);
        assertThat(semanticCounts.get("intentSlots")).isEqualTo(1);

        var lifecycleCounts = lifecyclePublisher.publish("manufacturing-ontology", doc, goldenJson);
        assertThat(lifecycleCounts.get("lifecycleSnapshots")).isEqualTo(1);

        verify(agentIntentMapper).insert(intentCaptor.capture());
        AgentIntentPO savedIntent = intentCaptor.getValue();
        assertThat(savedIntent.getId()).isEqualTo("intent-release-order");
        assertThat(savedIntent.getCategory()).isEqualTo("workflow");
        assertThat(savedIntent.getTargetEntityId()).isEqualTo("production-order");
        assertThat(savedIntent.getRequiresConfirmation()).isTrue();

        verify(lifecycleMapper).insert(lifecycleCaptor.capture());
        assertThat(lifecycleCaptor.getValue().getEntityId()).isEqualTo("production-order");

        when(agentIntentMapper.selectByOntologyId("manufacturing-ontology"))
                .thenReturn(List.of(savedIntent));
        when(intentSlotMapper.selectList(any())).thenReturn(List.of(
                IntentSlotPO.builder()
                        .id("slot-order-id")
                        .intentId("intent-release-order")
                        .name("生产订单号")
                        .slotType("string")
                        .required(true)
                        .examples("[\"PO-2026-001\"]")
                        .build()));

        IntentResult result = semanticService.resolveIntent("manufacturing-ontology", "下达订单");
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("intent-release-order");
        assertThat(result.getActionId()).isEqualTo("action-release-order");
        assertThat(result.getCategory()).isEqualTo("workflow");
        assertThat(result.getTargetEntityId()).isEqualTo("production-order");
        assertThat(result.getSlots()).hasSize(1);
    }
}
