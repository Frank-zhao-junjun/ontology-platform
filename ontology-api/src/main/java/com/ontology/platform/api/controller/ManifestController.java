package com.ontology.platform.api.controller;

import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.application.dto.manifest.*;
import com.ontology.platform.application.service.manifest.ManifestService;
import com.ontology.platform.domain.vo.manifest.ManifestDocument;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/manifests")
@RequiredArgsConstructor
@Tag(name = "Manifest", description = "Manifest 导入/预览/发布/导出 API (P01-P03b)")
public class ManifestController {

    private final ManifestService manifestService;

    @PostMapping("/import")
    @Operation(summary = "导入 Manifest", description = "上传设计台导出的 OntologyManifest YAML/JSON")
    public ApiResponse<ImportManifestResponse> importManifest(@RequestBody ImportManifestRequest request) {
        return ApiResponse.success(manifestService.importManifest(request));
    }

    @PostMapping("/{id}/preview")
    @Operation(summary = "预览变更", description = "比较当前 draft 与上一已发布版本的差异")
    public ApiResponse<ManifestPreviewResponse> preview(@PathVariable String id) {
        return ApiResponse.success(manifestService.preview(id));
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "发布 Manifest", description = "DRAFT -> PUBLISHED，写入版本快照")
    public ApiResponse<ManifestPublishResponse> publish(@PathVariable String id) {
        return ApiResponse.success(manifestService.publish(id));
    }

    @GetMapping("/{id}/export")
    @Operation(summary = "导出 Manifest", description = "从已发布版本导出 YAML/JSON")
    public ApiResponse<ManifestDocument> exportManifest(
            @PathVariable String id,
            @RequestParam(defaultValue = "json") String format) {
        return ApiResponse.success(manifestService.export(id, format));
    }
}
