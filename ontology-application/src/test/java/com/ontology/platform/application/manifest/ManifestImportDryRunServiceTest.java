package com.ontology.platform.application.manifest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class ManifestImportDryRunServiceTest {
    private ManifestImportDryRunService service;

    @BeforeEach
    void setUp() {
        service = new ManifestImportDryRunService(new ManifestYamlParser());
    }

    @Test
    void usA01ManufacturingManifestShouldPassDryRun() throws Exception {
        InputStream in = getClass().getResourceAsStream("/manifests/us-a01/manufacturing-manifest.yaml");
        assertThat(in).isNotNull();
        byte[] bytes = in.readAllBytes();
        String yaml = new String(bytes, StandardCharsets.UTF_8);
        ManifestDryRunResult result = service.dryRunYaml(yaml);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getDraftId()).isEqualTo("dry-run:manufacturing-ontology:0.1.0");
        assertThat(result.getErrors()).isEmpty();

        ManifestImportedCounts c = result.getImportedCounts();
        assertThat(c.getBoundedContext()).isEqualTo(1);
        assertThat(c.getBusinessScenarios()).isEqualTo(2);
        assertThat(c.getObjectTypes()).isEqualTo(5);
        assertThat(c.getProperties()).isGreaterThanOrEqualTo(8);
        assertThat(c.getPropertyFieldKeys()).isGreaterThanOrEqualTo(6);
        assertThat(c.getRelations()).isEqualTo(1);
        assertThat(c.getStateMachines()).isEqualTo(1);
        assertThat(c.getActions()).isEqualTo(3);
        assertThat(c.getRules()).isEqualTo(2);
        assertThat(c.getDomainEvents()).isEqualTo(3);
        assertThat(c.getRoles()).isEqualTo(2);
        assertThat(c.getDataSources()).isEqualTo(1);
    }
}
