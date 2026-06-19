package com.ontology.platform.api.controller.domain;

import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.application.dto.domain.CreateEpcModelRefRequest;
import com.ontology.platform.application.dto.domain.EpcModelRefResponse;
import com.ontology.platform.application.service.EpcModelRefService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j @RestController
@RequestMapping("/v1/ontologies/{ontologyId}/epc/model-refs")
@RequiredArgsConstructor
@Tag(name = "EPC模型引用")
public class EpcModelRefController {
    private final EpcModelRefService epcModelRefService;

    @PostMapping @Operation(summary = "创建EPC模型引用")
    public ResponseEntity<ApiResponse<EpcModelRefResponse>> create(@PathVariable String ontologyId,
            @Valid @RequestBody CreateEpcModelRefRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(epcModelRefService.create(ontologyId, request, userId)));
    }

    @GetMapping @Operation(summary = "列表")
    public ResponseEntity<ApiResponse<List<EpcModelRefResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(epcModelRefService.list()));
    }

    @GetMapping("/{id}") @Operation(summary = "详情")
    public ResponseEntity<ApiResponse<EpcModelRefResponse>> getById(@PathVariable String id) {
        EpcModelRefResponse r = epcModelRefService.getById(id);
        return r == null ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(ApiResponse.success(r));
    }

    @DeleteMapping("/{id}") @Operation(summary = "删除")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        epcModelRefService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
