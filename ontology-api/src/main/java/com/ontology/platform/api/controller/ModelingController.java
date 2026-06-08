package com.ontology.platform.api.controller;

import com.ontology.platform.api.dto.*;
import com.ontology.platform.application.service.ModelingService;
import com.ontology.platform.domain.entity.AggregateRoot;
import com.ontology.platform.domain.entity.ObjectTypeV2;
import com.ontology.platform.domain.entity.Relationship;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/contexts/{contextId}")
@RequiredArgsConstructor
@Tag(name = "Modeling", description = "聚合根 / 对象类型 / 关系 (US-S03, S04, S07)")
public class ModelingController {
    private final ModelingService svc;

    // ════════════════════════════════════════════════
    // US-S03: Aggregate Roots
    // ════════════════════════════════════════════════

    @PostMapping("/aggregate-roots")
    @Operation(summary = "定义聚合根 (US-S03)")
    public ResponseEntity<Map<String, Object>> createAggregateRoot(
            @PathVariable String contextId,
            @Valid @RequestBody AggregateRootCreateRequest req) {
        AggregateRoot ar = svc.createAggregateRoot(contextId, req.getName(), req.getCode(), req.getDescription());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("code", 0, "message", "success", "data", AggregateRootResponse.from(ar)));
    }

    @GetMapping("/aggregate-roots")
    @Operation(summary = "查询聚合根列表 (US-S03)")
    public ResponseEntity<Map<String, Object>> listAggregateRoots(@PathVariable String contextId) {
        var data = svc.listAggregateRoots(contextId).stream()
                .map(AggregateRootResponse::from).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("code", 0, "message", "success", "data", data));
    }

    // ════════════════════════════════════════════════
    // US-S04: Object Types
    // ════════════════════════════════════════════════

    @PostMapping("/object-types")
    @Operation(summary = "定义对象类型 (US-S04 AC-1)")
    public ResponseEntity<Map<String, Object>> createObjectType(
            @PathVariable String contextId,
            @Valid @RequestBody ObjectTypeCreateRequest req) {
        ObjectTypeV2 ot = svc.createObjectType(contextId, req.getName(), req.getCode(),
                req.getObjectKind(), req.getAggregateRootId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("code", 0, "message", "success", "data", ObjectTypeResponse.from(ot)));
    }

    @GetMapping("/object-types")
    @Operation(summary = "查询对象类型列表 (US-S04)")
    public ResponseEntity<Map<String, Object>> listObjectTypes(@PathVariable String contextId) {
        var data = svc.listObjectTypes(contextId).stream()
                .map(ObjectTypeResponse::from).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("code", 0, "message", "success", "data", data));
    }

    @GetMapping("/object-types/{id}")
    @Operation(summary = "查询单个对象类型 (US-S04)")
    public ResponseEntity<Map<String, Object>> getObjectType(
            @PathVariable String contextId, @PathVariable String id) {
        ObjectTypeV2 ot = svc.getObjectType(id);
        return ResponseEntity.ok(Map.of("code", 0, "message", "success", "data", ObjectTypeResponse.from(ot)));
    }

    @PutMapping("/object-types/{id}/attributes")
    @Operation(summary = "更新对象属性列表 (US-S04 AC-4)")
    public ResponseEntity<Map<String, Object>> updateAttributes(
            @PathVariable String contextId, @PathVariable String id,
            @RequestBody String attributes) {
        ObjectTypeV2 ot = svc.updateAttributes(id, attributes);
        return ResponseEntity.ok(Map.of("code", 0, "message", "success", "data", ObjectTypeResponse.from(ot)));
    }

    // ════════════════════════════════════════════════
    // US-S07: Relationships
    // ════════════════════════════════════════════════

    @PostMapping("/relationships")
    @Operation(summary = "定义关系类型 (US-S07 AC-1)")
    public ResponseEntity<Map<String, Object>> createRelationship(
            @PathVariable String contextId,
            @Valid @RequestBody RelationshipCreateRequest req) {
        Relationship r = svc.createRelationship(contextId,
                req.getSourceObjectId(), req.getTargetObjectId(),
                req.getName(), req.getCode(),
                req.getCardinality(), req.getRelationKind(),
                req.isCrossContext(), req.getTargetContextId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("code", 0, "message", "success", "data", RelationshipResponse.from(r)));
    }

    @GetMapping("/relationships")
    @Operation(summary = "查询关系列表 (US-S07)")
    public ResponseEntity<Map<String, Object>> listRelationships(@PathVariable String contextId) {
        var data = svc.listRelationships(contextId).stream()
                .map(RelationshipResponse::from).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("code", 0, "message", "success", "data", data));
    }
}
