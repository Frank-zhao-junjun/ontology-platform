package com.ontology.platform.api.controller.domain;

import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.application.dto.domain.CreateDepartmentRequest;
import com.ontology.platform.application.dto.domain.DepartmentResponse;
import com.ontology.platform.application.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/ontologies/{ontologyId}/departments")
@RequiredArgsConstructor
@Tag(name = "部门", description = "部门管理")
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    @Operation(summary = "创建部门")
    public ResponseEntity<ApiResponse<DepartmentResponse>> create(@PathVariable String ontologyId,
            @Valid @RequestBody CreateDepartmentRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(departmentService.create(ontologyId, request, userId)));
    }

    @GetMapping
    @Operation(summary = "获取部门列表")
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> list(@PathVariable String ontologyId) {
        return ResponseEntity.ok(ApiResponse.success(departmentService.listByOntologyId(ontologyId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取部门详情")
    public ResponseEntity<ApiResponse<DepartmentResponse>> getById(@PathVariable String id) {
        DepartmentResponse response = departmentService.getById(id);
        if (response == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除部门")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        departmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
