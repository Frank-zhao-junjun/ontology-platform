package com.ontology.platform.api.controller;

import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.application.dto.*;
import com.ontology.platform.application.service.RelationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 关系管理API控制器
 * Relation Management API Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/ontologies/{ontologyId}/relations")
@RequiredArgsConstructor
@Tag(name = "Relation", description = "关系管理API")
public class RelationController {

    private final RelationService relationService;

    /**
     * 创建关系
     */
    @PostMapping
    @Operation(summary = "创建关系", description = "在指定本体内创建一个新的关系定义")
    public ResponseEntity<ApiResponse<RelationResponse>> createRelation(
            @Parameter(description = "本体ID") @PathVariable String ontologyId,
            @Valid @RequestBody CreateRelationRequest request) {
        log.info("REST: Create relation, ontologyId={}, name={}", ontologyId, request.getName());
        request.setOntologyId(ontologyId);
        RelationResponse response = relationService.createRelation(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * 获取本体下的所有关系
     */
    @GetMapping
    @Operation(summary = "获取关系列表", description = "获取指定本体内的所有关系定义")
    public ResponseEntity<ApiResponse<List<RelationResponse>>> listRelations(
            @Parameter(description = "本体ID") @PathVariable String ontologyId) {
        log.debug("REST: List relations, ontologyId={}", ontologyId);
        List<RelationResponse> response = relationService.listRelations(ontologyId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 获取关系详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取关系详情", description = "根据ID获取关系的详细信息")
    public ResponseEntity<ApiResponse<RelationResponse>> getRelation(
            @Parameter(description = "本体ID") @PathVariable String ontologyId,
            @Parameter(description = "关系ID") @PathVariable String id) {
        log.debug("REST: Get relation, ontologyId={}, id={}", ontologyId, id);
        RelationResponse response = relationService.getRelationById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 更新关系
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新关系", description = "更新指定关系的基本信息和属性")
    public ResponseEntity<ApiResponse<RelationResponse>> updateRelation(
            @Parameter(description = "本体ID") @PathVariable String ontologyId,
            @Parameter(description = "关系ID") @PathVariable String id,
            @Valid @RequestBody UpdateRelationRequest request) {
        log.info("REST: Update relation, ontologyId={}, id={}", ontologyId, id);
        RelationResponse response = relationService.updateRelation(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 删除关系
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除关系", description = "删除指定的关系定义")
    public ResponseEntity<ApiResponse<Void>> deleteRelation(
            @Parameter(description = "本体ID") @PathVariable String ontologyId,
            @Parameter(description = "关系ID") @PathVariable String id) {
        log.info("REST: Delete relation, ontologyId={}, id={}", ontologyId, id);
        relationService.deleteRelation(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 按源对象类型查询关系
     */
    @GetMapping("/by-source/{sourceTypeId}")
    @Operation(summary = "按源类型查询关系", description = "查询以指定对象类型为源的所有关系")
    public ResponseEntity<ApiResponse<List<RelationResponse>>> findBySourceType(
            @Parameter(description = "本体ID") @PathVariable String ontologyId,
            @Parameter(description = "源对象类型ID") @PathVariable String sourceTypeId) {
        log.debug("REST: Find relations by source type, ontologyId={}, sourceTypeId={}", ontologyId, sourceTypeId);
        List<RelationResponse> response = relationService.findBySourceTypeId(sourceTypeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 按目标对象类型查询关系
     */
    @GetMapping("/by-target/{targetTypeId}")
    @Operation(summary = "按目标类型查询关系", description = "查询以指定对象类型为目标的所有关系")
    public ResponseEntity<ApiResponse<List<RelationResponse>>> findByTargetType(
            @Parameter(description = "本体ID") @PathVariable String ontologyId,
            @Parameter(description = "目标对象类型ID") @PathVariable String targetTypeId) {
        log.debug("REST: Find relations by target type, ontologyId={}, targetTypeId={}", ontologyId, targetTypeId);
        List<RelationResponse> response = relationService.findByTargetTypeId(targetTypeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 查询关联的对象类型
     */
    @GetMapping("/{id}/related-types")
    @Operation(summary = "查询关联对象类型", description = "查询指定关系关联的所有对象类型")
    public ResponseEntity<ApiResponse<List<ObjectTypeResponse>>> findRelatedObjectTypes(
            @Parameter(description = "本体ID") @PathVariable String ontologyId,
            @Parameter(description = "关系ID") @PathVariable String id) {
        log.debug("REST: Find related object types, ontologyId={}, id={}", ontologyId, id);
        List<ObjectTypeResponse> response = relationService.findRelatedObjectTypes(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
