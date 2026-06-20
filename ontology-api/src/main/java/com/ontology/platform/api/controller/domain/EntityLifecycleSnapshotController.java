package com.ontology.platform.api.controller.domain;

import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.application.dto.domain.CreateEntityLifecycleSnapshotRequest;
import com.ontology.platform.application.dto.domain.EntityLifecycleSnapshotResponse;
import com.ontology.platform.application.service.EntityLifecycleSnapshotService;
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
@RequestMapping("/v1/ontologies/{ontologyId}/lifecycle-snapshots")
@RequiredArgsConstructor
@Tag(name = "生命周期快照")
public class EntityLifecycleSnapshotController {
    private final EntityLifecycleSnapshotService entityLifecycleSnapshotService;

    @PostMapping @Operation(summary = "创建生命周期快照")
    public ResponseEntity<ApiResponse<EntityLifecycleSnapshotResponse>> create(@Parameter(description = "本体ID") @PathVariable String ontologyId,
            @Valid @RequestBody CreateEntityLifecycleSnapshotRequest request,
            @Parameter(description = "操作用户ID") @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(entityLifecycleSnapshotService.create(ontologyId, request, userId)));
    }

    @GetMapping @Operation(summary = "列表")
    public ResponseEntity<ApiResponse<List<EntityLifecycleSnapshotResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(entityLifecycleSnapshotService.list()));
    }

    @GetMapping("/{id}") @Operation(summary = "详情")
    public ResponseEntity<ApiResponse<EntityLifecycleSnapshotResponse>> getById(@Parameter(description = "生命周期快照ID") @PathVariable String id) {
        EntityLifecycleSnapshotResponse r = entityLifecycleSnapshotService.getById(id);
        return r == null ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(ApiResponse.success(r));
    }

    @DeleteMapping("/{id}") @Operation(summary = "删除")
    public ResponseEntity<ApiResponse<Void>> delete(@Parameter(description = "生命周期快照ID") @PathVariable String id) {
        entityLifecycleSnapshotService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
