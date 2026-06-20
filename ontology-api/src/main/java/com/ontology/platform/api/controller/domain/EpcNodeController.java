package com.ontology.platform.api.controller.domain;

import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.application.dto.domain.CreateEpcNodeRequest;
import com.ontology.platform.application.dto.domain.EpcNodeResponse;
import com.ontology.platform.application.service.EpcNodeService;
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
@RequestMapping("/v1/ontologies/{ontologyId}/epc/nodes")
@RequiredArgsConstructor
@Tag(name = "EPC节点")
public class EpcNodeController {
    private final EpcNodeService epcNodeService;

    @PostMapping @Operation(summary = "创建EPC节点")
    public ResponseEntity<ApiResponse<EpcNodeResponse>> create(@Parameter(description = "本体ID") @PathVariable String ontologyId,
            @Valid @RequestBody CreateEpcNodeRequest request,
            @Parameter(description = "操作用户ID") @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(epcNodeService.create(ontologyId, request, userId)));
    }

    @GetMapping @Operation(summary = "列表")
    public ResponseEntity<ApiResponse<List<EpcNodeResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(epcNodeService.list()));
    }

    @GetMapping("/{id}") @Operation(summary = "详情")
    public ResponseEntity<ApiResponse<EpcNodeResponse>> getById(@Parameter(description = "EPC节点ID") @PathVariable String id) {
        EpcNodeResponse r = epcNodeService.getById(id);
        return r == null ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(ApiResponse.success(r));
    }

    @DeleteMapping("/{id}") @Operation(summary = "删除")
    public ResponseEntity<ApiResponse<Void>> delete(@Parameter(description = "EPC节点ID") @PathVariable String id) {
        epcNodeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
