package com.ontology.platform.api.controller.domain;

import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.application.dto.domain.CreateEpcProfileRequest;
import com.ontology.platform.application.dto.domain.EpcProfileResponse;
import com.ontology.platform.application.service.EpcProfileService;
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
@RequestMapping("/v1/ontologies/{ontologyId}/epc/profiles")
@RequiredArgsConstructor
@Tag(name = "EPC配置档案")
public class EpcProfileController {
    private final EpcProfileService epcProfileService;

    @PostMapping @Operation(summary = "创建EPC配置档案")
    public ResponseEntity<ApiResponse<EpcProfileResponse>> create(@PathVariable String ontologyId,
            @Valid @RequestBody CreateEpcProfileRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(epcProfileService.create(ontologyId, request, userId)));
    }

    @GetMapping @Operation(summary = "列表")
    public ResponseEntity<ApiResponse<List<EpcProfileResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(epcProfileService.list()));
    }

    @GetMapping("/{id}") @Operation(summary = "详情")
    public ResponseEntity<ApiResponse<EpcProfileResponse>> getById(@PathVariable String id) {
        EpcProfileResponse r = epcProfileService.getById(id);
        return r == null ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(ApiResponse.success(r));
    }

    @DeleteMapping("/{id}") @Operation(summary = "删除")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        epcProfileService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
