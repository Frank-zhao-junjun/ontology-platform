package com.ontology.platform.domain.dto.imports;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OntologyExchangeDocument Golden JSON Parse Test")
class OntologyExchangeDocumentParseTest {

    private static final String GOLDEN_PATH = "../docs/shared/examples/manufacturing-exchange-v2.json";
    private static ObjectMapper objectMapper;
    private static String goldenJson;

    @BeforeAll
    static void loadFixture() throws Exception {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        goldenJson = Files.readString(Path.of(GOLDEN_PATH));
    }

    @Test
    @DisplayName("should fully deserialize manufacturing-exchange-v2.json")
    void parseGoldenJson() throws Exception {
        OntologyExchangeDocument doc = objectMapper.readValue(goldenJson, OntologyExchangeDocument.class);

        assertThat(doc).isNotNull();
        assertThat(doc.getApiVersion()).isEqualTo("ontology.platform/v2");
        assertThat(doc.getMetadata().getId()).isEqualTo("manufacturing-ontology");
        assertThat(doc.getSpec().getProject().getDataModel().getEntities()).hasSize(2);
        assertThat(doc.getSpec().getProject().getBehaviorModel().getStateMachines()).hasSize(1);
        assertThat(doc.getSpec().getProject().getBehaviorModel().getActions()).hasSize(1);
        assertThat(doc.getSpec().getProject().getAgentSemanticLayer().getIntents()).hasSize(1);

        var intent = doc.getSpec().getProject().getAgentSemanticLayer().getIntents().get(0);
        assertThat(intent.getId()).isEqualTo("intent-release-order");
        assertThat(intent.getCategory()).isEqualTo("workflow");
        assertThat(intent.getTargetEntityId()).isEqualTo("production-order");
        assertThat(intent.getSlotFilling().getSlots()).hasSize(1);

        var transition = doc.getSpec().getProject().getBehaviorModel().getStateMachines().get(0)
                .getTransitions().get(0);
        assertThat(transition.getPreConditions()).contains("rule-kitting");
    }
}
