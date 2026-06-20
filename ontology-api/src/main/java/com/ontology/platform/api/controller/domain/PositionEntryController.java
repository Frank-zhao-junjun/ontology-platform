package com.ontology.platform.api.controller.domain;

import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.application.dto.domain.CreatePositionEntryRequest;
import com.ontology.platform.application.dto.domain.PositionEntryResponse;
import com.ontology.platform.application.service.PositionEntryService;
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
@RequestMapping("/v1/ontologies/{ontologyId}/positions")
@RequiredArgsConstructor
@Tag(name = "岗位", description = "岗位管理")
public class PositionEntryController {

    private final PositionEntryService positionEntryService;

    @PostMapping
    @Operation(summary = "创建岗位")
    public ResponseEntity<ApiResponse<PositionEntryResponse>> create(@Parameter(description = "本体ID") @PathVariable String ontologyId,
            @Valid @RequestBody CreatePositionEntryRequest request,
            @Parameter(description = "操作用户ID") @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(positionEntryService.create(ontologyId, request, userId)));
    }

    @GetMapping
    @Operation(summary = "获取岗位列表")
    public ResponseEntity<ApiResponse<List<PositionEntryResponse>>> list(@Parameter(description = "本体ID") @PathVariable String ontologyId) {
        return ResponseEntity.ok(ApiResponse.success(positionEntryService.listByOntologyId(ontologyId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取岗位详情")
    public ResponseEntity<ApiResponse<PositionEntryResponse>> getById(@Parameter(description = "岗位ID") @PathVariable String id) {
        PositionEntryResponse response = positionEntryService.getById(id);
        if (response == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除岗位")
    public ResponseEntity<ApiResponse<Void>> delete(@Parameter(description = "岗位ID") @PathVariable String id) {
        positionEntryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
