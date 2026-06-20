package com.ontology.platform.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

/**
 * 构建信息响应
 * Build Information Response
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "构建信息响应")
public class BuildInfoResponse {

    @Schema(description = "构建版本")
    private String version;

    @Schema(description = "构建时间")
    private String buildTime;

    @Schema(description = "JDK 版本")
    private String javaVersion;

    @Schema(description = "当前激活的 profile")
    private List<String> activeProfiles;

    @Schema(description = "测试总数")
    private int testCount;

    @Schema(description = "应用启动时长（毫秒）")
    private long uptime;
}
