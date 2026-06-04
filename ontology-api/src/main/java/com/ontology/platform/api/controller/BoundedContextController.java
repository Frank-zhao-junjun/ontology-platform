package com.ontology.platform.api.controller;

import com.ontology.platform.api.dto.BoundedContextCreateRequest;
import com.ontology.platform.api.dto.BoundedContextResponse;
import com.ontology.platform.application.manifest.ManifestSnapshotService;
import com.ontology.platform.application.service.BoundedContextService;
import com.ontology.platform.common.enums.DomainTag;
import com.ontology.platform.domain.entity.BoundedContext;
import com.ontology.platform.domain.entity.PublishedManifest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/contexts")
@RequiredArgsConstructor
@Tag(name = "Contexts")
public class BoundedContextController {
    private final BoundedContextService service;
    private final ManifestSnapshotService manifestSnapshotService;

    @PostMapping
    @Operation(summary = "创建限界上下文 (US-S01)")
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody BoundedContextCreateRequest req) {
        BoundedContext ctx = service.create(req.getName(), req.getCode(), req.getDescription(), DomainTag.fromCode(req.getDomainTag()), "user");
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("code", 0, "message", "success", "data", BoundedContextResponse.from(ctx)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> get(@PathVariable String id) {
        BoundedContext ctx = service.findById(id);
        return ResponseEntity.ok(Map.of("code", 0, "message", "success", "data", BoundedContextResponse.from(ctx)));
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> list() {
        var data = service.findAll().stream().map(BoundedContextResponse::from).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("code", 0, "message", "success", "data", data));
    }

    @PostMapping("/{id}/submit-review")
    public ResponseEntity<Map<String, Object>> submitReview(@PathVariable String id) {
        return ResponseEntity.ok(Map.of("code", 0, "message", "ok", "data", BoundedContextResponse.from(service.submitForReview(id))));
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "批准发布并生成 Manifest 快照 (US-G05 AC-4 + US-A01 Round 3)")
    public ResponseEntity<Map<String, Object>> approve(@PathVariable String id) throws Exception {
        PublishedManifest manifest = service.approvePublishAndSnapshot(id);
        return ResponseEntity.ok(Map.of("code", 0, "message", "ok", "data", Map.of(
                "context", BoundedContextResponse.from(service.findById(id)),
                "publishedManifest", Map.of(
                        "id", manifest.getId(),
                        "version", manifest.getVersion(),
                        "ontologyId", manifest.getOntologyId()))));
    }

    @GetMapping("/{id}/manifests")
    public ResponseEntity<Map<String, Object>> listManifests(@PathVariable String id) {
        var data = manifestSnapshotService.listManifests(id).stream()
                .map(m -> Map.of("id", m.getId(), "version", m.getVersion(), "createdAt", m.getCreatedAt().toString()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("code", 0, "message", "success", "data", data));
    }

    @GetMapping("/{id}/manifests/latest")
    public ResponseEntity<Map<String, Object>> latestManifest(@PathVariable String id) {
        PublishedManifest m = manifestSnapshotService.getLatest(id);
        return ResponseEntity.ok(Map.of("code", 0, "message", "success", "data", Map.of(
                "id", m.getId(), "version", m.getVersion(), "ontologyId", m.getOntologyId(),
                "snapshotJson", m.getSnapshotJson())));
    }

    @PostMapping("/{id}/reject-review")
    public ResponseEntity<Map<String, Object>> reject(@PathVariable String id) {
        return ResponseEntity.ok(Map.of("code", 0, "message", "ok", "data", BoundedContextResponse.from(service.rejectToDraft(id))));
    }
}
