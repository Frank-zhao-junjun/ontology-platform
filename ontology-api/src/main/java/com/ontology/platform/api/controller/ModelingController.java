package com.ontology.platform.api.controller;

import com.ontology.platform.api.dto.AggregateRootCreateRequest;
import com.ontology.platform.api.dto.AggregateRootResponse;
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

    // ──── US-S03: Aggregate Roots ────
    @PostMapping("/aggregate-roots")
    @Operation(summary = "定义聚合根 (US-S03)")
    public ResponseEntity<Map<String,Object>> createAggRoot(@PathVariable String contextId, @Valid @RequestBody AggregateRootCreateRequest req) {
        AggregateRoot ar = svc.createAggregateRoot(contextId, req.getName(), req.getCode(), req.getDescription());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("code",0,"message","success","data",AggregateRootResponse.from(ar)));
    }
    @GetMapping("/aggregate-roots")
    public ResponseEntity<Map<String,Object>> listAggRoots(@PathVariable String contextId) {
        var data = svc.listAggregateRoots(contextId).stream().map(AggregateRootResponse::from).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("code",0,"message","success","data",data));
    }

    // ──── US-S04: Object Types ────
    @PostMapping("/object-types")
    @Operation(summary = "定义对象类型 (US-S04)")
    public ResponseEntity<Map<String,Object>> createObjType(@PathVariable String contextId, @RequestBody Map<String,String> body) {
        ObjectTypeV2 ot = svc.createObjectType(contextId, body.get("name"), body.get("code"), body.getOrDefault("objectKind","ENTITY"), body.get("aggregateRootId"));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("code",0,"message","success","data",Map.of("id",ot.getId(),"name",ot.getName(),"code",ot.getCode(),"objectKind",ot.getObjectKind())));
    }
    @GetMapping("/object-types")
    public ResponseEntity<Map<String,Object>> listObjTypes(@PathVariable String contextId) {
        var data = svc.listObjectTypes(contextId).stream().map(o -> Map.of("id",o.getId(),"name",o.getName(),"code",o.getCode(),"objectKind",o.getObjectKind(),"aggregateRootId",o.getAggregateRootId() != null ? o.getAggregateRootId() : "")).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("code",0,"message","success","data",data));
    }
    @PutMapping("/object-types/{id}/attributes")
    @Operation(summary = "更新对象属性列表 (US-S04 AC-4)")
    public ResponseEntity<Map<String,Object>> updateAttrs(@PathVariable String contextId, @PathVariable String id, @RequestBody String attributes) {
        ObjectTypeV2 ot = svc.updateAttributes(id, attributes);
        return ResponseEntity.ok(Map.of("code",0,"message","success","data",Map.of("id",ot.getId(),"attributes",ot.getAttributes())));
    }

    // ──── US-S07: Relationships ────
    @PostMapping("/relationships")
    @Operation(summary = "定义关系类型 (US-S07)")
    public ResponseEntity<Map<String,Object>> createRel(@PathVariable String contextId, @RequestBody Map<String,String> body) {
        Relationship r = svc.createRelationship(contextId, body.get("sourceObjectId"), body.get("targetObjectId"), body.get("name"), body.get("code"), body.getOrDefault("cardinality","1:N"), body.getOrDefault("relationKind","REFERENCE"));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("code",0,"message","success","data",Map.of("id",r.getId(),"name",r.getName(),"code",r.getCode(),"cardinality",r.getCardinality(),"relationKind",r.getRelationKind())));
    }
    @GetMapping("/relationships")
    public ResponseEntity<Map<String,Object>> listRels(@PathVariable String contextId) {
        var data = svc.listRelationships(contextId).stream().map(r -> Map.of("id",r.getId(),"name",r.getName(),"code",r.getCode(),"sourceObjectId",r.getSourceObjectId(),"targetObjectId",r.getTargetObjectId(),"cardinality",r.getCardinality(),"relationKind",r.getRelationKind())).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("code",0,"message","success","data",data));
    }
}
