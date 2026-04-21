package com.ontology.platform.api.controller;

import com.ontology.platform.application.dto.*;
import com.ontology.platform.application.service.ObjectInstanceService;
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
 * 对象实例管理API控制器
 * ObjectInstance Management API Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/ontologies/{ontologyId}/instances")
@RequiredArgsConstructor
@Tag(name = "ObjectInstance", description = "对象实例管理API")
public class ObjectInstanceController {

    private final ObjectInstanceService instanceService;

    /**
     * 创建对象实例
     */
    @PostMapping
    @Operation(summary = "创建对象实例", description = "根据ObjectType定义创建具体的对象实例")
    public ResponseEntity<ApiResponse<InstanceResponse>> createInstance(
            @Parameter(description = "本体ID") @PathVariable String ontologyId,
            @Valid @RequestBody CreateInstanceRequest request,
            @Parameter(description = "用户ID") @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        log.info("REST: Create instance, ontology={}, objectType={}, userId={}", 
                ontologyId, request.getObjectTypeId(), userId);
        InstanceResponse response = instanceService.createInstance(ontologyId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * 获取对象实例
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取对象实例", description = "根据ID获取对象实例详情")
    public ResponseEntity<ApiResponse<InstanceResponse>> getInstance(
            @Parameter(description = "本体ID") @PathVariable String ontologyId,
            @Parameter(description = "实例ID") @PathVariable String id) {
        log.debug("REST: Get instance, ontology={}, id={}", ontologyId, id);
        InstanceResponse response = instanceService.getInstance(ontologyId, id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 获取对象实例列表
     */
    @GetMapping
    @Operation(summary = "获取对象实例列表", description = "分页获取本体下的对象实例列表")
    public ResponseEntity<ApiResponse<ObjectListResponse<InstanceResponse>>> listInstances(
            @Parameter(description = "本体ID") @PathVariable String ontologyId,
            @Parameter(description = "对象类型ID") @RequestParam(required = false) String objectTypeId,
            @Parameter(description = "状态过滤") @RequestParam(required = false) String status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") int pageSize) {
        log.debug("REST: List instances, ontology={}, objectTypeId={}, page={}, pageSize={}", 
                ontologyId, objectTypeId, page, pageSize);
        
        InstanceQuery query = InstanceQuery.builder()
                .objectTypeId(objectTypeId)
                .status(status)
                .page(page)
                .pageSize(pageSize)
                .build();
        
        ObjectListResponse<InstanceResponse> response = instanceService.listInstances(ontologyId, query);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 根据对象类型查询实例
     */
    @GetMapping("/by-type/{typeId}")
    @Operation(summary = "根据类型查询实例", description = "获取指定对象类型的所有实例")
    public ResponseEntity<ApiResponse<ObjectListResponse<InstanceResponse>>> listInstancesByType(
            @Parameter(description = "本体ID") @PathVariable String ontologyId,
            @Parameter(description = "对象类型ID") @PathVariable String typeId) {
        log.debug("REST: List instances by type, ontology={}, typeId={}", ontologyId, typeId);
        ObjectListResponse<InstanceResponse> response = instanceService.listInstancesByType(ontologyId, typeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 更新对象实例
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新对象实例", description = "更新对象实例的属性")
    public ResponseEntity<ApiResponse<InstanceResponse>> updateInstance(
            @Parameter(description = "本体ID") @PathVariable String ontologyId,
            @Parameter(description = "实例ID") @PathVariable String id,
            @Valid @RequestBody UpdateInstanceRequest request) {
        log.info("REST: Update instance, ontology={}, id={}", ontologyId, id);
        InstanceResponse response = instanceService.updateInstance(ontologyId, id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 删除对象实例
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除对象实例", description = "删除指定的对象实例（逻辑删除）")
    public ResponseEntity<ApiResponse<Void>> deleteInstance(
            @Parameter(description = "本体ID") @PathVariable String ontologyId,
            @Parameter(description = "实例ID") @PathVariable String id) {
        log.info("REST: Delete instance, ontology={}, id={}", ontologyId, id);
        instanceService.deleteInstance(ontologyId, id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 批量导入实例
     */
    @PostMapping("/batch-import")
    @Operation(summary = "批量导入实例", description = "批量导入对象实例数据")
    public ResponseEntity<ApiResponse<BatchImportResponse>> batchImport(
            @Parameter(description = "本体ID") @PathVariable String ontologyId,
            @Valid @RequestBody BatchImportRequest request,
            @Parameter(description = "用户ID") @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        log.info("REST: Batch import instances, ontology={}, objectType={}, count={}", 
                ontologyId, request.getObjectTypeId(), request.getInstances().size());
        BatchImportResponse response = instanceService.batchImport(ontologyId, request, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 批量删除实例
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除实例", description = "批量删除指定的对象实例")
    public ResponseEntity<ApiResponse<Void>> batchDelete(
            @Parameter(description = "本体ID") @PathVariable String ontologyId,
            @RequestBody List<String> instanceIds) {
        log.info("REST: Batch delete instances, ontology={}, count={}", ontologyId, instanceIds.size());
        instanceService.batchDelete(ontologyId, instanceIds);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 验证单个实例
     */
    @PostMapping("/{id}/validate")
    @Operation(summary = "验证对象实例", description = "验证对象实例是否符合ObjectType定义")
    public ResponseEntity<ApiResponse<InstanceValidationResponse>> validateInstance(
            @Parameter(description = "本体ID") @PathVariable String ontologyId,
            @Parameter(description = "实例ID") @PathVariable String id) {
        log.info("REST: Validate instance, ontology={}, id={}", ontologyId, id);
        InstanceValidationResponse response = instanceService.validateInstance(ontologyId, id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 批量验证实例
     */
    @PostMapping("/validate")
    @Operation(summary = "批量验证实例", description = "批量验证对象实例是否符合ObjectType定义")
    public ResponseEntity<ApiResponse<InstanceValidationResponse>> validateInstances(
            @Parameter(description = "本体ID") @PathVariable String ontologyId,
            @RequestBody ValidateInstancesRequest request) {
        log.info("REST: Batch validate instances, ontology={}, count={}, strictMode={}", 
                ontologyId, request.getInstanceIds().size(), request.isStrictMode());
        InstanceValidationResponse response = instanceService.validateInstances(
                ontologyId, request.getInstanceIds(), request.isStrictMode());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 激活实例
     */
    @PostMapping("/{id}/activate")
    @Operation(summary = "激活实例", description = "将停用的实例重新激活")
    public ResponseEntity<ApiResponse<InstanceResponse>> activateInstance(
            @Parameter(description = "本体ID") @PathVariable String ontologyId,
            @Parameter(description = "实例ID") @PathVariable String id) {
        log.info("REST: Activate instance, ontology={}, id={}", ontologyId, id);
        InstanceResponse response = instanceService.activateInstance(ontologyId, id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 停用实例
     */
    @PostMapping("/{id}/deactivate")
    @Operation(summary = "停用实例", description = "将激活的实例停用")
    public ResponseEntity<ApiResponse<InstanceResponse>> deactivateInstance(
            @Parameter(description = "本体ID") @PathVariable String ontologyId,
            @Parameter(description = "实例ID") @PathVariable String id) {
        log.info("REST: Deactivate instance, ontology={}, id={}", ontologyId, id);
        InstanceResponse response = instanceService.deactivateInstance(ontologyId, id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 批量验证请求DTO
     */
    @lombok.Getter
    @lombok.Setter
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ValidateInstancesRequest {
        private List<String> instanceIds;
        
        @lombok.Builder.Default
        private boolean strictMode = false;
    }
}
