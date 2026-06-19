package com.ontology.platform.application.service.exchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.infrastructure.persistence.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExchangePhase3cPublisher Test")
class ExchangePhase3cPublisherTest {

    @Mock private AgentIntentPOMapper agentIntentMapper;
    @Mock private IntentSlotPOMapper intentSlotMapper;
    @Mock private BusinessTermPOMapper businessTermMapper;
    @Mock private SemanticRelationPOMapper semanticRelationMapper;
    @Mock private AgentPolicySemanticPOMapper agentPolicySemanticMapper;
    @Mock private ErrorRecoveryPOMapper errorRecoveryMapper;
    @Mock private SemanticFieldMappingPOMapper semanticFieldMappingMapper;

    private ExchangePhase3cPublisher publisher;
    private String goldenJson;

    @BeforeEach
    void setUp() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        publisher = new ExchangePhase3cPublisher(
                agentIntentMapper, intentSlotMapper, businessTermMapper, semanticRelationMapper,
                agentPolicySemanticMapper, errorRecoveryMapper, semanticFieldMappingMapper, objectMapper);
        goldenJson = Files.readString(Path.of("../docs/shared/examples/manufacturing-exchange-v2.json"));
    }

    @Test
    @DisplayName("should persist intents from golden JSON via JsonNode fallback")
    void publishFromGoldenJson() {
        Map<String, Integer> counts = publisher.publish("manufacturing-ontology", null, goldenJson);

        assertThat(counts.get("intents")).isEqualTo(1);
        assertThat(counts.get("intentSlots")).isEqualTo(1);
        verify(agentIntentMapper, atLeastOnce()).insert(any());
        verify(intentSlotMapper, atLeastOnce()).insert(any());
    }
}
