package com.ontology.platform.api.controller.domain;

import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.application.dto.domain.CreateBusinessMetricRequest;
import com.ontology.platform.application.dto.domain.BusinessMetricResponse;
import com.ontology.platform.application.service.BusinessMetricService;
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
@RequestMapping("/v1/ontologies/{ontologyId}/metrics")
@RequiredArgsConstructor
@Tag(name = "业务指标", description = "业务指标管理")
public class BusinessMetricController {

    private final BusinessMetricService businessMetricService;

    @PostMapping
    @Operation(summary = "创建业务指标")
    public ResponseEntity<ApiResponse<BusinessMetricResponse>> create(@PathVariable String ontologyId,
            @Valid @RequestBody CreateBusinessMetricRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(businessMetricService.create(ontologyId, request, userId)));
    }

    @GetMapping
    @Operation(summary = "获取业务指标列表")
    public ResponseEntity<ApiResponse<List<BusinessMetricResponse>>> list(@PathVariable String ontologyId) {
        return ResponseEntity.ok(ApiResponse.success(businessMetricService.listByOntologyId(ontologyId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取业务指标详情")
    public ResponseEntity<ApiResponse<BusinessMetricResponse>> getById(@PathVariable String id) {
        BusinessMetricResponse response = businessMetricService.getById(id);
        if (response == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除业务指标")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        businessMetricService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
