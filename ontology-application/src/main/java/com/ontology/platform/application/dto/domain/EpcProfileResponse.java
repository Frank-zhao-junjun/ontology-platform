package com.ontology.platform.application.dto.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.Instant;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "EpcProfile响应")
public class EpcProfileResponse {
    @Schema(description = "ID") private String id;
    @Schema(description = "chain_id") private String chainId;
    @Schema(description = "profile_data") private String profileData;
    @Schema(description = "profile_version") private String profileVersion;
    @Schema(description = "is_active") private Boolean isActive;
    @Schema(description = "创建时间") private Instant createdAt;
    @Schema(description = "更新时间") private Instant updatedAt;
}
