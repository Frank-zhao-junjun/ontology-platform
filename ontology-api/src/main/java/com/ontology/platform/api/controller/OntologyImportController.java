package com.ontology.platform.api.controller;

import com.ontology.platform.application.manifest.ManifestDryRunResult;
import com.ontology.platform.application.manifest.ManifestImportDryRunService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * US-A01 Platform Import Smoke — read-only dry-run (no DB write).
 * Full URL: POST /api/ontology/import/dry-run (context-path /api).
 */
@RestController
@RequestMapping("/ontology/import")
@RequiredArgsConstructor
@Tag(name = "Ontology Import")
public class OntologyImportController {
    private final ManifestImportDryRunService dryRunService;

    @PostMapping(value = "/dry-run", consumes = {
            MediaType.APPLICATION_JSON_VALUE,
            "application/yaml",
            "text/yaml",
            "application/x-yaml"
    })
    @Operation(summary = "Manifest import dry-run (US-A01 Round 1)")
    public ResponseEntity<Map<String, Object>> dryRun(@RequestBody byte[] body) throws IOException {
        String raw = new String(body, StandardCharsets.UTF_8).trim();
        ManifestDryRunResult result = isLikelyYaml(raw)
                ? dryRunService.dryRunYaml(raw)
                : dryRunService.dryRunJson(raw);
        return ResponseEntity.ok(Map.of("code", 0, "message", "success", "data", result));
    }

    @PostMapping("/dry-run/us-a01-smoke")
    @Operation(summary = "Dry-run bundled US-A01 manufacturing-manifest.yaml")
    public ResponseEntity<Map<String, Object>> dryRunUsA01Smoke() throws IOException {
        try (var in = getClass().getResourceAsStream("/manifests/us-a01/manufacturing-manifest.yaml")) {
            if (in == null) {
                return ResponseEntity.internalServerError().body(Map.of(
                        "code", 500,
                        "message", "Bundled manifest not found",
                        "data", Map.of()));
            }
            String yaml = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            ManifestDryRunResult result = dryRunService.dryRunYaml(yaml);
            return ResponseEntity.ok(Map.of("code", 0, "message", "success", "data", result));
        }
    }

    private static boolean isLikelyYaml(String raw) {
        if (raw.startsWith("{") || raw.startsWith("[")) {
            return false;
        }
        return raw.contains("apiVersion:") || raw.contains("kind: OntologyManifest");
    }
}
