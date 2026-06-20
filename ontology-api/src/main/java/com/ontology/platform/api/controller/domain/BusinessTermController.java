package com.ontology.platform.api.controller.domain;

import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.application.dto.domain.CreateBusinessTermRequest;
import com.ontology.platform.application.dto.domain.BusinessTermResponse;
import com.ontology.platform.application.service.BusinessTermService;
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

@Slf4j
@RestController
@RequestMapping("/v1/ontologies/{ontologyId}/business-terms")
@RequiredArgsConstructor
@Tag(name = "业务术语", description = "业务术语管理")
public class BusinessTermController {

    private final BusinessTermService businessTermService;

    @PostMapping
    @Operation(summary = "创建业务术语")
    public ResponseEntity<ApiResponse<BusinessTermResponse>> create(@Parameter(description = "本体ID") @PathVariable String ontologyId,
            @Valid @RequestBody CreateBusinessTermRequest request,
            @Parameter(description = "操作用户ID") @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(businessTermService.create(ontologyId, request, userId)));
    }

    @GetMapping
    @Operation(summary = "获取业务术语列表")
    public ResponseEntity<ApiResponse<List<BusinessTermResponse>>> list(@Parameter(description = "本体ID") @PathVariable String ontologyId) {
        return ResponseEntity.ok(ApiResponse.success(businessTermService.listByOntologyId(ontologyId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取业务术语详情")
    public ResponseEntity<ApiResponse<BusinessTermResponse>> getById(@Parameter(description = "业务术语ID") @PathVariable String id) {
        BusinessTermResponse response = businessTermService.getById(id);
        if (response == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除业务术语")
    public ResponseEntity<ApiResponse<Void>> delete(@Parameter(description = "业务术语ID") @PathVariable String id) {
        businessTermService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
