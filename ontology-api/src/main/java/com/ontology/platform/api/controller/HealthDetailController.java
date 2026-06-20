package com.ontology.platform.api.controller;

import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.application.dto.BuildInfoResponse;
import com.ontology.platform.application.service.BuildInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 健康检查详情控制器
 * Health Detail Controller — exposes aggregated build & runtime information.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
@Tag(name = "健康检查")
public class HealthDetailController {

    private final BuildInfoService buildInfoService;

    /**
     * 获取健康检查详情（构建版本、运行时信息、测试统计等）
     */
    @GetMapping("/details")
    @Operation(
            summary = "获取健康检查详情",
            description = "返回聚合的构建信息与运行时健康状态，包括版本号、构建时间、Java 版本、活跃 Profile、测试数量、运行时长等"
    )
    @ApiResponse(responseCode = "200", description = "成功返回健康检查详情")
    public ResponseEntity<ApiResponse<BuildInfoResponse>> getHealthDetails() {
        log.debug("REST: Get health details");
        BuildInfoResponse buildInfo = buildInfoService.getBuildInfo();
        return ResponseEntity.ok(ApiResponse.success(buildInfo));
    }
}
