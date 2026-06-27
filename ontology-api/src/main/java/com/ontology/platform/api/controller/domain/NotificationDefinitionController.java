package com.ontology.platform.api.controller.domain;

import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.application.dto.domain.CreateNotificationDefinitionRequest;
import com.ontology.platform.application.dto.domain.NotificationDefinitionResponse;
import com.ontology.platform.application.service.NotificationDefinitionService;
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
@RequestMapping("/api/v1/ontologies/{ontologyId}/notifications")
@RequiredArgsConstructor
@Tag(name = "通知定义", description = "消息通知渠道")
public class NotificationDefinitionController {

    private final NotificationDefinitionService notificationDefinitionService;

    @PostMapping
    @Operation(summary = "创建通知定义", description = "在指定本体下创建消息通知渠道")
    public ResponseEntity<ApiResponse<NotificationDefinitionResponse>> create(
            @Parameter(description = "本体ID") @PathVariable String ontologyId,
            @Valid @RequestBody CreateNotificationDefinitionRequest request,
            @Parameter(description = "操作用户ID") @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        log.info("REST Create NotificationDefinition: ontologyId={}", ontologyId);
        NotificationDefinitionResponse response = notificationDefinitionService.create(ontologyId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "获取通知定义列表", description = "获取指定本体下所有消息通知渠道")
    public ResponseEntity<ApiResponse<List<NotificationDefinitionResponse>>> list(@Parameter(description = "本体ID") @PathVariable String ontologyId) {
        List<NotificationDefinitionResponse> list = notificationDefinitionService.listByOntologyId(ontologyId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取通知定义详情", description = "根据ID获取消息通知渠道详细信息")
    public ResponseEntity<ApiResponse<NotificationDefinitionResponse>> getById(@Parameter(description = "通知定义ID") @PathVariable String id) {
        NotificationDefinitionResponse response = notificationDefinitionService.getById(id);
        if (response == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新通知定义", description = "更新消息通知渠道")
    public ResponseEntity<ApiResponse<NotificationDefinitionResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody CreateNotificationDefinitionRequest request) {
        NotificationDefinitionResponse response = notificationDefinitionService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除通知定义", description = "删除消息通知渠道")
    public ResponseEntity<ApiResponse<Void>> delete(@Parameter(description = "通知定义ID") @PathVariable String id) {
        notificationDefinitionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
