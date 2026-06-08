package com.ontology.platform.api.controller;

import com.ontology.platform.application.manifest.ManifestDryRunResult;
import com.ontology.platform.application.manifest.ManifestImportDryRunService;
import com.ontology.platform.application.manifest.ManifestImportResult;
import com.ontology.platform.application.manifest.ManifestImportService;
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
 * US-A01 Manifest import: dry-run (Round 1) and persist (Round 2).
 * Full URL prefix: /api/ontology/import (context-path /api).
 */
@RestController
@RequestMapping("/ontology/import")
@RequiredArgsConstructor
@Tag(name = "Ontology Import")
public class OntologyImportController {
    private final ManifestImportDryRunService dryRunService;
    private final ManifestImportService importService;

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

    @PostMapping(value = "/import", consumes = {
            MediaType.APPLICATION_JSON_VALUE,
            "application/yaml",
            "text/yaml",
            "application/x-yaml"
    })
    @Operation(summary = "Manifest import — validate (dry-run) then persist to /v1/contexts model (US-A01 Round 2)")
    public ResponseEntity<Map<String, Object>> importManifest(@RequestBody byte[] body) throws IOException {
        String raw = new String(body, StandardCharsets.UTF_8).trim();
        ManifestImportResult result = isLikelyYaml(raw)
                ? importService.importYaml(raw)
                : importService.importJson(raw);
        if (!result.isValid()) {
            return ResponseEntity.badRequest().body(Map.of("code", 3001, "message", "Manifest validation failed", "data", result));
        }
        return ResponseEntity.status(201).body(Map.of("code", 0, "message", "success", "data", result));
    }

    @PostMapping("/import/us-a01-smoke")
    @Operation(summary = "Import bundled US-A01 manufacturing-manifest.yaml into DB")
    public ResponseEntity<Map<String, Object>> importUsA01Smoke() throws IOException {
        String yaml = loadBundledManifest();
        if (yaml == null) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "code", 500, "message", "Bundled manifest not found", "data", Map.of()));
        }
        ManifestImportResult result = importService.importYaml(yaml);
        if (!result.isValid()) {
            return ResponseEntity.badRequest().body(Map.of("code", 3001, "message", "Manifest validation failed", "data", result));
        }
        return ResponseEntity.status(201).body(Map.of("code", 0, "message", "success", "data", result));
    }

    @PostMapping("/dry-run/us-a01-smoke")
    @Operation(summary = "Dry-run bundled US-A01 manufacturing-manifest.yaml")
    public ResponseEntity<Map<String, Object>> dryRunUsA01Smoke() throws IOException {
        String yaml = loadBundledManifest();
        if (yaml == null) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "code", 500, "message", "Bundled manifest not found", "data", Map.of()));
        }
        ManifestDryRunResult result = dryRunService.dryRunYaml(yaml);
        return ResponseEntity.ok(Map.of("code", 0, "message", "success", "data", result));
    }

    private String loadBundledManifest() throws IOException {
        try (var in = getClass().getResourceAsStream("/manifests/us-a01/manufacturing-manifest.yaml")) {
            if (in == null) {
                return null;
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static boolean isLikelyYaml(String raw) {
        if (raw.startsWith("{") || raw.startsWith("[")) {
            return false;
        }
        return raw.contains("apiVersion:") || raw.contains("kind: OntologyManifest");
    }
}
