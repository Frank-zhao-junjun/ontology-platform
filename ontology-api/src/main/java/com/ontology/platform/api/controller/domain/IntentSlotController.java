package com.ontology.platform.api.controller.domain;

import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.application.dto.domain.CreateIntentSlotRequest;
import com.ontology.platform.application.dto.domain.IntentSlotResponse;
import com.ontology.platform.application.service.IntentSlotService;
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
@RequestMapping("/v1/ontologies/{ontologyId}/intent-slots")
@RequiredArgsConstructor
@Tag(name = "意图槽位")
public class IntentSlotController {
    private final IntentSlotService intentSlotService;

    @PostMapping @Operation(summary = "创建意图槽位")
    public ResponseEntity<ApiResponse<IntentSlotResponse>> create(@PathVariable String ontologyId,
            @Valid @RequestBody CreateIntentSlotRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(intentSlotService.create(ontologyId, request, userId)));
    }

    @GetMapping @Operation(summary = "列表")
    public ResponseEntity<ApiResponse<List<IntentSlotResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(intentSlotService.list()));
    }

    @GetMapping("/{id}") @Operation(summary = "详情")
    public ResponseEntity<ApiResponse<IntentSlotResponse>> getById(@PathVariable String id) {
        IntentSlotResponse r = intentSlotService.getById(id);
        return r == null ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(ApiResponse.success(r));
    }

    @DeleteMapping("/{id}") @Operation(summary = "删除")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        intentSlotService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
