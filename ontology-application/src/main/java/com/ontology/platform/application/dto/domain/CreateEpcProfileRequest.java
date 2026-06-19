package com.ontology.platform.application.dto.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "创建EpcProfile请求")
public class CreateEpcProfileRequest {
    @Schema(description = "chain_id") private String chainId;
    @Schema(description = "profile_data") private String profileData;
    @Schema(description = "profile_version") private String profileVersion;
    @Schema(description = "is_active") private Boolean isActive;
}
