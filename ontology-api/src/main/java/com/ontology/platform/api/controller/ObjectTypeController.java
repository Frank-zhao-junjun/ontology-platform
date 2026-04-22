package com.ontology.platform.api.controller;

import com.ontology.platform.application.dto.*;
import com.ontology.platform.application.service.ObjectTypeService;
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
 * 对象类型管理API控制器
 * ObjectType Management API Controller
 */
@Slf4j
@RestController
@RequestMapping("/v1/ontologies/{ontologyId}/object-types")
@RequiredArgsConstructor
@Tag(name = "ObjectType", description = "对象类型管理API")
public class ObjectTypeController {

    private final ObjectTypeService objectTypeService;

    @PostMapping
    @Operation(summary = "创建对象类型", description = "在指定本体下创建一个新的对象类型")
    public ResponseEntity<ApiResponse<ObjectTypeResponse>> createObjectType(
            @Parameter(description = "本体ID") @PathVariable String ontologyId,
            @Valid @RequestBody CreateObjectTypeRequest request) {
        log.info("REST: Create object type, ontologyId={}, name={}", ontologyId, request.getName());

        if (!ontologyId.equals(request.getOntologyId())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(3002, "路径中的ontologyId与请求体中的ontologyId不一致"));
        }

        ObjectTypeResponse response = objectTypeService.createObjectType(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "获取对象类型列表", description = "获取指定本体下的所有对象类型")
    public ResponseEntity<ApiResponse<List<ObjectTypeResponse>>> listObjectTypes(
            @Parameter(description = "本体ID") @PathVariable String ontologyId) {
        log.debug("REST: List object types, ontologyId={}", ontologyId);
        List<ObjectTypeResponse> response = objectTypeService.listObjectTypes(ontologyId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取对象类型详情", description = "根据ID获取对象类型的详细信息，包括属性列表")
    public ResponseEntity<ApiResponse<ObjectTypeDetailResponse>> getObjectType(
            @Parameter(description = "本体ID") @PathVariable String ontologyId,
            @Parameter(description = "对象类型ID") @PathVariable String id) {
        log.debug("REST: Get object type, ontologyId={}, id={}", ontologyId, id);
        ObjectTypeDetailResponse response = objectTypeService.getObjectTypeById(id);

        if (!ontologyId.equals(response.getOntologyId())) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新对象类型", description = "更新指定对象类型的信息")
    public ResponseEntity<ApiResponse<ObjectTypeResponse>> updateObjectType(
            @Parameter(description = "本体ID") @PathVariable String ontologyId,
            @Parameter(description = "对象类型ID") @PathVariable String id,
            @Valid @RequestBody UpdateObjectTypeRequest request) {
        log.info("REST: Update object type, ontologyId={}, id={}", ontologyId, id);
        ObjectTypeResponse response = objectTypeService.updateObjectType(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除对象类型", description = "删除指定的对象类型，同时会删除关联的属性")
    public ResponseEntity<ApiResponse<Void>> deleteObjectType(
            @Parameter(description = "本体ID") @PathVariable String ontologyId,
            @Parameter(description = "对象类型ID") @PathVariable String id) {
        log.info("REST: Delete object type, ontologyId={}, id={}", ontologyId, id);
        objectTypeService.deleteObjectType(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{objectTypeId}/properties")
    @Operation(summary = "创建属性", description = "在指定对象类型下创建一个新的属性")
    public ResponseEntity<ApiResponse<PropertyResponse>> createProperty(
            @Parameter(description = "本体ID") @PathVariable String ontologyId,
            @Parameter(description = "对象类型ID") @PathVariable String objectTypeId,
            @Valid @RequestBody CreatePropertyRequest request) {
        log.info("REST: Create property, ontologyId={}, objectTypeId={}, name={}",
                ontologyId, objectTypeId, request.getName());

        if (!objectTypeId.equals(request.getObjectTypeId())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(3002, "路径中的objectTypeId与请求体中的objectTypeId不一致"));
        }

        PropertyResponse response = objectTypeService.createProperty(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @GetMapping("/{objectTypeId}/properties")
    @Operation(summary = "获取属性列表", description = "获取指定对象类型下的所有属性")
    public ResponseEntity<ApiResponse<List<PropertyResponse>>> listProperties(
            @Parameter(description = "本体ID") @PathVariable String ontologyId,
            @Parameter(description = "对象类型ID") @PathVariable String objectTypeId) {
        log.debug("REST: List properties, ontologyId={}, objectTypeId={}", ontologyId, objectTypeId);
        List<PropertyResponse> response = objectTypeService.listProperties(objectTypeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{objectTypeId}/properties/{propertyId}")
    @Operation(summary = "更新属性", description = "更新指定属性的信息")
    public ResponseEntity<ApiResponse<PropertyResponse>> updateProperty(
            @Parameter(description = "本体ID") @PathVariable String ontologyId,
            @Parameter(description = "对象类型ID") @PathVariable String objectTypeId,
            @Parameter(description = "属性ID") @PathVariable String propertyId,
            @Valid @RequestBody UpdatePropertyRequest request) {
        log.info("REST: Update property, ontologyId={}, objectTypeId={}, propertyId={}",
                ontologyId, objectTypeId, propertyId);
        PropertyResponse response = objectTypeService.updateProperty(propertyId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{objectTypeId}/properties/{propertyId}")
    @Operation(summary = "删除属性", description = "删除指定的属性")
    public ResponseEntity<ApiResponse<Void>> deleteProperty(
            @Parameter(description = "本体ID") @PathVariable String ontologyId,
            @Parameter(description = "对象类型ID") @PathVariable String objectTypeId,
            @Parameter(description = "属性ID") @PathVariable String propertyId) {
        log.info("REST: Delete property, ontologyId={}, objectTypeId={}, propertyId={}",
                ontologyId, objectTypeId, propertyId);
        objectTypeService.deleteProperty(propertyId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{objectTypeId}/properties/batch")
    @Operation(summary = "批量创建属性", description = "在指定对象类型下批量创建属性")
    public ResponseEntity<ApiResponse<List<PropertyResponse>>> batchCreateProperties(
            @Parameter(description = "本体ID") @PathVariable String ontologyId,
            @Parameter(description = "对象类型ID") @PathVariable String objectTypeId,
            @Valid @RequestBody List<CreatePropertyRequest> requests) {
        log.info("REST: Batch create properties, ontologyId={}, objectTypeId={}, count={}",
                ontologyId, objectTypeId, requests.size());

        List<PropertyResponse> response = objectTypeService.batchCreateProperties(objectTypeId, requests);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }
}
