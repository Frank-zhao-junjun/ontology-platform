package com.ontology.platform.api.controller;

import com.ontology.platform.api.dto.BoundedContextCreateRequest;
import com.ontology.platform.api.dto.BoundedContextResponse;
import com.ontology.platform.application.service.BoundedContextService;
import com.ontology.platform.common.enums.DomainTag;
import com.ontology.platform.domain.entity.BoundedContext;
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
    public ResponseEntity<Map<String, Object>> approve(@PathVariable String id) {
        return ResponseEntity.ok(Map.of("code", 0, "message", "ok", "data", BoundedContextResponse.from(service.approveAndPublish(id))));
    }

    @PostMapping("/{id}/reject-review")
    public ResponseEntity<Map<String, Object>> reject(@PathVariable String id) {
        return ResponseEntity.ok(Map.of("code", 0, "message", "ok", "data", BoundedContextResponse.from(service.rejectToDraft(id))));
    }
}
