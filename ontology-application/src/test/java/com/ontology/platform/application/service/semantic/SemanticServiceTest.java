package com.ontology.platform.application.service.semantic;

import com.ontology.platform.domain.dto.semantic.IntentResult;
import com.ontology.platform.infrastructure.persistence.AgentIntentPO;
import com.ontology.platform.infrastructure.persistence.AgentIntentPOMapper;
import com.ontology.platform.infrastructure.persistence.BusinessTermPOMapper;
import com.ontology.platform.infrastructure.persistence.IntentSlotPO;
import com.ontology.platform.infrastructure.persistence.IntentSlotPOMapper;
import com.ontology.platform.infrastructure.persistence.SemanticRelationPOMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SemanticService Test")
class SemanticServiceTest {

    @Mock
    private AgentIntentPOMapper agentIntentMapper;
    @Mock
    private IntentSlotPOMapper intentSlotMapper;
    @Mock
    private BusinessTermPOMapper businessTermMapper;
    @Mock
    private SemanticRelationPOMapper semanticRelationMapper;

    private SemanticService service;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        service = new SemanticService(
                agentIntentMapper, intentSlotMapper, businessTermMapper, semanticRelationMapper, objectMapper);
    }

    @Test
    @DisplayName("should resolve manufacturing intent by trigger phrase")
    void resolveManufacturingIntent() {
        AgentIntentPO intent = AgentIntentPO.builder()
                .id("intent-release-order")
                .ontologyId("manufacturing-ontology")
                .name("下达生产订单")
                .actionId("action-release-order")
                .triggerPhrases("[\"下达订单\", \"release production order\"]")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(agentIntentMapper.selectByOntologyId("manufacturing-ontology")).thenReturn(List.of(intent));
        when(intentSlotMapper.selectList(any())).thenReturn(List.of(
                IntentSlotPO.builder()
                        .id("slot-order-id")
                        .intentId("intent-release-order")
                        .name("生产订单号")
                        .slotType("string")
                        .required(true)
                        .examples("[\"PO-2026-001\"]")
                        .build()));

        IntentResult result = service.resolveIntent("manufacturing-ontology", "下达订单");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("intent-release-order");
        assertThat(result.getActionId()).isEqualTo("action-release-order");
        assertThat(result.getMatchScore()).isGreaterThan(0);
        assertThat(result.getSlots()).hasSize(1);
    }

    @Test
    @DisplayName("should return null when no intents match")
    void resolveNoMatch() {
        when(agentIntentMapper.selectByOntologyId("empty-ontology")).thenReturn(List.of());

        IntentResult result = service.resolveIntent("empty-ontology", "unknown phrase");

        assertThat(result).isNull();
    }
}
