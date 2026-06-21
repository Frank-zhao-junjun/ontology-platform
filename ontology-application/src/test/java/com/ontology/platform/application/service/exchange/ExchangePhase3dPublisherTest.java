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
@DisplayName("ExchangePhase3dPublisher Test")
class ExchangePhase3dPublisherTest {

    @Mock private EpcChainPOMapper epcChainMapper;
    @Mock private EpcNodePOMapper epcNodeMapper;
    @Mock private EpcEdgePOMapper epcEdgeMapper;
    @Mock private EpcModelRefPOMapper epcModelRefMapper;
    @Mock private EpcProfilePOMapper epcProfileMapper;

    private ExchangePhase3dPublisher publisher;
    private String goldenJson;

    @BeforeEach
    void setUp() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        publisher = new ExchangePhase3dPublisher(
                epcChainMapper, epcNodeMapper, epcEdgeMapper, epcModelRefMapper, epcProfileMapper, objectMapper);
        goldenJson = Files.readString(Path.of("../docs/shared/examples/manufacturing-exchange-v2.json"));
    }

    @Test
    @DisplayName("should persist EPC chain from golden JSON")
    void publishFromGoldenJson() {
        Map<String, Integer> counts = publisher.publish("manufacturing-ontology", null, goldenJson);

        assertThat(counts.get("chains")).isEqualTo(1);
        assertThat(counts.get("nodes")).isEqualTo(2);
        assertThat(counts.get("edges")).isEqualTo(1);
        assertThat(counts.get("modelRefs")).isEqualTo(2);

        verify(epcChainMapper, atLeastOnce()).insert(any());
        verify(epcNodeMapper, atLeastOnce()).insert(any());
        verify(epcEdgeMapper, atLeastOnce()).insert(any());
        verify(epcModelRefMapper, atLeastOnce()).insert(any());
    }
}
