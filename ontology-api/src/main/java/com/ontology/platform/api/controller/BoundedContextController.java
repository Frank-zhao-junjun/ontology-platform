package com.ontology.platform.api.controller;

import com.ontology.platform.api.dto.*;
import com.ontology.platform.application.manifest.ManifestSnapshotService;
import com.ontology.platform.application.service.BoundedContextService;
import com.ontology.platform.application.service.WorkflowService;
import com.ontology.platform.common.enums.DomainTag;
import com.ontology.platform.domain.entity.BoundedContext;
import com.ontology.platform.domain.entity.PublishedManifest;
import com.ontology.platform.domain.entity.ReviewComment;
import com.ontology.platform.domain.entity.WorkflowStateLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/contexts")
@RequiredArgsConstructor
@Tag(name = "Contexts")
public class BoundedContextController {
    private final BoundedContextService service;
    private final ManifestSnapshotService manifestSnapshotService;
    private final WorkflowService workflowService;

    @PostMapping
    @Operation(summary = "创建限界上下文 (US-S01)")
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody BoundedContextCreateRequest req) {
        BoundedContext ctx = service.create(req.getName(), req.getCode(), req.getDescription(), DomainTag.fromCode(req.getDomainTag()), "user");
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("code", 0, "message", "success", "data", BoundedContextResponse.from(ctx)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> get(@PathVariable String id) {
        BoundedContext ctx = service.findById(id);
        return ResponseEntity.ok(Map.of("code", 0, "message", "success", "data", BoundedContextResponse.from(ctx)));
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> list() {
        var data = service.findAll().stream().map(BoundedContextResponse::from).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("code", 0, "message", "success", "data", data));
    }

    @PostMapping("/{id}/submit-review")
    @Operation(summary = "提交审核 (US-G05 AC-1)")
    public ResponseEntity<Map<String, Object>> submitReview(@PathVariable String id,
                                                            @RequestBody(required = false) WorkflowTransitionRequest req) {
        String operatedBy = req != null && req.getOperatedBy() != null ? req.getOperatedBy() : "user";
        String comment = req != null ? req.getComment() : null;
        return ResponseEntity.ok(Map.of("code", 0, "message", "ok", "data", BoundedContextResponse.from(service.submitForReview(id, operatedBy, comment))));
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "批准发布并生成 Manifest 快照 (US-G05 AC-4 + US-A01 Round 3)")
    public ResponseEntity<Map<String, Object>> approve(@PathVariable String id,
                                                       @RequestBody(required = false) WorkflowTransitionRequest req) throws Exception {
        String operatedBy = req != null && req.getOperatedBy() != null ? req.getOperatedBy() : "user";
        String comment = req != null ? req.getComment() : null;
        PublishedManifest manifest = service.approvePublishAndSnapshot(id, operatedBy, comment);
        return ResponseEntity.ok(Map.of("code", 0, "message", "ok", "data", Map.of(
                "context", BoundedContextResponse.from(service.findById(id)),
                "publishedManifest", Map.of(
                        "id", manifest.getId(),
                        "version", manifest.getVersion(),
                        "ontologyId", manifest.getOntologyId()))));
    }

    @GetMapping("/{id}/manifests")
    public ResponseEntity<Map<String, Object>> listManifests(@PathVariable String id) {
        var data = manifestSnapshotService.listManifests(id).stream()
                .map(m -> Map.of("id", m.getId(), "version", m.getVersion(), "createdAt", m.getCreatedAt().toString()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("code", 0, "message", "success", "data", data));
    }

    @GetMapping("/{id}/manifests/latest")
    public ResponseEntity<Map<String, Object>> latestManifest(@PathVariable String id) {
        PublishedManifest m = manifestSnapshotService.getLatest(id);
        return ResponseEntity.ok(Map.of("code", 0, "message", "success", "data", Map.of(
                "id", m.getId(), "version", m.getVersion(), "ontologyId", m.getOntologyId(),
                "snapshotJson", m.getSnapshotJson())));
    }

    @PostMapping("/{id}/reject-review")
    @Operation(summary = "拒绝退回草稿 (US-G05 AC-3)")
    public ResponseEntity<Map<String, Object>> reject(@PathVariable String id,
                                                      @RequestBody(required = false) WorkflowTransitionRequest req) {
        String operatedBy = req != null && req.getOperatedBy() != null ? req.getOperatedBy() : "user";
        String comment = req != null ? req.getComment() : null;
        return ResponseEntity.ok(Map.of("code", 0, "message", "ok", "data", BoundedContextResponse.from(service.rejectToDraft(id, operatedBy, comment))));
    }

    // ── 工作流审计 ──

    @GetMapping("/{id}/workflow-log")
    @Operation(summary = "查询状态流转日志 (US-G05 AC-5)")
    public ResponseEntity<Map<String, Object>> workflowLog(@PathVariable String id) {
        List<WorkflowStateLog> logs = workflowService.getWorkflowLog(id);
        var data = logs.stream().map(WorkflowStateLogResponse::from).map(WorkflowStateLogResponse::toMap).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("code", 0, "message", "success", "data", data));
    }

    // ── 审核批注 ──

    @PostMapping("/{id}/review-comments")
    @Operation(summary = "提交审核批注 (US-G05 AC-2)")
    public ResponseEntity<Map<String, Object>> addReviewComment(@PathVariable String id,
                                                                @Valid @RequestBody ReviewCommentRequest req) {
        ReviewComment comment = workflowService.createReviewComment(id, req.getTargetType(), req.getTargetId(),
                req.getReviewer(), req.getResolution(), req.getContent());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("code", 0, "message", "success", "data", ReviewCommentResponse.from(comment).toMap()));
    }

    @GetMapping("/{id}/review-comments")
    @Operation(summary = "查询审核批注列表")
    public ResponseEntity<Map<String, Object>> listReviewComments(@PathVariable String id) {
        List<ReviewComment> comments = workflowService.getReviewComments(id);
        var data = comments.stream().map(ReviewCommentResponse::from).map(ReviewCommentResponse::toMap).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("code", 0, "message", "success", "data", data));
    }

    @PutMapping("/{id}/review-comments/{commentId}/resolve")
    @Operation(summary = "更新批注决议状态")
    public ResponseEntity<Map<String, Object>> resolveComment(@PathVariable String id,
                                                              @PathVariable String commentId,
                                                              @RequestParam String resolution) {
        ReviewComment comment = workflowService.resolveComment(commentId, resolution);
        return ResponseEntity.ok(Map.of("code", 0, "message", "success", "data", ReviewCommentResponse.from(comment).toMap()));
    }
}
