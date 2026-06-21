package com.ontology.platform.infrastructure.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.domain.dto.imports.OntologyExchangeDocument;
import com.ontology.platform.domain.service.validation.ValidationContext;
import com.ontology.platform.domain.service.validation.ValidationIssue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Import Golden JSON Validation Test")
class ImportGoldenValidationTest {

    private static final String IMPORT_GOLDEN_PATH = "../docs/import/manufacturing-exchange-v2.json";
    private static ObjectMapper objectMapper;
    private static OntologyExchangeDocument doc;

    @BeforeAll
    static void loadFixture() throws Exception {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        String json = Files.readString(Path.of(IMPORT_GOLDEN_PATH));
        doc = objectMapper.readValue(json, OntologyExchangeDocument.class);
    }

    @Test
    @DisplayName("import golden JSON passes LifecycleValidator without errors")
    void lifecycleValidatorPasses() {
        var issues = new LifecycleValidator().validate(new ValidationContext(doc, "strict", "test"));
        List<ValidationIssue> errors = issues.stream()
                .filter(i -> "error".equals(i.getSeverity()))
                .toList();
        assertThat(errors).isEmpty();
    }

    @Test
    @DisplayName("import golden JSON passes SemanticValidator without errors")
    void semanticValidatorPasses() {
        var issues = new SemanticValidator().validate(new ValidationContext(doc, "strict", "test"));
        List<ValidationIssue> errors = issues.stream()
                .filter(i -> "error".equals(i.getSeverity()))
                .toList();
        assertThat(errors).isEmpty();
    }
}
