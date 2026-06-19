package com.ontology.platform.api.controller.domain;

import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.application.dto.domain.CreateEpcEdgeRequest;
import com.ontology.platform.application.dto.domain.EpcEdgeResponse;
import com.ontology.platform.application.service.EpcEdgeService;
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
@RequestMapping("/v1/ontologies/{ontologyId}/epc/edges")
@RequiredArgsConstructor
@Tag(name = "EPC边")
public class EpcEdgeController {
    private final EpcEdgeService epcEdgeService;

    @PostMapping @Operation(summary = "创建EPC边")
    public ResponseEntity<ApiResponse<EpcEdgeResponse>> create(@PathVariable String ontologyId,
            @Valid @RequestBody CreateEpcEdgeRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(epcEdgeService.create(ontologyId, request, userId)));
    }

    @GetMapping @Operation(summary = "列表")
    public ResponseEntity<ApiResponse<List<EpcEdgeResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(epcEdgeService.list()));
    }

    @GetMapping("/{id}") @Operation(summary = "详情")
    public ResponseEntity<ApiResponse<EpcEdgeResponse>> getById(@PathVariable String id) {
        EpcEdgeResponse r = epcEdgeService.getById(id);
        return r == null ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(ApiResponse.success(r));
    }

    @DeleteMapping("/{id}") @Operation(summary = "删除")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        epcEdgeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
