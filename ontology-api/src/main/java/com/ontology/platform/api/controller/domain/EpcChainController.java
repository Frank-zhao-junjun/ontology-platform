package com.ontology.platform.api.controller.domain;

import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.application.dto.domain.CreateEpcChainRequest;
import com.ontology.platform.application.dto.domain.EpcChainResponse;
import com.ontology.platform.application.service.EpcChainService;
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

@Slf4j @RestController
@RequestMapping("/api/v1/ontologies/{ontologyId}/epc/chains")
@RequiredArgsConstructor
@Tag(name = "EPC链")
public class EpcChainController {
    private final EpcChainService epcChainService;

    @PostMapping @Operation(summary = "创建EPC链")
    public ResponseEntity<ApiResponse<EpcChainResponse>> create(@Parameter(description = "本体ID") @PathVariable String ontologyId,
            @Valid @RequestBody CreateEpcChainRequest request,
            @Parameter(description = "操作用户ID") @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(epcChainService.create(ontologyId, request, userId)));
    }

    @GetMapping @Operation(summary = "列表")
    public ResponseEntity<ApiResponse<List<EpcChainResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(epcChainService.list()));
    }

    @GetMapping("/{id}") @Operation(summary = "详情")
    public ResponseEntity<ApiResponse<EpcChainResponse>> getById(@Parameter(description = "EPC链ID") @PathVariable String id) {
        EpcChainResponse r = epcChainService.getById(id);
        return r == null ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(ApiResponse.success(r));
    }

    @DeleteMapping("/{id}") @Operation(summary = "删除")
    public ResponseEntity<ApiResponse<Void>> delete(@Parameter(description = "EPC链ID") @PathVariable String id) {
        epcChainService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
